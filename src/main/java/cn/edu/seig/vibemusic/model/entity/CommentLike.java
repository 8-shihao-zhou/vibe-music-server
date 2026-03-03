package cn.edu.seig.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论点赞实体类
 *
 * @author sunpingli
 * @since 2026-02-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_comment_like")
public class CommentLike implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 点赞ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论ID
     */
    @TableField("comment_id")
    private Long commentId;

    /**
     * 点赞用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 点赞时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
