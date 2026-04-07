package cn.edu.seig.vibemusic.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端评论列表 VO
 */
@Data
public class CommentAdminVO {

    /**
     * 评论 ID
     */
    private Long commentId;

    /**
     * 评论用户 ID
     */
    private Long userId;

    /**
     * 评论用户名称
     */
    private String username;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 评论类型：0-歌曲评论，1-歌单评论
     */
    private Integer type;

    /**
     * 评论类型名称
     */
    private String typeName;

    /**
     * 目标内容 ID
     */
    private Long targetId;

    /**
     * 目标标题
     */
    private String targetTitle;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
