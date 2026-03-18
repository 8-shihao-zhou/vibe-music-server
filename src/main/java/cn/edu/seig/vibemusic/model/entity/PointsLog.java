package cn.edu.seig.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_points_log")
public class PointsLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Integer points;
    
    private String actionType;
    
    private String description;
    
    private Long relatedId;
    
    private LocalDateTime createTime;
}
