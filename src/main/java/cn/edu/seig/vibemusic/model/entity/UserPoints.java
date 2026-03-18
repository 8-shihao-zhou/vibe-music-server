package cn.edu.seig.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user_points")
public class UserPoints {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Integer totalPoints;
    
    private Integer availablePoints;
    
    private Integer level;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
