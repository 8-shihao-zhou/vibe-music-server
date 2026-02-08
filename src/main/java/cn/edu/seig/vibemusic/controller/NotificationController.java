package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.model.entity.Notification;
import cn.edu.seig.vibemusic.model.entity.User;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.INotificationService;
import cn.edu.seig.vibemusic.service.IUserService;
import cn.edu.seig.vibemusic.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知消息控制器
 *
 * @author system
 * @since 2026-01-26
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private IUserService userService;

    // ==================== 管理员接口 ====================

    /**
     * 创建通知（管理员）
     *
     * @param requestBody 请求体，包含 userIds, title, content
     * @return 结果
     */
    @PostMapping("/admin/create")
    public Result<String> createNotification(@RequestBody Map<String, Object> requestBody) {
        try {
            // 处理userIds，可能是Integer或Long类型
            @SuppressWarnings("unchecked")
            List<Object> userIdsObj = (List<Object>) requestBody.get("userIds");
            List<Long> userIds = new ArrayList<>();
            for (Object id : userIdsObj) {
                if (id instanceof Integer) {
                    userIds.add(((Integer) id).longValue());
                } else if (id instanceof Long) {
                    userIds.add((Long) id);
                } else if (id instanceof String) {
                    userIds.add(Long.parseLong((String) id));
                }
            }
            
            String title = (String) requestBody.get("title");
            String content = (String) requestBody.get("content");
            String type = (String) requestBody.get("type");
            String priority = (String) requestBody.get("priority");

            // 获取当前管理员ID（从UserContext或其他方式）
            Long senderId = UserContext.getUserId();

            // 使用增强版创建方法
            notificationService.createNotificationsEnhanced(userIds, title, content, type, priority, senderId);
            return Result.success("通知发送成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("通知发送失败：" + e.getMessage());
        }
    }

    /**
     * 查询通知历史（管理员，分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 结果
     */
    @GetMapping("/admin/history")
    public Result<Map<String, Object>> getNotificationHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = notificationService.getNotificationHistory(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有用户列表（管理员）
     *
     * @return 结果
     */
    @GetMapping("/admin/users")
    public Result<List<Map<String, Object>>> getAllUsers() {
        try {
            System.out.println("=== 开始查询用户列表 ===");
            
            // 查询所有启用状态的用户
            List<User> users = userService.list();
            System.out.println("查询到用户数量: " + (users != null ? users.size() : 0));
            
            if (users == null || users.isEmpty()) {
                System.out.println("警告：数据库中没有用户数据");
                return Result.success(new ArrayList<>());
            }
            
            // 转换为简化的用户信息
            List<Map<String, Object>> userList = users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                // 注意：User实体的主键字段是userId，对应数据库的id
                userMap.put("userId", user.getUserId());
                userMap.put("username", user.getUsername() != null ? user.getUsername() : "");
                userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
                userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
                userMap.put("createTime", user.getCreateTime() != null ? user.getCreateTime().toString() : "");
                // userStatus是枚举类型，需要转换为前端期望的格式
                // ENABLE(0) -> "启用", DISABLE(1) -> "禁用"
                if (user.getUserStatus() != null) {
                    String statusText = user.getUserStatus().getUserStatus(); // "启用" 或 "禁用"
                    userMap.put("userStatus", statusText);
                    userMap.put("userStatusValue", user.getUserStatus().getId()); // 0 或 1
                } else {
                    userMap.put("userStatus", "启用");
                    userMap.put("userStatusValue", 0);
                }
                return userMap;
            }).toList();
            
            System.out.println("=== 用户列表查询成功，返回 " + userList.size() + " 条数据 ===");
            
            // 打印第一个用户的数据作为示例
            if (!userList.isEmpty()) {
                System.out.println("示例用户数据: " + userList.get(0));
            }
            
            Result<List<Map<String, Object>>> result = Result.success(userList);
            System.out.println("返回的Result对象: code=" + result.getCode() + ", message=" + result.getMessage() + ", data size=" + (result.getData() != null ? result.getData().size() : 0));
            
            return result;
        } catch (Exception e) {
            System.err.println("=== 查询用户列表失败 ===");
            e.printStackTrace();
            return Result.error("查询用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 删除通知（管理员）
     *
     * @param id 通知ID
     * @return 结果
     */
    @DeleteMapping("/admin/delete/{id}")
    public Result<String> deleteNotification(@PathVariable("id") Long id) {
        try {
            boolean success = notificationService.removeById(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    // ==================== 用户接口 ====================

    /**
     * 获取用户通知列表
     *
     * @return 结果
     */
    @GetMapping("/user/list")
    public Result<List<Notification>> getUserNotifications() {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return Result.success(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取通知详情
     *
     * @param id 通知ID
     * @return 结果
     */
    @GetMapping("/user/detail/{id}")
    public Result<Notification> getNotificationDetail(@PathVariable Long id) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            Notification notification = notificationService.getNotificationDetail(id, userId);
            return Result.success(notification);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("无权访问")) {
                return Result.error("无权访问该通知");
            } else if (e.getMessage().contains("不存在")) {
                return Result.error("通知不存在");
            }
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 标记已读
     *
     * @param id 通知ID
     * @return 结果
     */
    @PutMapping("/user/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            notificationService.markAsRead(id, userId);
            return Result.success("标记成功");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("无权操作")) {
                return Result.error("无权操作该通知");
            }
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 标记未读
     *
     * @param id 通知ID
     * @return 结果
     */
    @PutMapping("/user/unread/{id}")
    public Result<String> markAsUnread(@PathVariable Long id) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            notificationService.markAsUnread(id, userId);
            return Result.success("标记成功");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("无权操作")) {
                return Result.error("无权操作该通知");
            }
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 获取未读消息数量
     *
     * @return 结果
     */
    @GetMapping("/user/unread-count")
    public Result<Map<String, Integer>> getUnreadCount() {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            Integer count = notificationService.getUnreadCount(userId);
            Map<String, Integer> result = new HashMap<>();
            result.put("count", count);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 批量标记已读
     *
     * @param requestBody 请求体，包含 ids
     * @return 结果
     */
    @PutMapping("/user/batch-read")
    public Result<String> batchMarkAsRead(@RequestBody Map<String, Object> requestBody) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) requestBody.get("ids");
            notificationService.batchMarkAsRead(ids, userId);
            return Result.success("批量标记成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除通知
     *
     * @param requestBody 请求体，包含 ids
     * @return 结果
     */
    @DeleteMapping("/user/batch-delete")
    public Result<String> batchDelete(@RequestBody Map<String, Object> requestBody) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) requestBody.get("ids");
            notificationService.batchDelete(ids, userId);
            return Result.success("批量删除成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 根据条件筛选通知
     *
     * @param type     通知类型
     * @param priority 优先级
     * @param isRead   是否已读
     * @param keyword  关键词
     * @return 结果
     */
    @GetMapping("/user/filter")
    public Result<List<Notification>> filterNotifications(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(required = false) String keyword) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            List<Notification> notifications = notificationService.getNotificationsByConditions(userId, type, priority, isRead, keyword);
            return Result.success(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取通知统计信息
     *
     * @return 结果
     */
    @GetMapping("/user/stats")
    public Result<Map<String, Object>> getNotificationStats() {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            Map<String, Object> stats = notificationService.getNotificationStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 全部标记已读
     *
     * @return 结果
     */
    @PutMapping("/user/read-all")
    public Result<String> markAllAsRead() {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }

            notificationService.markAllAsRead(userId);
            return Result.success("全部标记成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 获取管理员发送统计（管理员）
     *
     * @return 结果
     */
    @GetMapping("/admin/stats")
    public Result<Map<String, Object>> getSenderStats() {
        try {
            Long senderId = UserContext.getUserId();
            if (senderId == null) {
                return Result.error("用户未登录");
            }

            Map<String, Object> stats = notificationService.getSenderStats(senderId);
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

}
