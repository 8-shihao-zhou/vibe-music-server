package cn.edu.seig.vibemusic.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_mall_item")
public class MallItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String itemCode;
    
    private String itemName;
    
    private String itemDescription;
    
    private Integer itemPrice;
    
    private String itemType;
    
    private Integer durationDays;
    
    private Integer itemStatus;
    
    private Integer sortOrder;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}