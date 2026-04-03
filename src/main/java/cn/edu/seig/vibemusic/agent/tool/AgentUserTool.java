package cn.edu.seig.vibemusic.agent.tool;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import cn.edu.seig.vibemusic.model.vo.UserPointsVO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.INotificationService;
import cn.edu.seig.vibemusic.service.IPointsService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户信息相关工具
 */
@Component
@RequiredArgsConstructor
public class AgentUserTool {

    /**
     * 通知业务服务，用于查询未读通知数量
     */
    private final INotificationService notificationService;

    /**
     * 积分业务服务，用于查询用户积分信息
     */
    private final IPointsService pointsService;

    /**
     * 提供给大模型调用的未读通知查询工具
     *
     * @param userId 当前用户 ID
     * @return 便于模型理解的文本结果
     */
    @Tool("查询用户未读通知数量")
    public String getUnreadNotificationCount(Long userId) {
        AgentToolDataVO data = getUnreadNotificationCountData(userId);
        if (Boolean.TRUE.equals(data.getSuccess())) {
            return "当前未读通知数量：" + data.getUnreadCount();
        }
        return "查询未读通知失败";
    }

    /**
     * 提供给大模型调用的积分查询工具
     *
     * @param userId 当前用户 ID
     * @return 便于模型理解的文本结果
     */
    @Tool("查询用户积分")
    public String getUserPoints(Long userId) {
        AgentToolDataVO data = getUserPointsData(userId);
        if (Boolean.TRUE.equals(data.getSuccess())) {
            return "当前可用积分：" + data.getAvailablePoints() + "，总积分：" + data.getTotalPoints();
        }
        return "查询积分失败";
    }

    /**
     * 查询未读通知数量的结构化方法
     */
    public AgentToolDataVO getUnreadNotificationCountData(Long userId) {
        AgentToolDataVO data = new AgentToolDataVO();
        data.setSuccess(false);

        if (userId == null) {
            data.setMessage("用户ID不能为空");
            return data;
        }

        Integer unreadCount = notificationService.getUnreadCount(userId);
        data.setSuccess(true);
        data.setUnreadCount(unreadCount == null ? 0 : unreadCount);
        data.setMessage("查询未读通知成功");
        return data;
    }

    /**
     * 查询积分的结构化方法
     */
    public AgentToolDataVO getUserPointsData(Long userId) {
        AgentToolDataVO data = new AgentToolDataVO();
        data.setSuccess(false);

        if (userId == null) {
            data.setMessage("用户ID不能为空");
            return data;
        }

        Result result = pointsService.getUserPoints(userId);
        if (result == null || result.getCode() != 0 || result.getData() == null) {
            data.setMessage("积分查询失败");
            return data;
        }

        UserPointsVO pointsVO = (UserPointsVO) result.getData();
        data.setSuccess(true);
        data.setAvailablePoints(pointsVO.getAvailablePoints());
        data.setTotalPoints(pointsVO.getTotalPoints());
        data.setMessage("积分查询成功");
        return data;
    }
}

