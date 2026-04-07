package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.constant.MessageConstant;
import cn.edu.seig.vibemusic.mapper.CommentMapper;
import cn.edu.seig.vibemusic.mapper.CommunityPostMapper;
import cn.edu.seig.vibemusic.mapper.ReportMapper;
import cn.edu.seig.vibemusic.mapper.ReportStatsMapper;
import cn.edu.seig.vibemusic.model.dto.ReportDTO;
import cn.edu.seig.vibemusic.model.entity.Comment;
import cn.edu.seig.vibemusic.model.entity.CommunityPost;
import cn.edu.seig.vibemusic.model.entity.Report;
import cn.edu.seig.vibemusic.model.entity.ReportStats;
import cn.edu.seig.vibemusic.model.vo.ReportVO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.INotificationService;
import cn.edu.seig.vibemusic.service.ReportService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 举报服务实现类
 *
 * @author system
 * @since 2026-03-11
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ReportStatsMapper reportStatsMapper;

    @Autowired
    private CommunityPostMapper communityPostMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private INotificationService notificationService;

    // 自动隐藏阈值：举报次数达到此值自动隐藏内容
    private static final int AUTO_HIDE_THRESHOLD = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result submitReport(ReportDTO reportDTO, Long reporterId) {
        // 验证参数
        if (reportDTO.getTargetType() == null || reportDTO.getTargetId() == null) {
            return Result.error("参数错误");
        }

        // 检查是否已举报过
        if (hasReported(reporterId, reportDTO.getTargetType(), reportDTO.getTargetId())) {
            return Result.error("您已举报过该内容");
        }

        // 检查目标是否存在
        if (!checkTargetExists(reportDTO.getTargetType(), reportDTO.getTargetId())) {
            return Result.error("举报目标不存在");
        }

        // 创建举报记录
        Report report = new Report();
        BeanUtils.copyProperties(reportDTO, report);
        report.setReporterId(reporterId);
        report.setStatus(0); // 待处理
        report.setCreateTime(LocalDateTime.now());

        if (reportMapper.insert(report) == 0) {
            return Result.error("举报提交失败");
        }

        // 更新举报统计
        updateReportStats(reportDTO.getTargetType(), reportDTO.getTargetId());

        log.info(">>> [举报] 用户 {} 举报了 {} ID={}", reporterId, 
                 reportDTO.getTargetType() == 1 ? "帖子" : "评论", reportDTO.getTargetId());

        return Result.success("举报提交成功，我们会尽快处理");
    }

    @Override
    public boolean hasReported(Long reporterId, Integer targetType, Long targetId) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, reporterId)
               .eq(Report::getTargetType, targetType)
               .eq(Report::getTargetId, targetId)
               .eq(Report::getStatus, 0);
        return reportMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Result<Page<ReportVO>> getReportList(Integer pageNum, Integer pageSize, 
                                                  Integer status, Integer targetType) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Page<ReportVO> page = new Page<>(pageNum, pageSize);
        IPage<ReportVO> reportPage = reportMapper.selectReportPage(page, status, targetType);
        
        // 将IPage转换为Page
        Page<ReportVO> result = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        result.setRecords(reportPage.getRecords());

        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result handleReport(Long reportId, String handleResult, Long handlerId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            return Result.error("举报记录不存在");
        }

        if (report.getStatus() != 0) {
            return Result.error("该举报已处理");
        }

        // 更新举报状态
        report.setStatus(1); // 已处理
        report.setHandlerId(handlerId);
        report.setHandleResult(handleResult);
        report.setHandleTime(LocalDateTime.now());

        if (reportMapper.updateById(report) == 0) {
            return Result.error("处理失败");
        }

        // 处理举报时同步处置对应内容，避免管理员还要再次手动删除
        disposeReportedContent(report.getTargetType(), report.getTargetId());

        // 发送通知
        sendReportNotifications(report, handleResult, true);

        log.info(">>> [举报] 管理员 {} 处理了举报 ID={}", handlerId, reportId);

        return Result.success("处理成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result rejectReport(Long reportId, String handleResult, Long handlerId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            return Result.error("举报记录不存在");
        }

        if (report.getStatus() != 0) {
            return Result.error("该举报已处理");
        }

        // 更新举报状态
        report.setStatus(2); // 已驳回
        report.setHandlerId(handlerId);
        report.setHandleResult(handleResult);
        report.setHandleTime(LocalDateTime.now());

        if (reportMapper.updateById(report) == 0) {
            return Result.error("驳回失败");
        }

        // 发送通知
        sendReportNotifications(report, handleResult, false);

        log.info(">>> [举报] 管理员 {} 驳回了举报 ID={}", handlerId, reportId);

        return Result.success("驳回成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result resetReportData(Long adminId) {
        // 只清理举报记录与举报统计，不动帖子、评论等业务数据
        reportStatsMapper.deleteAllReportStats();
        reportMapper.deleteAllReports();

        log.warn(">>> [举报] 管理员 {} 已清空全部举报记录与举报统计", adminId);
        return Result.success("举报数据已重置");
    }

    /**
     * 检查目标是否存在
     */
    private boolean checkTargetExists(Integer targetType, Long targetId) {
        if (targetType == 1) {
            // 帖子
            CommunityPost post = communityPostMapper.selectById(targetId);
            return post != null && post.getStatus() != -1;
        } else if (targetType == 2) {
            // 评论
            Comment comment = commentMapper.selectById(targetId);
            return comment != null;
        }
        return false;
    }

    /**
     * 更新举报统计
     */
    private void updateReportStats(Integer targetType, Long targetId) {
        LambdaQueryWrapper<ReportStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportStats::getTargetType, targetType)
               .eq(ReportStats::getTargetId, targetId);
        ReportStats stats = reportStatsMapper.selectOne(wrapper);

        if (stats == null) {
            // 创建新统计
            stats = new ReportStats();
            stats.setTargetType(targetType);
            stats.setTargetId(targetId);
            stats.setReportCount(1);
            stats.setLastReportTime(LocalDateTime.now());
            stats.setIsAutoHidden(0);
            reportStatsMapper.insert(stats);
        } else {
            // 更新统计
            stats.setReportCount(stats.getReportCount() + 1);
            stats.setLastReportTime(LocalDateTime.now());
            reportStatsMapper.updateById(stats);

            // 检查是否需要自动隐藏
            if (stats.getReportCount() >= AUTO_HIDE_THRESHOLD && stats.getIsAutoHidden() == 0) {
                autoHideContent(targetType, targetId);
                stats.setIsAutoHidden(1);
                reportStatsMapper.updateById(stats);
            }
        }
    }

    /**
     * 自动隐藏内容
     */
    private void autoHideContent(Integer targetType, Long targetId) {
        if (targetType == 1) {
            // 隐藏帖子（软删除）
            CommunityPost post = communityPostMapper.selectById(targetId);
            if (post != null) {
                post.setStatus(-1);
                communityPostMapper.updateById(post);
                log.warn(">>> [举报] 帖子 ID={} 因举报次数过多被自动隐藏", targetId);
            }
        } else if (targetType == 2) {
            // 删除评论
            deleteReportedComment(targetId);
            log.warn(">>> [举报] 评论 ID={} 因举报次数过多被自动删除", targetId);
        }
    }

    /**
     * 管理员处理举报后，直接执行对应内容处置
     */
    private void disposeReportedContent(Integer targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return;
        }

        if (targetType == 1) {
            CommunityPost post = communityPostMapper.selectById(targetId);
            if (post != null && post.getStatus() != -1) {
                post.setStatus(-1);
                communityPostMapper.updateById(post);
            }
        } else if (targetType == 2) {
            deleteReportedComment(targetId);
        }
    }

    /**
     * 删除被举报评论，并处理社区评论的回复与计数
     */
    private void deleteReportedComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return;
        }

        commentMapper.deleteById(commentId);

        if (comment.getCommentType() != null && comment.getCommentType() == 3 && comment.getTargetId() != null) {
            communityPostMapper.decrementCommentCount(comment.getTargetId());
        }

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId);
        List<Comment> replies = commentMapper.selectList(wrapper);
        if (replies == null || replies.isEmpty()) {
            return;
        }

        for (Comment reply : replies) {
            commentMapper.deleteById(reply.getCommentId());
            if (comment.getCommentType() != null && comment.getCommentType() == 3 && comment.getTargetId() != null) {
                communityPostMapper.decrementCommentCount(comment.getTargetId());
            }
        }
    }

    /**
     * 发送举报处理通知
     */
    private void sendReportNotifications(Report report, String handleResult, boolean isHandled) {
        try {
            String targetTypeName = report.getTargetType() == 1 ? "帖子" : "评论";
            String reasonTypeName = getReasonTypeName(report.getReasonType());

            // 1. 通知举报人
            String reporterTitle = isHandled ? "举报处理结果" : "举报已驳回";
            String reporterContent = isHandled 
                ? String.format("您举报的%s已被管理员处理。处理结果：%s", targetTypeName, handleResult)
                : String.format("您举报的%s经审核不符合违规标准。驳回原因：%s", targetTypeName, handleResult);
            
            notificationService.createNotificationsEnhanced(
                Collections.singletonList(report.getReporterId()),
                reporterTitle,
                reporterContent,
                "REPORT",
                "NORMAL",
                report.getHandlerId()
            );

            // 2. 如果是处理（不是驳回），通知内容作者
            if (isHandled) {
                Long authorId = getContentAuthorId(report.getTargetType(), report.getTargetId());
                if (authorId != null) {
                    String authorTitle = "内容违规通知";
                    String authorContent = String.format(
                        "您的%s因违反社区规范被举报并下架。违规原因：%s。处理结果：%s",
                        targetTypeName,
                        reasonTypeName,
                        handleResult
                    );
                    
                    notificationService.createNotificationsEnhanced(
                        Collections.singletonList(authorId),
                        authorTitle,
                        authorContent,
                        "VIOLATION",
                        "HIGH",
                        report.getHandlerId()
                    );
                }
            }

            log.info(">>> [举报] 已发送举报处理通知，举报ID={}", report.getId());
        } catch (Exception e) {
            log.error(">>> [举报] 发送通知失败", e);
            // 通知发送失败不影响主流程
        }
    }

    /**
     * 获取内容作者ID
     */
    private Long getContentAuthorId(Integer targetType, Long targetId) {
        if (targetType == 1) {
            CommunityPost post = communityPostMapper.selectById(targetId);
            return post != null ? post.getUserId() : null;
        } else if (targetType == 2) {
            Comment comment = commentMapper.selectById(targetId);
            return comment != null ? comment.getUserId() : null;
        }
        return null;
    }

    /**
     * 获取举报原因名称
     */
    private String getReasonTypeName(String reasonType) {
        switch (reasonType) {
            case "SPAM": return "垃圾广告";
            case "ILLEGAL": return "违规内容";
            case "ABUSE": return "侮辱谩骂";
            case "PORN": return "色情低俗";
            case "FAKE": return "虚假信息";
            case "COPYRIGHT": return "侵权内容";
            case "OTHER": return "其他原因";
            default: return reasonType;
        }
    }
}
