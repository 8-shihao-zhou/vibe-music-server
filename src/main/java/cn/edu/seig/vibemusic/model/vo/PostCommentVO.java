package cn.edu.seig.vibemusic.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子评论VO
 *
 * @author sunpingli
 * @since 2026-02-08
 */
@Data
public class PostCommentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 点赞数量
     */
    private Long likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 评论时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 回复列表
     */
    private List<PostCommentVO> replies;
}
