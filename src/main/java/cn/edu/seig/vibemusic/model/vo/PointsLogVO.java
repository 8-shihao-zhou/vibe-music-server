package cn.edu.seig.vibemusic.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointsLogVO {
    private Long id;
    private Integer points;
    private String actionType;
    private String description;
    private Long relatedId;
    private LocalDateTime createTime;
    private String changeType; // "EARN" 或 "SPEND"
}
