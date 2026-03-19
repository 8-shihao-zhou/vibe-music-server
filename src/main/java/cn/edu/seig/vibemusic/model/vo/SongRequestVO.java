package cn.edu.seig.vibemusic.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SongRequestVO {
    private Long id;
    private Long userId;
    private String username;
    private String songName;
    private String artistName;
    private String album;
    private String style;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseTime;
    private String coverUrl;
    private String audioUrl;
    private String licenseDesc;
    private String remark;
    /** 0-待审核 1-已通过 2-已拒绝 */
    private Integer status;
    private String rejectReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
