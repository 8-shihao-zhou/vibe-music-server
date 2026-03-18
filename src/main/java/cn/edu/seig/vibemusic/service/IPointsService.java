package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.result.Result;

public interface IPointsService {
    /**
     * 增加积分
     */
    Result addPoints(Long userId, String actionType, Long relatedId);

    /**
     * 扣除积分
     */
    Result deductPoints(Long userId, Integer points, String description);

    /**
     * 查询用户积分
     */
    Result getUserPoints(Long userId);

    /**
     * 查询积分记录
     */
    Result getPointsLog(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询积分排行榜
     */
    Result getPointsRanking(Integer pageNum, Integer pageSize);

    /**
     * 检查每日限制
     */
    boolean checkDailyLimit(Long userId, String actionType);

    /**
     * 查询今日任务完成状态
     */
    Result getDailyTaskStatus(Long userId);

    /**
     * 初始化用户积分
     */
    void initUserPoints(Long userId);
}
