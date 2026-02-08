package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.model.entity.Notification;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 通知消息服务接口
 *
 * @author system
 * @since 2026-01-26
 */
public interface INotificationService extends IService<Notification> {

    /**
     * 创建通知（批量）
     *
     * @param userIds 用户ID列表
     * @param title   通知标题
     * @param content 通知内容
     */
    void createNotifications(List<Long> userIds, String title, String content);

    /**
     * 查询通知历史（管理员，分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Map<String, Object> getNotificationHistory(Integer pageNum, Integer pageSize);

    /**
     * 查询用户通知列表
     *
     * @param userId 用户ID
     * @return 通知列表
     */
    List<Notification> getUserNotifications(Long userId);

    /**
     * 获取通知详情并标记已读
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 通知详情
     */
    Notification getNotificationDetail(Long notificationId, Long userId);

    /**
     * 标记已读
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记未读
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     */
    void markAsUnread(Long notificationId, Long userId);

    /**
     * 获取未读数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 验证通知所有权
     *
     * @param notificationId 通知ID
     * @param userId         用户ID
     * @return 是否属于该用户
     */
    boolean validateOwnership(Long notificationId, Long userId);

    /**
     * 创建通知（增强版，支持类型和优先级）
     *
     * @param userIds  用户ID列表
     * @param title    通知标题
     * @param content  通知内容
     * @param type     通知类型
     * @param priority 优先级
     * @param senderId 发送者ID
     */
    void createNotificationsEnhanced(List<Long> userIds, String title, String content, String type, String priority, Long senderId);

    /**
     * 批量标记已读
     *
     * @param ids    通知ID列表
     * @param userId 用户ID
     */
    void batchMarkAsRead(List<Long> ids, Long userId);

    /**
     * 批量删除通知
     *
     * @param ids    通知ID列表
     * @param userId 用户ID
     */
    void batchDelete(List<Long> ids, Long userId);

    /**
     * 根据条件查询通知列表
     *
     * @param userId   用户ID
     * @param type     通知类型
     * @param priority 优先级
     * @param isRead   是否已读
     * @param keyword  关键词
     * @return 通知列表
     */
    List<Notification> getNotificationsByConditions(Long userId, String type, String priority, Integer isRead, String keyword);

    /**
     * 获取通知统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getNotificationStats(Long userId);

    /**
     * 获取管理员发送统计
     *
     * @param senderId 发送者ID
     * @return 统计信息
     */
    Map<String, Object> getSenderStats(Long senderId);

    /**
     * 全部标记已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

}
