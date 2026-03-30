package cn.edu.seig.vibemusic.model.vo;

import lombok.Data;

@Data
public class MallItemVO {
    private Long id;
    private String itemCode;
    private String itemName;
    private String itemDescription;
    private Integer itemPrice;
    private String itemType;
    private String itemTypeName;
    private Integer durationDays;
    private String durationText;
    private Boolean canPurchase; // 是否可购买（积分是否足够）
    private Boolean alreadyOwned; // 是否已拥有
    private Boolean isActive; // 是否当前激活（仅装扮类商品）
}