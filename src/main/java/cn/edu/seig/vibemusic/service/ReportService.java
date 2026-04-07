package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.model.dto.ReportDTO;
import cn.edu.seig.vibemusic.model.vo.ReportVO;
import cn.edu.seig.vibemusic.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 举报服务接口
 *
 * @author system
 * @since 2026-03-11
 */
public interface ReportService {

    /**
     * 提交举报
     *
     * @param reportDTO 举报信息
     * @param reporterId 举报人ID
     * @return 结果
     */
    Result submitReport(ReportDTO reportDTO, Long reporterId);

    /**
     * 检查用户是否已举报过该目标
     *
     * @param reporterId 举报人ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 是否已举报
     */
    boolean hasReported(Long reporterId, Integer targetType, Long targetId);

    /**
     * 分页查询举报列表（管理员）
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param status 处理状态
     * @param targetType 目标类型
     * @return 举报列表
     */
    Result<Page<ReportVO>> getReportList(Integer pageNum, Integer pageSize, 
                                          Integer status, Integer targetType);

    /**
     * 处理举报
     *
     * @param reportId 举报ID
     * @param handleResult 处理结果
     * @param handlerId 处理人ID
     * @return 结果
     */
    Result handleReport(Long reportId, String handleResult, Long handlerId);

    /**
     * 驳回举报
     *
     * @param reportId 举报ID
     * @param handleResult 驳回原因
     * @param handlerId 处理人ID
     * @return 结果
     */
    Result rejectReport(Long reportId, String handleResult, Long handlerId);

    /**
     * 重置举报相关数据
     *
     * @param adminId 管理员ID
     * @return 结果
     */
    Result resetReportData(Long adminId);
}
