package cn.edu.seig.vibemusic.agent.tool;

import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import cn.edu.seig.vibemusic.model.vo.UserPointsVO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.INotificationService;
import cn.edu.seig.vibemusic.service.IPointsService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户信息相关工具。
 */
@Component
@RequiredArgsConstructor
public class AgentUserTool {

    private final INotificationService notificationService;
    private final IPointsService pointsService;
    private final AgentRuntimeContext agentRuntimeContext;

    /**
     * 查询当前会话用户的未读通知数量。
     */
    @Tool("查询当前登录用户的未读通知数量。适用于用户问未读通知、通知条数、通知数量等问题。")
    public String getUnreadNotificationCount() {
        Long userId = agentRuntimeContext.current().getUserId();
        AgentToolDataVO data = getUnreadNotificationCountData(userId);
        agentRuntimeContext.setToolData(data);

        if (Boolean.TRUE.equals(data.getSuccess())) {
            return "当前未读通知数量为：" + data.getUnreadCount();
        }
        return "查询未读通知失败";
    }

    /**
     * 查询当前会话用户的积分信息。
     */
    @Tool("查询当前登录用户的积分信息。适用于用户问积分、可用积分、总积分等问题。")
    public String getUserPoints() {
        Long userId = agentRuntimeContext.current().getUserId();
        AgentToolDataVO data = getUserPointsData(userId);
        agentRuntimeContext.setToolData(data);

        if (Boolean.TRUE.equals(data.getSuccess())) {
            return "当前可用积分为：" + data.getAvailablePoints() + "，总积分为：" + data.getTotalPoints();
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
