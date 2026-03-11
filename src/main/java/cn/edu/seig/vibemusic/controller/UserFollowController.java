package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.IUserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户关注控制器
 *
 * @author system
 * @since 2026-03-10
 */
@RestController
@RequestMapping("/user/follow")
public class UserFollowController {

    @Autowired
    private IUserFollowService userFollowService;

    /**
     * 关注用户
     *
     * @param followingId 被关注者ID
     * @return 结果
     */
    @PostMapping("/{followingId}")
    public Result followUser(@PathVariable Long followingId) {
        return userFollowService.followUser(followingId);
    }

    /**
     * 取消关注用户
     *
     * @param followingId 被关注者ID
     * @return 结果
     */
    @DeleteMapping("/{followingId}")
    public Result unfollowUser(@PathVariable Long followingId) {
        return userFollowService.unfollowUser(followingId);
    }

    /**
     * 获取关注列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 关注列表
     */
    @GetMapping("/following/{userId}")
    public Result getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return userFollowService.getFollowingList(userId, pageNum, pageSize);
    }

    /**
     * 获取粉丝列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 粉丝列表
     */
    @GetMapping("/followers/{userId}")
    public Result getFollowerList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return userFollowService.getFollowerList(userId, pageNum, pageSize);
    }

    /**
     * 获取关注统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    @GetMapping("/stats/{userId}")
    public Result<Map<String, Object>> getFollowStats(@PathVariable Long userId) {
        return userFollowService.getFollowStats(userId);
    }
}
