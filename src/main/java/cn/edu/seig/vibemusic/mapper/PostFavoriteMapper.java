package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.PostFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 帖子收藏Mapper接口
 *
 * @author sunpingli
 * @since 2026-03-04
 */
@Mapper
public interface PostFavoriteMapper extends BaseMapper<PostFavorite> {

    /**
     * 检查用户是否收藏了帖子
     */
    @Select("SELECT COUNT(*) FROM tb_post_favorite WHERE post_id = #{postId} AND user_id = #{userId}")
    Integer checkUserFavorite(Long postId, Long userId);
}
