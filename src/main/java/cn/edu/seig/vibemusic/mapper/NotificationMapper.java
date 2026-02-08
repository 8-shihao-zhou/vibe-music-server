package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.Notification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知消息 Mapper 接口
 *
 * @author system
 * @since 2026-01-26
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 批量插入通知
     *
     * @param notifications 通知列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("notifications") List<Notification> notifications);

    /**
     * 根据用户ID查询通知列表（按创建时间倒序）
     *
     * @param userId 用户ID
     * @return 通知列表
     */
    List<Notification> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询所有通知（分页，按创建时间倒序）
     *
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 通知列表
     */
    List<Notification> selectAll(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计通知总数
     *
     * @return 总数
     */
    Long countAll();

    /**
     * 更新通知已读状态
     *
     * @param id       通知ID
     * @param isRead   是否已读
     * @param readTime 阅读时间
     * @return 更新的记录数
     */
    int updateReadStatus(@Param("id") Long id, @Param("isRead") Integer isRead, @Param("readTime") LocalDateTime readTime);

    /**
     * 统计用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Integer countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 验证通知所有权（检查通知是否属于指定用户）
     *
     * @param id     通知ID
     * @param userId 用户ID
     * @return 匹配的记录数（0或1）
     */
    Integer countByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 批量标记已读
     *
     * @param ids      通知ID列表
     * @param userId   用户ID
     * @param readTime 阅读时间
     * @return 更新的记录数
     */
    int batchMarkAsRead(@Param("ids") List<Long> ids, @Param("userId") Long userId, @Param("readTime") LocalDateTime readTime);

    /**
     * 批量删除通知（软删除）
     *
     * @param ids    通知ID列表
     * @param userId 用户ID
     * @return 更新的记录数
     */
    int batchDelete(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /**
     * 根据条件查询通知列表（支持筛选）
     *
     * @param userId   用户ID
     * @param type     通知类型
     * @param priority 优先级
     * @param isRead   是否已读
     * @param keyword  关键词（搜索标题）
     * @return 通知列表
     */
    List<Notification> selectByConditions(@Param("userId") Long userId,
                                          @Param("type") String type,
                                          @Param("priority") String priority,
                                          @Param("isRead") Integer isRead,
                                          @Param("keyword") String keyword);

    /**
     * 统计各类型通知数量
     *
     * @param userId 用户ID
     * @return 统计结果
     */
    List<java.util.Map<String, Object>> countByType(@Param("userId") Long userId);

    /**
     * 统计管理员发送的通知数量
     *
     * @param senderId 发送者ID
     * @return 统计结果
     */
    java.util.Map<String, Object> countBySender(@Param("senderId") Long senderId);

}
