package cn.edu.seig.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_song_request")
public class SongRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("song_name")
    private String songName;

    @TableField("artist_name")
    private String artistName;

    @TableField("album")
    private String album;

    @TableField("style")
    private String style;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("release_time")
    private LocalDate releaseTime;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("audio_url")
    private String audioUrl;

    @TableField("duration")
    private String duration;

    @TableField("license_desc")
    private String licenseDesc;

    @TableField("remark")
    private String remark;

    /** 0-待审核 1-已通过 2-已拒绝 */
    @TableField("status")
    private Integer status;

    @TableField("reject_reason")
    private String rejectReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
