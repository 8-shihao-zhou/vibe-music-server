package cn.edu.seig.vibemusic.agent.tool;

import cn.edu.seig.vibemusic.agent.model.vo.AgentActionVO;
import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 运行时上下文。
 *
 * 作用：
 * 1. 让工具在被大模型调用时，也能把结构化结果回填给后端
 * 2. 保持前端现有的 reply/actions/toolData 协议不变
 * 3. 避免把前端协议拼装逻辑继续写死在 Service 的 if/else 中
 */
@Component
public class AgentRuntimeContext {

    private final ThreadLocal<State> holder = new ThreadLocal<>();

    /**
     * 每次请求开始时初始化上下文
     */
    public void start(Long userId, String sessionId) {
        State state = new State();
        state.setUserId(userId);
        state.setSessionId(sessionId);
        holder.set(state);
    }

    /**
     * 获取当前请求上下文
     */
    public State current() {
        State state = holder.get();
        if (state == null) {
            state = new State();
            holder.set(state);
        }
        return state;
    }

    /**
     * 记录工具返回的结构化数据
     */
    public void setToolData(AgentToolDataVO toolData) {
        current().setToolData(toolData);
    }

    /**
     * 向前端动作列表中追加动作
     */
    public void addAction(AgentActionVO action) {
        current().getActions().add(action);
    }

    /**
     * 清理当前请求上下文，避免线程复用带来脏数据
     */
    public void clear() {
        holder.remove();
    }

    /**
     * Agent 当前请求状态
     */
    public static class State {
        private Long userId;
        private String sessionId;
        private AgentToolDataVO toolData;
        private final List<AgentActionVO> actions = new ArrayList<>();

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public AgentToolDataVO getToolData() {
            return toolData;
        }

        public void setToolData(AgentToolDataVO toolData) {
            this.toolData = toolData;
        }

        public List<AgentActionVO> getActions() {
            return actions;
        }
    }
}
