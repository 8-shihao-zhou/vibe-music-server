package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.IPointsService;
import cn.edu.seig.vibemusic.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/points")
public class PointsController {

    @Autowired
    private IPointsService pointsService;

    /**
     * 获取用户积分信息
     */
    @GetMapping("/info")
    public Result getUserPoints() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        return pointsService.getUserPoints(userId);
    }

    /**
     * 获取今日任务完成状态
     */
    @GetMapping("/daily-tasks")
    public Result getDailyTaskStatus() {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("未登录");
        return pointsService.getDailyTaskStatus(userId);
    }

    /**
     * 获取积分记录
     */
    @GetMapping("/log")
    public Result getPointsLog(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        return pointsService.getPointsLog(userId, pageNum, pageSize);
    }

    /**
     * 获取积分排行榜
     */
    @GetMapping("/ranking")
    public Result getPointsRanking(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "50") Integer pageSize) {
        return pointsService.getPointsRanking(pageNum, pageSize);
    }

    /**
     * 扣除积分（用于兑换等功能）
     */
    @PostMapping("/deduct")
    public Result deductPoints(@RequestParam Integer points,
                               @RequestParam String description) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        return pointsService.deductPoints(userId, points, description);
    }
}
