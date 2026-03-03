package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.CommentLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论点赞 Mapper 接口
 *
 * @author sunpingli
 * @since 2026-02-08
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    /**
     * 检查用户是否已点赞评论
     */
    Integer checkUserLike(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
