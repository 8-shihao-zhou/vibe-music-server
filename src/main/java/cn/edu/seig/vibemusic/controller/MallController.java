package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.model.entity.MallItem;
import cn.edu.seig.vibemusic.model.entity.UserPurchase;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.IMallService;
import cn.edu.seig.vibemusic.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分商城控制器
 */
@RestController
@RequestMapping("/api/mall")
public class MallController {

    @Autowired
    private IMallService mallService;

    /**
     * 获取商城商品列表
     */
    @GetMapping("/items")
    public Result getItems(@RequestParam(required = false) String type) {
        try {
            // 使用 getMallItems 方法获取完整的商品信息
            Result result = mallService.getMallItems();
            
            // 如果指定了类型，需要过滤结果
            if (type != null && !type.isEmpty() && result.getCode() == 0) {
                List<?> allItems = (List<?>) result.getData();
                // 这里可以添加类型过滤逻辑，但目前先返回所有商品
            }
            
            return result;
        } catch (Exception e) {
            return Result.error("获取商品列表失败");
        }
    }

    /**
     * 购买商品
     */
    @PostMapping("/purchase")
    public Result<String> purchaseItem(@RequestParam Long itemId, 
                                     @RequestParam(required = false) Long targetId) {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = mallService.purchaseItem(userId, itemId, targetId);
            if (success) {
                return Result.success("购买成功");
            } else {
                return Result.error("购买失败，积分不足或商品不存在");
            }
        } catch (Exception e) {
            return Result.error("购买失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户购买记录
     */
    @GetMapping("/purchases")
    public Result<List<UserPurchase>> getUserPurchases() {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            List<UserPurchase> purchases = mallService.getUserPurchases(userId);
            return Result.success(purchases);
        } catch (Exception e) {
            return Result.error("获取购买记录失败");
        }
    }

    /**
     * 检查用户是否拥有某个特权
     */
    @GetMapping("/privilege/check")
    public Result<Boolean> checkPrivilege(@RequestParam String privilegeType, 
                                        @RequestParam(required = false) Long targetId) {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean hasPrivilege = mallService.hasPrivilege(userId, privilegeType, targetId);
            return Result.success(hasPrivilege);
        } catch (Exception e) {
            return Result.error("检查特权失败");
        }
    }
}