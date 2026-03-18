package cn.edu.seig.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user_privilege")
public class UserPrivilege {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String privilegeType;
    
    private String privilegeValue;
    
    private LocalDateTime expireTime;
    
    private Integer isActive;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}