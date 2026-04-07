package cn.edu.seig.vibemusic.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建 AI MV 任务请求
 */
@Data
public class AiVideoTaskCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌曲名称
     */
    private String songName;

    /**
     * 歌手名称
     */
    private String artistName;

    /**
     * 曲库音频地址
     */
    private String audioUrl;

    /**
     * MV 生成风格编码
     */
    private String styleCode;

    /**
     * MV 生成风格名称
     */
    private String styleLabel;
}
