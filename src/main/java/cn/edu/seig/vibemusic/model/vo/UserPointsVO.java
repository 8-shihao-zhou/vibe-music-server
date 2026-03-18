package cn.edu.seig.vibemusic.model.vo;

import lombok.Data;

@Data
public class UserPointsVO {
    private Long userId;
    private String username;
    private Integer totalPoints;
    private Integer availablePoints;
    private Integer level;
    private String levelName;
    private Integer nextLevelPoints;
    private Integer ranking;
}
