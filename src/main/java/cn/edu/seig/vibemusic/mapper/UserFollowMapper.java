package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.UserFollow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户关注Mapper接口
 *
 * @author system
 * @since 2026-03-10
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 检查用户是否已关注
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 关注记录数量
     */
    @Select("SELECT COUNT(*) FROM tb_user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    Integer checkFollowStatus(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}
