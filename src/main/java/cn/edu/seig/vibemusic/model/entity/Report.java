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
 * 举报实体类
 *
 * @author system
 * @since 2026-03-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_report")
public class Report implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 举报ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 举报人ID
     */
    @TableField("reporter_id")
    private Long reporterId;

    /**
     * 举报目标类型: 1-帖子, 2-评论
     */
    @TableField("target_type")
    private Integer targetType;

    /**
     * 举报目标ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 举报原因类型
     */
    @TableField("reason_type")
    private String reasonType;

    /**
     * 举报详细说明
     */
    @TableField("reason_detail")
    private String reasonDetail;

    /**
     * 处理状态: 0-待处理, 1-已处理, 2-已驳回
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理结果说明
     */
    @TableField("handle_result")
    private String handleResult;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 举报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
