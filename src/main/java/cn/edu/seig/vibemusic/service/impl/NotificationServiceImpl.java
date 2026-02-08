package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.mapper.NotificationMapper;
import cn.edu.seig.vibemusic.model.entity.Notification;
import cn.edu.seig.vibemusic.service.INotificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知消息服务实现类
 *
 * @author system
 * @since 2026-01-26
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 创建通知（批量）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotifications(List<Long> userIds, String title, String content) {
        // 输入验证
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("通知标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("用户列表不能为空");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("通知标题长度不能超过200个字符");
        }
        if (content.length() > 5000) {
            throw new IllegalArgumentException("通知内容长度不能超过5000个字符");
        }

        // 创建通知列表
        List<Notification> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Long userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title.trim());
            notification.setContent(content.trim());
            notification.setIsRead(0);  // 初始状态为未读
            notification.setCreateTime(now);
            notifications.add(notification);
        }

        // 批量插入
        notificationMapper.batchInsert(notifications);
    }

    /**
     * 查询通知历史（管理员，分页）
     */
    @Override
    public Map<String, Object> getNotificationHistory(Integer pageNum, Integer pageSize) {
        // 参数验证
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询数据
        List<Notification> records = notificationMapper.selectAll(offset, pageSize);
        Long total = notificationMapper.countAll();

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    /**
     * 查询用户通知列表
     */
    @Override
    public List<Notification> getUserNotifications(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return notificationMapper.selectByUserId(userId);
    }

    /**
     * 获取通知详情并标记已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notification getNotificationDetail(Long notificationId, Long userId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("通知ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 验证所有权
        if (!validateOwnership(notificationId, userId)) {
            throw new RuntimeException("无权访问该通知");
        }

        // 查询通知
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }

        // 如果是未读状态，自动标记为已读
        if (notification.getIsRead() == 0) {
            markAsRead(notificationId, userId);
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
        }

        return notification;
    }

    /**
     * 标记已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId, Long userId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("通知ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 验证所有权
        if (!validateOwnership(notificationId, userId)) {
            throw new RuntimeException("无权操作该通知");
        }

        // 更新状态
        notificationMapper.updateReadStatus(notificationId, 1, LocalDateTime.now());
    }

    /**
     * 标记未读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsUnread(Long notificationId, Long userId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("通知ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 验证所有权
        if (!validateOwnership(notificationId, userId)) {
            throw new RuntimeException("无权操作该通知");
        }

        // 更新状态
        notificationMapper.updateReadStatus(notificationId, 0, null);
    }

    /**
     * 获取未读数量
     */
    @Override
    public Integer getUnreadCount(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return notificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 验证通知所有权
     */
    @Override
    public boolean validateOwnership(Long notificationId, Long userId) {
        if (notificationId == null || userId == null) {
            return false;
        }
        Integer count = notificationMapper.countByIdAndUserId(notificationId, userId);
        return count != null && count > 0;
    }

    /**
     * 创建通知（增强版，支持类型和优先级）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotificationsEnhanced(List<Long> userIds, String title, String content, String type, String priority, Long senderId) {
        // 输入验证
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("通知标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("用户列表不能为空");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("通知标题长度不能超过200个字符");
        }
        if (content.length() > 5000) {
            throw new IllegalArgumentException("通知内容长度不能超过5000个字符");
        }

        // 设置默认值
        if (type == null || type.trim().isEmpty()) {
            type = "SYSTEM";
        }
        if (priority == null || priority.trim().isEmpty()) {
            priority = "NORMAL";
        }

        // 创建通知列表
        List<Notification> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Long userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title.trim());
            notification.setContent(content.trim());
            notification.setType(type);
            notification.setPriority(priority);
            notification.setIsRead(0);
            notification.setIsDeleted(0);
            notification.setCreateTime(now);
            notification.setSenderId(senderId);
            notifications.add(notification);
        }

        // 批量插入
        notificationMapper.batchInsert(notifications);
    }

    /**
     * 批量标记已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkAsRead(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("通知ID列表不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        notificationMapper.batchMarkAsRead(ids, userId, LocalDateTime.now());
    }

    /**
     * 批量删除通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("通知ID列表不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        notificationMapper.batchDelete(ids, userId);
    }

    /**
     * 根据条件查询通知列表
     */
    @Override
    public List<Notification> getNotificationsByConditions(Long userId, String type, String priority, Integer isRead, String keyword) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        return notificationMapper.selectByConditions(userId, type, priority, isRead, keyword);
    }

    /**
     * 获取通知统计信息
     */
    @Override
    public Map<String, Object> getNotificationStats(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        Map<String, Object> stats = new HashMap<>();

        // 总数
        List<Notification> allNotifications = notificationMapper.selectByUserId(userId);
        stats.put("total", allNotifications.size());

        // 未读数量
        Integer unreadCount = notificationMapper.countUnreadByUserId(userId);
        stats.put("unread", unreadCount);

        // 已读数量
        stats.put("read", allNotifications.size() - unreadCount);

        // 按类型统计
        List<Map<String, Object>> typeStats = notificationMapper.countByType(userId);
        stats.put("byType", typeStats);

        return stats;
    }

    /**
     * 获取管理员发送统计
     */
    @Override
    public Map<String, Object> getSenderStats(Long senderId) {
        if (senderId == null) {
            throw new IllegalArgumentException("发送者ID不能为空");
        }

        return notificationMapper.countBySender(senderId);
    }

    /**
     * 全部标记已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 获取所有未读通知
        List<Notification> unreadNotifications = notificationMapper.selectByConditions(userId, null, null, 0, null);
        if (!unreadNotifications.isEmpty()) {
            List<Long> ids = unreadNotifications.stream().map(Notification::getId).toList();
            notificationMapper.batchMarkAsRead(ids, userId, LocalDateTime.now());
        }
    }

}
