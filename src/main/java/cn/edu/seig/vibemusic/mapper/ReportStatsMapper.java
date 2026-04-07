package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.ReportStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 举报统计Mapper接口
 *
 * @author system
 * @since 2026-03-11
 */
@Mapper
public interface ReportStatsMapper extends BaseMapper<ReportStats> {

    /**
     * 清空全部举报统计
     *
     * @return 影响行数
     */
    @Delete("DELETE FROM tb_report_stats")
    int deleteAllReportStats();
}
