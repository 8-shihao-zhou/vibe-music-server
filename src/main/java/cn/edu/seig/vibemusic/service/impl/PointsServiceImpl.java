package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.enums.PointsActionType;
import cn.edu.seig.vibemusic.mapper.DailyPointsLimitMapper;
import cn.edu.seig.vibemusic.mapper.PointsLogMapper;
import cn.edu.seig.vibemusic.mapper.UserPointsMapper;
import cn.edu.seig.vibemusic.model.entity.DailyPointsLimit;
import cn.edu.seig.vibemusic.model.entity.PointsLog;
import cn.edu.seig.vibemusic.model.entity.UserPoints;
import cn.edu.seig.vibemusic.model.vo.PointsLogVO;
import cn.edu.seig.vibemusic.model.vo.UserPointsVO;
import cn.edu.seig.vibemusic.service.IPointsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PointsServiceImpl implements IPointsService {

    @Autowired
    private UserPointsMapper userPointsMapper;

    @Autowired
    private PointsLogMapper pointsLogMapper;

    @Autowired
    private DailyPointsLimitMapper dailyPointsLimitMapper;

    @Autowired
    private cn.edu.seig.vibemusic.mapper.UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addPoints(Long userId, String actionType, Long relatedId) {
        try {
            // 获取行为类型
            PointsActionType action = PointsActionType.fromCode(actionType);
            if (action == null) {
                return Result.error("无效的行为类型");
            }

            // 检查每日限制
            if (!checkDailyLimit(userId, actionType)) {
                return Result.error("今日该行为已达上限");
            }

            // 获取或创建用户积分记录
            UserPoints userPoints = getUserPointsEntity(userId);

            // 增加积分
            userPoints.setTotalPoints(userPoints.getTotalPoints() + action.getPoints());
            userPoints.setAvailablePoints(userPoints.getAvailablePoints() + action.getPoints());

            // 更新等级
            updateLevel(userPoints);

            // 保存用户积分
            userPointsMapper.updateById(userPoints);

            // 记录积分日志
            PointsLog pointsLog = new PointsLog();
            pointsLog.setUserId(userId);
            pointsLog.setPoints(action.getPoints());
            pointsLog.setActionType(actionType);
            pointsLog.setDescription(action.getDescription());
            pointsLog.setRelatedId(relatedId);
            pointsLogMapper.insert(pointsLog);

            // 更新每日限制
            updateDailyLimit(userId, actionType, action.getPoints());

            log.info("用户{}通过{}获得{}积分", userId, action.getDescription(), action.getPoints());
            return Result.success("获得" + action.getPoints() + "积分");
        } catch (Exception e) {
            log.error("增加积分失败", e);
            return Result.error("增加积分失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deductPoints(Long userId, Integer points, String description) {
        try {
            UserPoints userPoints = getUserPointsEntity(userId);

            // 检查积分是否足够
            if (userPoints.getAvailablePoints() < points) {
                return Result.error("积分不足");
            }

            // 扣除积分
            userPoints.setAvailablePoints(userPoints.getAvailablePoints() - points);
            userPointsMapper.updateById(userPoints);

            // 记录积分日志
            PointsLog pointsLog = new PointsLog();
            pointsLog.setUserId(userId);
            pointsLog.setPoints(-points);
            pointsLog.setActionType("DEDUCT");
            pointsLog.setDescription(description);
            pointsLogMapper.insert(pointsLog);

            log.info("用户{}消耗{}积分：{}", userId, points, description);
            return Result.success("消耗" + points + "积分");
        } catch (Exception e) {
            log.error("扣除积分失败", e);
            return Result.error("扣除积分失败");
        }
    }

    @Override
    public Result getUserPoints(Long userId) {
        try {
            UserPoints userPoints = getUserPointsEntity(userId);
            
            UserPointsVO vo = new UserPointsVO();
            vo.setUserId(userPoints.getUserId());
            vo.setTotalPoints(userPoints.getTotalPoints());
            vo.setAvailablePoints(userPoints.getAvailablePoints());
            vo.setLevel(userPoints.getLevel());
            vo.setLevelName(getLevelName(userPoints.getLevel()));
            vo.setNextLevelPoints(getNextLevelPoints(userPoints.getLevel()));

            // 计算排名
            LambdaQueryWrapper<UserPoints> wrapper = new LambdaQueryWrapper<>();
            wrapper.gt(UserPoints::getTotalPoints, userPoints.getTotalPoints());
            long ranking = userPointsMapper.selectCount(wrapper) + 1;
            vo.setRanking((int) ranking);

            return Result.success(vo);
        } catch (Exception e) {
            log.error("查询用户积分失败", e);
            return Result.error("查询失败");
        }
    }

    @Override
    public Result getPointsLog(Long userId, Integer pageNum, Integer pageSize) {
        try {
            Page<PointsLog> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PointsLog::getUserId, userId)
                   .orderByDesc(PointsLog::getCreateTime);

            Page<PointsLog> result = pointsLogMapper.selectPage(page, wrapper);

            List<PointsLogVO> voList = result.getRecords().stream().map(log -> {
                PointsLogVO vo = new PointsLogVO();
                BeanUtils.copyProperties(log, vo);
                vo.setChangeType(log.getPoints() >= 0 ? "EARN" : "SPEND");
                return vo;
            }).collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("records", voList);
            data.put("total", result.getTotal());
            data.put("current", result.getCurrent());
            data.put("size", result.getSize());

            return Result.success(data);
        } catch (Exception e) {
            log.error("查询积分记录失败", e);
            return Result.error("查询失败");
        }
    }

    @Override
    public Result getPointsRanking(Integer pageNum, Integer pageSize) {
        try {
            Page<UserPoints> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<UserPoints> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(UserPoints::getTotalPoints);

            Page<UserPoints> result = userPointsMapper.selectPage(page, wrapper);

            List<UserPointsVO> voList = result.getRecords().stream().map(userPoints -> {
                UserPointsVO vo = new UserPointsVO();
                vo.setUserId(userPoints.getUserId());
                vo.setTotalPoints(userPoints.getTotalPoints());
                vo.setAvailablePoints(userPoints.getAvailablePoints());
                vo.setLevel(userPoints.getLevel());
                vo.setLevelName(getLevelName(userPoints.getLevel()));
                // 查询用户名
                cn.edu.seig.vibemusic.model.entity.User user = userMapper.selectById(userPoints.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                }
                return vo;
            }).collect(Collectors.toList());

            // 设置排名
            for (int i = 0; i < voList.size(); i++) {
                voList.get(i).setRanking((int) ((page.getCurrent() - 1) * page.getSize() + i + 1));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("records", voList);
            data.put("total", result.getTotal());

            return Result.success(data);
        } catch (Exception e) {
            log.error("查询积分排行榜失败", e);
            return Result.error("查询失败");
        }
    }

    @Override
    public boolean checkDailyLimit(Long userId, String actionType) {
        PointsActionType action = PointsActionType.fromCode(actionType);
        if (action == null || action.getDailyLimit() == -1) {
            return true; // 无限制
        }

        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<DailyPointsLimit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyPointsLimit::getUserId, userId)
               .eq(DailyPointsLimit::getActionType, actionType)
               .eq(DailyPointsLimit::getDate, today);

        DailyPointsLimit limit = dailyPointsLimitMapper.selectOne(wrapper);

        if (limit == null) {
            return true; // 今天还没有记录
        }

        // 检查积分上限
        if (action.getDailyLimit() > 0 && limit.getPointsEarned() >= action.getDailyLimit()) {
            return false;
        }

        // 检查次数上限
        if (action.getMaxCount() > 0 && limit.getActionCount() >= action.getMaxCount()) {
            return false;
        }

        return true;
    }

    @Override
    public Result getDailyTaskStatus(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            // 任务定义：actionType -> 任务目标次数（完成几次算达成）
            Map<String, Integer> taskTargets = new java.util.LinkedHashMap<>();
            taskTargets.put("DAILY_LOGIN", 1);
            taskTargets.put("POST_CREATE", 1);
            taskTargets.put("COMMENT_CREATE", 3);

            List<Map<String, Object>> taskList = new java.util.ArrayList<>();
            for (Map.Entry<String, Integer> entry : taskTargets.entrySet()) {
                String actionType = entry.getKey();
                int target = entry.getValue();

                LambdaQueryWrapper<DailyPointsLimit> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(DailyPointsLimit::getUserId, userId)
                       .eq(DailyPointsLimit::getActionType, actionType)
                       .eq(DailyPointsLimit::getDate, today);
                DailyPointsLimit limit = dailyPointsLimitMapper.selectOne(wrapper);

                int count = (limit != null) ? limit.getActionCount() : 0;
                boolean completed = count >= target;

                PointsActionType action = PointsActionType.fromCode(actionType);
                Map<String, Object> taskInfo = new HashMap<>();
                taskInfo.put("actionType", actionType);
                taskInfo.put("count", count);
                taskInfo.put("target", target);
                taskInfo.put("completed", completed);
                taskInfo.put("pointsPerAction", action != null ? action.getPoints() : 0);
                taskList.add(taskInfo);
            }
            return Result.success(taskList);
        } catch (Exception e) {
            log.error("查询今日任务状态失败", e);
            return Result.error("查询失败");
        }
    }

    @Override
    public void initUserPoints(Long userId) {
        LambdaQueryWrapper<UserPoints> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPoints::getUserId, userId);
        UserPoints existing = userPointsMapper.selectOne(wrapper);

        if (existing == null) {
            UserPoints userPoints = new UserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(0);
            userPoints.setAvailablePoints(0);
            userPoints.setLevel(1);
            userPointsMapper.insert(userPoints);
            log.info("初始化用户{}积分记录", userId);
        }
    }

    /**
     * 获取用户积分实体，不存在则创建
     */
    private UserPoints getUserPointsEntity(Long userId) {
        LambdaQueryWrapper<UserPoints> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPoints::getUserId, userId);
        UserPoints userPoints = userPointsMapper.selectOne(wrapper);

        if (userPoints == null) {
            userPoints = new UserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(0);
            userPoints.setAvailablePoints(0);
            userPoints.setLevel(1);
            userPointsMapper.insert(userPoints);
        }

        return userPoints;
    }

    /**
     * 更新用户等级
     */
    private void updateLevel(UserPoints userPoints) {
        int totalPoints = userPoints.getTotalPoints();
        int newLevel;

        if (totalPoints >= 1000) {
            newLevel = 5;
        } else if (totalPoints >= 600) {
            newLevel = 4;
        } else if (totalPoints >= 300) {
            newLevel = 3;
        } else if (totalPoints >= 100) {
            newLevel = 2;
        } else {
            newLevel = 1;
        }

        userPoints.setLevel(newLevel);
    }

    /**
     * 更新每日限制
     */
    private void updateDailyLimit(Long userId, String actionType, Integer points) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<DailyPointsLimit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyPointsLimit::getUserId, userId)
               .eq(DailyPointsLimit::getActionType, actionType)
               .eq(DailyPointsLimit::getDate, today);

        DailyPointsLimit limit = dailyPointsLimitMapper.selectOne(wrapper);

        if (limit == null) {
            limit = new DailyPointsLimit();
            limit.setUserId(userId);
            limit.setActionType(actionType);
            limit.setPointsEarned(points);
            limit.setActionCount(1);
            limit.setDate(today);
            dailyPointsLimitMapper.insert(limit);
        } else {
            limit.setPointsEarned(limit.getPointsEarned() + points);
            limit.setActionCount(limit.getActionCount() + 1);
            dailyPointsLimitMapper.updateById(limit);
        }
    }

    /**
     * 获取等级名称
     */
    private String getLevelName(Integer level) {
        switch (level) {
            case 1: return "新手";
            case 2: return "活跃用户";
            case 3: return "资深用户";
            case 4: return "核心用户";
            case 5: return "大神";
            default: return "新手";
        }
    }

    /**
     * 获取下一等级所需积分
     */
    private Integer getNextLevelPoints(Integer level) {
        switch (level) {
            case 1: return 100;
            case 2: return 300;
            case 3: return 600;
            case 4: return 1000;
            case 5: return 0; // 已满级
            default: return 100;
        }
    }
}
