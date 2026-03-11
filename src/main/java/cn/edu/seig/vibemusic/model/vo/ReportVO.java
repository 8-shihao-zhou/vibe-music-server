package cn.edu.seig.vibemusic.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报VO
 *
 * @author system
 * @since 2026-03-11
 */
@Data
public class ReportVO {
    /**
     * 举报ID
     */
    private Long id;

    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 举报人用户名
     */
    private String reporterName;

    /**
     * 举报人头像
     */
    private String reporterAvatar;

    /**
     * 举报目标类型: 1-帖子, 2-评论
     */
    private Integer targetType;

    /**
     * 举报目标ID
     */
    private Long targetId;

    /**
     * 目标标题/内容预览
     */
    private String targetContent;

    /**
     * 目标作者ID
     */
    private Long targetAuthorId;

    /**
     * 目标作者用户名
     */
    private String targetAuthorName;

    /**
     * 举报原因类型
     */
    private String reasonType;

    /**
     * 举报详细说明
     */
    private String reasonDetail;

    /**
     * 处理状态: 0-待处理, 1-已处理, 2-已驳回
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人用户名
     */
    private String handlerName;

    /**
     * 处理结果说明
     */
    private String handleResult;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    /**
     * 举报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 举报次数（该目标被举报的总次数）
     */
    private Integer reportCount;
}
