package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.Report;
import cn.edu.seig.vibemusic.model.vo.ReportVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 举报Mapper接口
 *
 * @author system
 * @since 2026-03-11
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 分页查询举报列表（管理员用）
     *
     * @param page 分页对象
     * @param status 处理状态
     * @param targetType 目标类型
     * @return 举报列表
     */
    IPage<ReportVO> selectReportPage(Page<ReportVO> page, 
                                      @Param("status") Integer status,
                                      @Param("targetType") Integer targetType);
}
