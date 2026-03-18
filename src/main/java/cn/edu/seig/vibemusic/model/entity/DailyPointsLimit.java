package cn.edu.seig.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_daily_points_limit")
public class DailyPointsLimit {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String actionType;
    
    private Integer pointsEarned;
    
    private Integer actionCount;
    
    private LocalDate date;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
