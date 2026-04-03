package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.enums.MallItemType;
import cn.edu.seig.vibemusic.mapper.MallItemMapper;
import cn.edu.seig.vibemusic.mapper.UserPrivilegeMapper;
import cn.edu.seig.vibemusic.mapper.UserPurchaseMapper;
import cn.edu.seig.vibemusic.model.entity.MallItem;
import cn.edu.seig.vibemusic.model.entity.UserPrivilege;
import cn.edu.seig.vibemusic.model.entity.UserPurchase;
import cn.edu.seig.vibemusic.model.vo.MallItemVO;
import cn.edu.seig.vibemusic.model.vo.UserPointsVO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.IMallService;
import cn.edu.seig.vibemusic.service.IPointsService;
import cn.edu.seig.vibemusic.utils.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MallServiceImpl implements IMallService {

    @Autowired
    private MallItemMapper mallItemMapper;

    @Autowired
    private UserPurchaseMapper userPurchaseMapper;

    @Autowired
    private UserPrivilegeMapper userPrivilegeMapper;

    @Autowired
    private IPointsService pointsService;

    @Override
    public Result getMallItems() {
        try {
            log.info("开始获取商城商品列表");
            
            Long userId = UserContext.getUserId();
            log.info("当前用户ID: {}", userId);
            
            // 获取所有上架商品
            LambdaQueryWrapper<MallItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MallItem::getItemStatus, 1)
                   .orderByAsc(MallItem::getSortOrder);
            List<MallItem> items = mallItemMapper.selectList(wrapper);
            log.info("查询到商品数量: {}", items.size());

            // 如果没有商品数据，返回空列表而不是错误
            if (items.isEmpty()) {
                log.warn("数据库中没有商品数据");
                return Result.success(new ArrayList<>());
            }

            // 获取用户积分信息
            Integer userPoints = 0;
            if (userId != null) {
                try {
                    Result pointsResult = pointsService.getUserPoints(userId);
                    log.info("积分查询结果: code={}, data={}", pointsResult.getCode(), pointsResult.getData());
                    if (pointsResult.getCode() == 0 && pointsResult.getData() != null) {
                        // 修复类型转换问题：直接使用UserPointsVO
                        UserPointsVO pointsVO = (UserPointsVO) pointsResult.getData();
                        userPoints = pointsVO.getAvailablePoints();
                    }
                } catch (Exception e) {
                    log.warn("获取用户积分失败: {}", e.getMessage());
                }
            }
            log.info("用户积分: {}", userPoints);

            // 获取用户已购买的特权 (具体到某个具体的 privilegeValue)
            Map<String, Boolean> ownedPrivileges = new HashMap<>();
            Map<String, Boolean> activePrivileges = new HashMap<>();
            if (userId != null) {
                try {
                    LambdaQueryWrapper<UserPrivilege> privilegeWrapper = new LambdaQueryWrapper<>();
                    privilegeWrapper.eq(UserPrivilege::getUserId, userId);
                    List<UserPrivilege> privileges = userPrivilegeMapper.selectList(privilegeWrapper);
                    log.info("查询到用户特权数量: {}", privileges.size());
                    
                    LocalDateTime now = LocalDateTime.now();
                    for (UserPrivilege privilege : privileges) {
                        if (privilege.getExpireTime() != null && privilege.getExpireTime().isBefore(now)) {
                            continue;
                        }
                        // 使用 privilegeType + privilegeValue 作为 key，这样同一类型的不同颜色/头像框可以区分开
                        String key = privilege.getPrivilegeType() + ":" + privilege.getPrivilegeValue();
                        ownedPrivileges.put(key, true);
                        if (Integer.valueOf(1).equals(privilege.getIsActive())) {
                            activePrivileges.put(key, true);
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取用户特权失败: {}", e.getMessage());
                }

                try {
                    LambdaQueryWrapper<UserPurchase> purchaseWrapper = new LambdaQueryWrapper<>();
                    purchaseWrapper.eq(UserPurchase::getUserId, userId)
                            .eq(UserPurchase::getStatus, 1)
                            .and(w -> w.likeRight(UserPurchase::getItemCode, "PROFILE_THEME")
                                    .or()
                                    .likeRight(UserPurchase::getItemCode, "POST_THEME"));
                    List<UserPurchase> decorationPurchases = userPurchaseMapper.selectList(purchaseWrapper);
                    LocalDateTime now = LocalDateTime.now();
                    for (UserPurchase purchase : decorationPurchases) {
                        if (purchase.getExpireTime() != null && purchase.getExpireTime().isBefore(now)) {
                            continue;
                        }
                        String type = purchase.getItemCode() != null && purchase.getItemCode().startsWith("POST_THEME")
                                ? "POST_THEME" : "PROFILE_THEME";
                        String value = getPrivilegeValueByItemCode(type, purchase.getItemCode());
                        if (value == null || value.isEmpty()) {
                            continue;
                        }
                        ownedPrivileges.put(type + ":" + value, true);
                    }
                } catch (Exception e) {
                    log.warn("获取用户装扮购买记录失败: {}", e.getMessage());
                }
            }

            final Integer finalUserPoints = userPoints;
            List<MallItemVO> itemVOs = items.stream().map(item -> {
                try {
                    MallItemVO vo = new MallItemVO();
                    BeanUtils.copyProperties(item, vo);
                    
                    // 设置类型名称
                    MallItemType type = MallItemType.fromCode(item.getItemType());
                    vo.setItemTypeName(type != null ? type.getDescription() : item.getItemType());
                    
                    // 设置持续时间文本
                    if (item.getDurationDays() == null || item.getDurationDays() == 0) {
                        vo.setDurationText("永久");
                    } else {
                        vo.setDurationText(item.getDurationDays() + "天");
                    }
                    
                    // 设置是否可购买
                    vo.setCanPurchase(finalUserPoints >= item.getItemPrice());
                    
                    vo.setIsActive(false);
                    // 设置是否已拥有（仅对装扮类商品）
                    if ("AVATAR_FRAME".equals(item.getItemType()) ||
                            "NICKNAME_COLOR".equals(item.getItemType()) ||
                            "PROFILE_THEME".equals(item.getItemType()) ||
                            "POST_THEME".equals(item.getItemType())) {
                        String value = getPrivilegeValueByItemCode(item.getItemType(), item.getItemCode());
                        String privilegeKey = item.getItemType() + ":" + value;
                        vo.setAlreadyOwned(ownedPrivileges.containsKey(privilegeKey));
                        vo.setIsActive(activePrivileges.containsKey(privilegeKey));
                    } else {
                        vo.setAlreadyOwned(false);
                    }
                    
                    return vo;
                } catch (Exception e) {
                    log.error("处理商品数据失败: itemId={}, error={}", item.getId(), e.getMessage());
                    throw new RuntimeException("处理商品数据失败", e);
                }
            }).collect(Collectors.toList());

            log.info("成功处理商品数量: {}", itemVOs.size());
            return Result.success(itemVOs);
        } catch (Exception e) {
            log.error("获取商城商品失败", e);
            return Result.error("获取商品列表失败");
        }
    }

    @Override
    public List<MallItem> getAvailableItems(String type) {
        LambdaQueryWrapper<MallItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MallItem::getItemStatus, 1);
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(MallItem::getItemType, type);
        }
        
        wrapper.orderByAsc(MallItem::getSortOrder);
        return mallItemMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean purchaseItem(Long userId, Long itemId, Long targetId) {
        try {
            // 获取商品信息
            MallItem item = mallItemMapper.selectById(itemId);
            if (item == null || item.getItemStatus() != 1) {
                return false;
            }

            // 检查用户积分是否足够
            Result pointsResult = pointsService.getUserPoints(userId);
            if (pointsResult.getCode() != 0) {
                return false;
            }
            
            UserPointsVO pointsVO = (UserPointsVO) pointsResult.getData();
            Integer availablePoints = pointsVO.getAvailablePoints();
            
            if (availablePoints < item.getItemPrice()) {
                return false;
            }

            // 扣除积分
            Result deductResult = pointsService.deductPoints(userId, item.getItemPrice(), 
                "购买商品：" + item.getItemName());
            if (deductResult.getCode() != 0) {
                return false;
            }

            // 创建购买记录
            UserPurchase purchase = new UserPurchase();
            purchase.setUserId(userId);
            purchase.setItemId(item.getId());
            purchase.setItemCode(item.getItemCode());
            purchase.setItemName(item.getItemName());
            purchase.setPointsCost(item.getItemPrice());
            purchase.setTargetId(targetId);
            purchase.setPurchaseTime(LocalDateTime.now());
            purchase.setStatus(1);

            // 设置过期时间
            if (item.getDurationDays() > 0) {
                purchase.setExpireTime(LocalDateTime.now().plusDays(item.getDurationDays()));
            }

            userPurchaseMapper.insert(purchase);

            // 根据商品类型执行相应操作
            handlePurchaseEffect(item, userId, targetId, purchase.getExpireTime(), purchase.getId());

            return true;
        } catch (Exception e) {
            log.error("购买商品失败", e);
            return false;
        }
    }

    @Override
    public List<UserPurchase> getUserPurchases(Long userId) {
        LambdaQueryWrapper<UserPurchase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPurchase::getUserId, userId)
               .orderByDesc(UserPurchase::getPurchaseTime);
        return userPurchaseMapper.selectList(wrapper);
    }

    @Override
    public boolean hasPrivilege(Long userId, String privilegeType, Long targetId) {
        // 对于帖子特权（置顶、高亮），需要检查购买记录表
        if ("POST_TOP".equals(privilegeType) || "POST_HIGHLIGHT".equals(privilegeType)) {
            LambdaQueryWrapper<UserPurchase> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserPurchase::getUserId, userId)
                   .like(UserPurchase::getItemCode, privilegeType)
                   .eq(UserPurchase::getStatus, 1);
            
            if (targetId != null) {
                wrapper.eq(UserPurchase::getTargetId, targetId);
            }
            
            UserPurchase purchase = userPurchaseMapper.selectOne(wrapper);
            
            if (purchase == null) {
                return false;
            }
            
            // 检查是否过期
            if (purchase.getExpireTime() != null && purchase.getExpireTime().isBefore(LocalDateTime.now())) {
                return false;
            }
            
            return true;
        } else {
            // 对于其他特权（头像框、昵称颜色），检查特权表
            LambdaQueryWrapper<UserPrivilege> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserPrivilege::getUserId, userId)
                   .eq(UserPrivilege::getPrivilegeType, privilegeType)
                   .eq(UserPrivilege::getIsActive, 1);
            
            UserPrivilege privilege = userPrivilegeMapper.selectOne(wrapper);
            
            if (privilege == null) {
                return false;
            }
            
            // 检查是否过期
            if (privilege.getExpireTime() != null && privilege.getExpireTime().isBefore(LocalDateTime.now())) {
                return false;
            }
            
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean togglePrivilege(Long userId, String type, String value) {
        // 如果要切换的是默认装扮（即取消装扮，可以用 "default" 作为 value 标识）
        if ("default".equals(value) || "#333333".equals(value)) {
            LambdaQueryWrapper<UserPrivilege> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(UserPrivilege::getUserId, userId)
                         .eq(UserPrivilege::getPrivilegeType, type);
            UserPrivilege updatePrivilege = new UserPrivilege();
            updatePrivilege.setIsActive(0);
            userPrivilegeMapper.update(updatePrivilege, updateWrapper);
            return true;
        }

        // 先检查用户是否拥有该装扮
        LambdaQueryWrapper<UserPrivilege> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserPrivilege::getUserId, userId)
                    .eq(UserPrivilege::getPrivilegeType, type)
                    .eq(UserPrivilege::getPrivilegeValue, value);
        UserPrivilege privilege = userPrivilegeMapper.selectOne(checkWrapper);

        if (privilege == null) {
            UserPurchase matchedPurchase = findValidPurchaseForPrivilege(userId, type, value);
            if (matchedPurchase == null) {
                return false;
            }
            privilege = new UserPrivilege();
            privilege.setUserId(userId);
            privilege.setPrivilegeType(type);
            privilege.setPrivilegeValue(value);
            privilege.setExpireTime(matchedPurchase.getExpireTime());
            privilege.setIsActive(0);
            userPrivilegeMapper.insert(privilege);
        }

        // 检查是否过期
        if (privilege.getExpireTime() != null && privilege.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 将该类型的所有其他装扮置为不激活
        LambdaQueryWrapper<UserPrivilege> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(UserPrivilege::getUserId, userId)
                     .eq(UserPrivilege::getPrivilegeType, type);
        UserPrivilege updatePrivilege = new UserPrivilege();
        updatePrivilege.setIsActive(0);
        userPrivilegeMapper.update(updatePrivilege, updateWrapper);

        // 激活当前选中的装扮
        privilege.setIsActive(1);
        userPrivilegeMapper.updateById(privilege);

        return true;
    }

    /**
     * 处理购买效果
     */
    private void handlePurchaseEffect(MallItem item, Long userId, Long targetId, LocalDateTime expireTime, Long currentPurchaseId) {
        switch (item.getItemType()) {
            case "POST_TOP":
                // 实现帖子置顶逻辑
                handlePostTop(targetId, expireTime, currentPurchaseId);
                break;
            case "POST_HIGHLIGHT":
                // 实现帖子高亮逻辑
                handlePostHighlight(targetId, expireTime, currentPurchaseId);
                break;
            case "AVATAR_FRAME":
                handleAvatarFrame(userId, item.getItemCode(), expireTime);
                break;
            case "NICKNAME_COLOR":
                handleNicknameColor(userId, item.getItemCode(), expireTime);
                break;
            case "PROFILE_THEME":
                handleProfileTheme(userId, item.getItemCode(), expireTime);
                break;
            case "POST_THEME":
                handlePostTheme(userId, item.getItemCode(), expireTime);
                break;
        }
    }

    private void handlePostTop(Long postId, LocalDateTime expireTime, Long currentPurchaseId) {
        // 实现帖子置顶逻辑: 将该帖子之前的置顶记录置为无效(避免重复导致查询出现笛卡尔积)
        LambdaQueryWrapper<UserPurchase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPurchase::getTargetId, postId)
               .like(UserPurchase::getItemCode, "POST_TOP")
               .eq(UserPurchase::getStatus, 1)
               .ne(UserPurchase::getId, currentPurchaseId); // 排除当前购买记录
        
        List<UserPurchase> oldPurchases = userPurchaseMapper.selectList(wrapper);
        for (UserPurchase op : oldPurchases) {
            op.setStatus(0);
            userPurchaseMapper.updateById(op);
        }
        log.info("帖子置顶：postId={}, expireTime={}", postId, expireTime);
    }

    private void handlePostHighlight(Long postId, LocalDateTime expireTime, Long currentPurchaseId) {
        // 实现帖子高亮逻辑: 将该帖子之前的高亮记录置为无效
        LambdaQueryWrapper<UserPurchase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPurchase::getTargetId, postId)
               .like(UserPurchase::getItemCode, "POST_HIGHLIGHT")
               .eq(UserPurchase::getStatus, 1)
               .ne(UserPurchase::getId, currentPurchaseId); // 排除当前购买记录
        
        List<UserPurchase> oldPurchases = userPurchaseMapper.selectList(wrapper);
        for (UserPurchase op : oldPurchases) {
            op.setStatus(0);
            userPurchaseMapper.updateById(op);
        }
        log.info("帖子高亮：postId={}, expireTime={}", postId, expireTime);
    }

    private void handleAvatarFrame(Long userId, String itemCode, LocalDateTime expireTime) {
        upsertDecorationPrivilege(userId, "AVATAR_FRAME", getFrameStyleFromCode(itemCode), expireTime);
    }

    private void handleNicknameColor(Long userId, String itemCode, LocalDateTime expireTime) {
        upsertDecorationPrivilege(userId, "NICKNAME_COLOR", getColorFromCode(itemCode), expireTime);
    }

    private void handleProfileTheme(Long userId, String itemCode, LocalDateTime expireTime) {
        upsertDecorationPrivilege(userId, "PROFILE_THEME", getProfileThemeFromCode(itemCode), expireTime);
    }

    private void handlePostTheme(Long userId, String itemCode, LocalDateTime expireTime) {
        upsertDecorationPrivilege(userId, "POST_THEME", getPostThemeFromCode(itemCode), expireTime);
    }

    private void upsertDecorationPrivilege(Long userId, String privilegeType, String privilegeValue, LocalDateTime expireTime) {
        if (privilegeValue == null || privilegeValue.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<UserPrivilege> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPrivilege::getUserId, userId)
               .eq(UserPrivilege::getPrivilegeType, privilegeType)
               .eq(UserPrivilege::getPrivilegeValue, privilegeValue);

        UserPrivilege privilege = userPrivilegeMapper.selectOne(wrapper);
        if (privilege == null) {
            LambdaQueryWrapper<UserPrivilege> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(UserPrivilege::getUserId, userId)
                         .eq(UserPrivilege::getPrivilegeType, privilegeType);
            UserPrivilege updatePrivilege = new UserPrivilege();
            updatePrivilege.setIsActive(0);
            userPrivilegeMapper.update(updatePrivilege, updateWrapper);

            privilege = new UserPrivilege();
            privilege.setUserId(userId);
            privilege.setPrivilegeType(privilegeType);
            privilege.setPrivilegeValue(privilegeValue);
            privilege.setExpireTime(expireTime);
            privilege.setIsActive(1);
            userPrivilegeMapper.insert(privilege);
        } else {
            LambdaQueryWrapper<UserPrivilege> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(UserPrivilege::getUserId, userId)
                         .eq(UserPrivilege::getPrivilegeType, privilegeType);
            UserPrivilege updatePrivilege = new UserPrivilege();
            updatePrivilege.setIsActive(0);
            userPrivilegeMapper.update(updatePrivilege, updateWrapper);

            privilege.setExpireTime(expireTime);
            privilege.setIsActive(1);
            userPrivilegeMapper.updateById(privilege);
        }
    }

    private UserPurchase findValidPurchaseForPrivilege(Long userId, String type, String value) {
        LambdaQueryWrapper<UserPurchase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPurchase::getUserId, userId)
                .eq(UserPurchase::getStatus, 1)
                .likeRight(UserPurchase::getItemCode, type)
                .orderByDesc(UserPurchase::getPurchaseTime);
        List<UserPurchase> purchases = userPurchaseMapper.selectList(wrapper);
        LocalDateTime now = LocalDateTime.now();
        for (UserPurchase purchase : purchases) {
            if (purchase.getExpireTime() != null && purchase.getExpireTime().isBefore(now)) {
                continue;
            }
            String mappedValue = getPrivilegeValueByItemCode(type, purchase.getItemCode());
            if (value.equals(mappedValue)) {
                return purchase;
            }
        }
        return null;
    }

    private String getPrivilegeValueByItemCode(String itemType, String itemCode) {
        switch (itemType) {
            case "AVATAR_FRAME":
                return getFrameStyleFromCode(itemCode);
            case "NICKNAME_COLOR":
                return getColorFromCode(itemCode);
            case "PROFILE_THEME":
                return getProfileThemeFromCode(itemCode);
            case "POST_THEME":
                return getPostThemeFromCode(itemCode);
            default:
                return null;
        }
    }

    private String getFrameStyleFromCode(String itemCode) {
        switch (itemCode) {
            case "AVATAR_FRAME_GOLD": return "gold";
            case "AVATAR_FRAME_RAINBOW": return "rainbow";
            default: return "default";
        }
    }

    private String getColorFromCode(String itemCode) {
        switch (itemCode) {
            case "NICKNAME_COLOR_RED": return "#ff4757";
            case "NICKNAME_COLOR_BLUE": return "#3742fa";
            case "NICKNAME_COLOR_PURPLE": return "#8c7ae6";
            case "NICKNAME_COLOR_GRADIENT": return "linear-gradient(45deg, #ff6b6b, #4ecdc4)";
            default: return "#333333";
        }
    }

    private String getProfileThemeFromCode(String itemCode) {
        switch (itemCode) {
            case "PROFILE_THEME_OCEAN": return "linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)";
            case "PROFILE_THEME_SUNSET": return "linear-gradient(135deg, #f093fb 0%, #f5576c 50%, #fda085 100%)";
            case "PROFILE_THEME_FOREST": return "linear-gradient(135deg, #134e5e 0%, #71b280 100%)";
            case "PROFILE_THEME_AURORA": return "linear-gradient(135deg, #00c3ff 0%, #ffff1c 50%, #ff00c8 100%)";
            default: return "";
        }
    }

    private String getPostThemeFromCode(String itemCode) {
        switch (itemCode) {
            case "POST_THEME_STARRY": return "starry";
            case "POST_THEME_SAKURA": return "sakura";
            case "POST_THEME_NEON": return "neon";
            case "POST_THEME_LAVA": return "lava";
            default: return "";
        }
    }
}
