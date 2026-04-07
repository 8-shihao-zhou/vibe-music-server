package cn.edu.seig.vibemusic.agent.tool;

import cn.edu.seig.vibemusic.agent.enums.AgentActionType;
import cn.edu.seig.vibemusic.agent.model.vo.AgentActionVO;
import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 页面导航工具。
 */
@Component
@RequiredArgsConstructor
public class AgentNavigationTool {

    private final AgentRuntimeContext agentRuntimeContext;

    /**
     * 解析用户想打开哪个页面，并准备前端跳转动作。
     *
     * @param userMessage 用户原话
     * @return 便于模型理解的文本结果
     */
    @Tool("根据用户原话解析要打开的页面，并在命中时准备页面跳转动作。适用于打开页面、进入页面、去某个功能页等请求。")
    public String resolvePage(String userMessage) {
        AgentToolDataVO data = resolvePageData(userMessage);
        agentRuntimeContext.setToolData(data);

        if (!Boolean.TRUE.equals(data.getSuccess())) {
            return "未识别到明确页面";
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("path", data.getPagePath());
        payload.put("pageName", data.getPageName());

        agentRuntimeContext.addAction(new AgentActionVO(AgentActionType.NAVIGATE_TO.getCode(), payload));
        return String.format("已解析页面：%s，路径=%s，已准备跳转动作", data.getPageName(), data.getPagePath());
    }

    /**
     * 供后端使用的结构化页面解析方法
     */
    public AgentToolDataVO resolvePageData(String userMessage) {
        AgentToolDataVO data = new AgentToolDataVO();
        data.setSuccess(false);

        if (userMessage == null || userMessage.trim().isEmpty()) {
            data.setMessage("页面意图为空");
            return data;
        }

        String message = normalizeMessage(userMessage);

        for (AgentPageRegistry.PageMapping pageMapping : AgentPageRegistry.PAGE_MAPPINGS) {
            for (String alias : pageMapping.getAliases()) {
                if (message.contains(alias.toLowerCase())) {
                    data.setSuccess(true);
                    data.setPagePath(pageMapping.getPagePath());
                    data.setPageName(pageMapping.getPageName());
                    data.setMessage("已解析到页面：" + pageMapping.getPageName());
                    return data;
                }
            }
        }

        data.setMessage("未识别到明确页面");
        return data;
    }

    /**
     * 对输入做标准化，提高匹配成功率
     */
    private String normalizeMessage(String userMessage) {
        return userMessage.trim()
                .toLowerCase()
                .replace("页面", "")
                .replace("界面", "")
                .replace("一个", "")
                .replace("一下", "")
                .replace("帮我", "")
                .replace("请帮我", "")
                .replace("带我去", "")
                .replace("跳到", "跳转")
                .replace("去到", "去");
    }
}
