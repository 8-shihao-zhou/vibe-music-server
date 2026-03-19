package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.model.entity.MallItem;
import cn.edu.seig.vibemusic.model.entity.UserPurchase;
import cn.edu.seig.vibemusic.result.Result;

import java.util.List;

public interface IMallService {
    /**
     * 获取可用商品列表
     */
    List<MallItem> getAvailableItems(String type);

    /**
     * 购买商品
     */
    boolean purchaseItem(Long userId, Long itemId, Long targetId);

    /**
     * 获取用户购买记录
     */
    List<UserPurchase> getUserPurchases(Long userId);

    /**
     * 检查用户是否拥有特权
     */
    boolean hasPrivilege(Long userId, String privilegeType, Long targetId);

    /**
     * 获取商城商品列表（返回Result）
     */
    Result getMallItems();

    /**
     * 切换装扮
     */
    boolean togglePrivilege(Long userId, String type, String value);
}