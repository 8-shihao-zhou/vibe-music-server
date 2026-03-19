package cn.edu.seig.vibemusic.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SongRequestDTO {

    @NotBlank(message = "歌曲名称不能为空")
    private String songName;

    @NotBlank(message = "歌手名称不能为空")
    private String artistName;

    private String album;

    private String style;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseTime;

    private String coverUrl;

    @NotBlank(message = "音频链接不能为空")
    private String audioUrl;

    private String duration;

    @NotBlank(message = "版权说明不能为空")
    private String licenseDesc;

    private String remark;
}
