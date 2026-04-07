package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.Comment;
import cn.edu.seig.vibemusic.model.vo.CommentAdminVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 管理端分页查询评论
     *
     * @param page 分页参数
     * @param keyword 关键词
     * @param type 评论类型
     * @return 评论分页结果
     */
    IPage<CommentAdminVO> selectAdminCommentPage(Page<CommentAdminVO> page,
                                                 @Param("keyword") String keyword,
                                                 @Param("type") Integer type);
}
