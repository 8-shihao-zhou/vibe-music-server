package cn.edu.seig.vibemusic.agent.tool;

import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 页面导航相关工具
 *
 * 说明：
 * 1. 负责把用户自然语言解析成前端路由
 * 2. 当前先支持所有静态页面
 * 3. 后续如果要支持歌手详情、歌单详情这类带 ID 页面，再扩展单独解析逻辑
 */
@Component
public class AgentNavigationTool {

    @Tool("根据用户输入解析应该跳转到哪个页面路径")
    public String resolvePage(String userMessage) {
        AgentToolDataVO data = resolvePageData(userMessage);
        if (Boolean.TRUE.equals(data.getSuccess())) {
            return String.format("页面名称：%s，页面路径：%s", data.getPageName(), data.getPagePath());
        }
        return "未识别到明确页面";
    }

    /**
     * 供 Service 使用的结构化页面解析
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
                    data.setMessage("解析到页面：" + pageMapping.getPageName());
                    return data;
                }
            }
        }

        data.setMessage("未识别到明确页面");
        return data;
    }

    /**
     * 对用户输入做简单标准化，提升页面匹配成功率
     */
    private String normalizeMessage(String userMessage) {
        return userMessage.trim()
                .toLowerCase()
                .replace("页面", "")
                .replace("界面", "")
                .replace("一下", "")
                .replace("一下子", "")
                .replace("帮我", "")
                .replace("请帮我", "")
                .replace("带我去", "")
                .replace("跳到", "跳转")
                .replace("去到", "去");
    }
}
