package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.constant.JwtClaimsConstant;
import cn.edu.seig.vibemusic.mapper.UserFollowMapper;
import cn.edu.seig.vibemusic.mapper.UserMapper;
import cn.edu.seig.vibemusic.model.entity.User;
import cn.edu.seig.vibemusic.model.entity.UserFollow;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.INotificationService;
import cn.edu.seig.vibemusic.service.IUserFollowService;
import cn.edu.seig.vibemusic.util.ThreadLocalUtil;
import cn.edu.seig.vibemusic.util.TypeConversionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户关注服务实现类
 *
 * @author system
 * @since 2026-03-10
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements IUserFollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private cn.edu.seig.vibemusic.service.IPointsService pointsService;

    @Autowired
    private cn.edu.seig.vibemusic.mapper.UserPrivilegeMapper userPrivilegeMapper;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        return TypeConversionUtil.toLong(userIdObj);
    }

    /**
     * 关注用户
     */
    @Override
    @Transactional
    public Result followUser(Long followingId) {
        Long followerId = getCurrentUserId();

        // 不能关注自己
        if (followerId.equals(followingId)) {
            return Result.error("不能关注自己");
        }

        // 检查被关注用户是否存在
        User followingUser = userMapper.selectById(followingId);
        if (followingUser == null) {
            return Result.error("用户不存在");
        }

        // 检查是否已关注
        Integer count = userFollowMapper.checkFollowStatus(followerId, followingId);
        if (count != null && count > 0) {
            return Result.error("已经关注过了");
        }

        // 创建关注记录
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFollowingId(followingId);
        userFollow.setCreateTime(LocalDateTime.now());

        if (userFollowMapper.insert(userFollow) == 0) {
            return Result.error("关注失败");
        }

        // 更新关注者的关注数
        User follower = userMapper.selectById(followerId);
        if (follower != null) {
            Integer followingCount = follower.getFollowingCount();
            follower.setFollowingCount(followingCount != null ? followingCount + 1 : 1);
            userMapper.updateById(follower);
        }

        // 更新被关注者的粉丝数
        Integer followerCount = followingUser.getFollowerCount();
        followingUser.setFollowerCount(followerCount != null ? followerCount + 1 : 1);
        userMapper.updateById(followingUser);

        // 给被关注者增加积分
        try {
            pointsService.addPoints(followingId, "FOLLOWED", followerId);
        } catch (Exception e) {
            System.err.println("增加关注积分失败: " + e.getMessage());
        }

        // 发送通知
        try {
            User currentUser = userMapper.selectById(followerId);
            String username = currentUser != null ? currentUser.getUsername() : "用户";
            String title = "新粉丝";
            String content = username + " 关注了你";

            notificationService.createNotificationsEnhanced(
                    List.of(followingId),
                    title,
                    content,
                    "PERSONAL",
                    "NORMAL",
                    followerId
            );
        } catch (Exception e) {
            System.err.println("发送关注通知失败: " + e.getMessage());
        }

        return Result.success("关注成功");
    }

    /**
     * 取消关注用户
     */
    @Override
    @Transactional
    public Result unfollowUser(Long followingId) {
        Long followerId = getCurrentUserId();

        // 查找关注记录
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        UserFollow userFollow = userFollowMapper.selectOne(wrapper);

        if (userFollow == null) {
            return Result.error("未关注过该用户");
        }

        // 删除关注记录
        if (userFollowMapper.deleteById(userFollow.getId()) == 0) {
            return Result.error("取消关注失败");
        }

        // 更新关注者的关注数
        User follower = userMapper.selectById(followerId);
        if (follower != null && follower.getFollowingCount() != null && follower.getFollowingCount() > 0) {
            follower.setFollowingCount(follower.getFollowingCount() - 1);
            userMapper.updateById(follower);
        }

        // 更新被关注者的粉丝数
        User followingUser = userMapper.selectById(followingId);
        if (followingUser != null && followingUser.getFollowerCount() != null && followingUser.getFollowerCount() > 0) {
            followingUser.setFollowerCount(followingUser.getFollowerCount() - 1);
            userMapper.updateById(followingUser);
        }

        return Result.success("取消关注成功");
    }

    /**
     * 检查是否已关注
     */
    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        Integer count = userFollowMapper.checkFollowStatus(followerId, followingId);
        return count != null && count > 0;
    }

    /**
     * 获取关注列表
     */
    @Override
    public Result getFollowingList(Long userId, Integer pageNum, Integer pageSize) {
        // 查询关注记录
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
                .orderByDesc(UserFollow::getCreateTime);

        Page<UserFollow> page = new Page<>(pageNum, pageSize);
        IPage<UserFollow> followPage = userFollowMapper.selectPage(page, wrapper);

        if (followPage.getRecords().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("records", List.of());
            result.put("total", 0);
            return Result.success(result);
        }

        // 获取被关注用户ID列表
        List<Long> followingIds = followPage.getRecords().stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toList());

        // 查询用户信息
        List<User> users = userMapper.selectBatchIds(followingIds);

        // 构建返回数据
        Long currentUserId = getCurrentUserId();
        List<Map<String, Object>> userList = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("username", user.getUsername());
            userMap.put("userAvatar", user.getUserAvatar());
            userMap.put("followerCount", user.getFollowerCount() != null ? user.getFollowerCount() : 0);
            userMap.put("followingCount", user.getFollowingCount() != null ? user.getFollowingCount() : 0);
            // 检查当前用户是否关注了该用户
            userMap.put("isFollowing", isFollowing(currentUserId, user.getUserId()));
            // 查询用户激活的主页装扮
            userMap.put("profileTheme", getActiveProfileTheme(user.getUserId()));
            return userMap;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", userList);
        result.put("total", followPage.getTotal());

        return Result.success(result);
    }

    /**
     * 获取粉丝列表
     */
    @Override
    public Result getFollowerList(Long userId, Integer pageNum, Integer pageSize) {
        // 查询粉丝记录
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowingId, userId)
                .orderByDesc(UserFollow::getCreateTime);

        Page<UserFollow> page = new Page<>(pageNum, pageSize);
        IPage<UserFollow> followPage = userFollowMapper.selectPage(page, wrapper);

        if (followPage.getRecords().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("records", List.of());
            result.put("total", 0);
            return Result.success(result);
        }

        // 获取粉丝用户ID列表
        List<Long> followerIds = followPage.getRecords().stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toList());

        // 查询用户信息
        List<User> users = userMapper.selectBatchIds(followerIds);

        // 构建返回数据
        Long currentUserId = getCurrentUserId();
        List<Map<String, Object>> userList = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("username", user.getUsername());
            userMap.put("userAvatar", user.getUserAvatar());
            userMap.put("followerCount", user.getFollowerCount() != null ? user.getFollowerCount() : 0);
            userMap.put("followingCount", user.getFollowingCount() != null ? user.getFollowingCount() : 0);
            // 检查当前用户是否关注了该用户
            userMap.put("isFollowing", isFollowing(currentUserId, user.getUserId()));
            // 查询用户激活的主页装扮
            userMap.put("profileTheme", getActiveProfileTheme(user.getUserId()));
            return userMap;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", userList);
        result.put("total", followPage.getTotal());

        return Result.success(result);
    }

    /**
     * 获取关注统计
     */
    @Override
    public Result<Map<String, Object>> getFollowStats(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("followingCount", user.getFollowingCount() != null ? user.getFollowingCount() : 0);
        stats.put("followerCount", user.getFollowerCount() != null ? user.getFollowerCount() : 0);

        // 如果是查询当前用户，返回是否关注
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(userId)) {
            stats.put("isFollowing", isFollowing(currentUserId, userId));
        }

        return Result.success(stats);
    }

    /**
     * 获取用户激活的主页装扮
     */
    private String getActiveProfileTheme(Long userId) {
        try {
            LambdaQueryWrapper<cn.edu.seig.vibemusic.model.entity.UserPrivilege> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(cn.edu.seig.vibemusic.model.entity.UserPrivilege::getUserId, userId)
                   .eq(cn.edu.seig.vibemusic.model.entity.UserPrivilege::getPrivilegeType, "PROFILE_THEME")
                   .eq(cn.edu.seig.vibemusic.model.entity.UserPrivilege::getIsActive, 1);
            cn.edu.seig.vibemusic.model.entity.UserPrivilege privilege = userPrivilegeMapper.selectOne(wrapper);
            if (privilege != null && (privilege.getExpireTime() == null || privilege.getExpireTime().isAfter(java.time.LocalDateTime.now()))) {
                return privilege.getPrivilegeValue();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
