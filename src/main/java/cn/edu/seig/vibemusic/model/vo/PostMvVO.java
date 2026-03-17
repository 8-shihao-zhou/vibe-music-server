package cn.edu.seig.vibemusic.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 帖子MV信息VO
 *
 * @author system
 * @since 2026-03-16
 */
@Data
public class PostMvVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * MV ID
     */
    private Long mvId;

    /**
     * MV名称
     */
    private String mvName;

    /**
     * MV URL
     */
    private String mvUrl;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 时长(秒)
     */
    private Integer duration;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 描述
     */
    private String description;
}
