package cn.edu.seig.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user_purchase")
public class UserPurchase {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long itemId;
    
    private String itemCode;
    
    private String itemName;
    
    private Integer pointsCost;
    
    private Long targetId;
    
    private LocalDateTime expireTime;
    
    private LocalDateTime purchaseTime;
    
    private Integer status;
}