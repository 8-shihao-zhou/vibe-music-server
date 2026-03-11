package cn.edu.seig.vibemusic.model.dto;

import lombok.Data;

/**
 * 举报DTO
 *
 * @author system
 * @since 2026-03-11
 */
@Data
public class ReportDTO {
    /**
     * 举报目标类型: 1-帖子, 2-评论
     */
    private Integer targetType;

    /**
     * 举报目标ID
     */
    private Long targetId;

    /**
     * 举报原因类型
     */
    private String reasonType;

    /**
     * 举报详细说明
     */
    private String reasonDetail;
}
