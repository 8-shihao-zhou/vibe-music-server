package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.PostLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子点赞 Mapper 接口
 *
 * @author sunpingli
 * @since 2026-02-08
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

    /**
     * 检查用户是否已点赞
     */
    Integer checkUserLike(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 获取帖子点赞数
     */
    Integer getPostLikeCount(@Param("postId") Long postId);
}
