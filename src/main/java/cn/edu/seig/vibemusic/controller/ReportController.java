package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.constant.JwtClaimsConstant;
import cn.edu.seig.vibemusic.model.dto.ReportDTO;
import cn.edu.seig.vibemusic.model.vo.ReportVO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.ReportService;
import cn.edu.seig.vibemusic.util.ThreadLocalUtil;
import cn.edu.seig.vibemusic.util.TypeConversionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 举报控制器
 *
 * @author system
 * @since 2026-03-11
 */
@Slf4j
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 提交举报
     */
    @PostMapping("/submit")
    public Result submitReport(@RequestBody ReportDTO reportDTO) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.USER_ID));
        
        log.info(">>> [举报] 用户 {} 提交举报: {}", userId, reportDTO);
        return reportService.submitReport(reportDTO, userId);
    }

    /**
     * 检查是否已举报
     */
    @GetMapping("/check")
    public Result<Boolean> checkReported(@RequestParam Integer targetType,
                                          @RequestParam Long targetId) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.USER_ID));
        
        boolean hasReported = reportService.hasReported(userId, targetType, targetId);
        return Result.success(hasReported);
    }

    /**
     * 获取举报列表（管理员）
     */
    @GetMapping("/admin/list")
    public Result<Page<ReportVO>> getReportList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer targetType) {
        
        log.info(">>> [举报] 管理员查询举报列表: pageNum={}, pageSize={}, status={}, targetType={}", 
                 pageNum, pageSize, status, targetType);
        return reportService.getReportList(pageNum, pageSize, status, targetType);
    }

    /**
     * 处理举报（管理员）
     */
    @PostMapping("/admin/handle/{reportId}")
    public Result handleReport(@PathVariable Long reportId,
                                @RequestParam String handleResult) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long adminId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.ADMIN_ID));
        
        log.info(">>> [举报] 管理员 {} 处理举报 ID={}", adminId, reportId);
        return reportService.handleReport(reportId, handleResult, adminId);
    }

    /**
     * 驳回举报（管理员）
     */
    @PostMapping("/admin/reject/{reportId}")
    public Result rejectReport(@PathVariable Long reportId,
                                @RequestParam String handleResult) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long adminId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.ADMIN_ID));
        
        log.info(">>> [举报] 管理员 {} 驳回举报 ID={}", adminId, reportId);
        return reportService.rejectReport(reportId, handleResult, adminId);
    }
}
