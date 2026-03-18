package cn.edu.seig.vibemusic.model.dto;

import lombok.Data;

@Data
public class PurchaseDTO {
    private Long itemId;
    private Long targetId; // 目标ID（如帖子ID，用于置顶、高亮功能）
}