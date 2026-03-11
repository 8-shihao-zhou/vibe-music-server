package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.model.entity.UserFollow;
import cn.edu.seig.vibemusic.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 用户关注服务接口
 *
 * @author system
 * @since 2026-03-10
 */
public interface IUserFollowService extends IService<UserFollow> {

    /**
     * 关注用户
     *
     * @param followingId 被关注者ID
     * @return 结果
     */
    Result followUser(Long followingId);

    /**
     * 取消关注用户
     *
     * @param followingId 被关注者ID
     * @return 结果
     */
    Result unfollowUser(Long followingId);

    /**
     * 检查是否已关注
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取关注列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 关注列表
     */
    Result getFollowingList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取粉丝列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 粉丝列表
     */
    Result getFollowerList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取关注统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Result<Map<String, Object>> getFollowStats(Long userId);
}
