package cn.edu.seig.vibemusic.agent.config;

import cn.edu.seig.vibemusic.agent.service.MusicAgentAssistant;
import cn.edu.seig.vibemusic.agent.tool.AgentMusicTool;
import cn.edu.seig.vibemusic.agent.tool.AgentNavigationTool;
import cn.edu.seig.vibemusic.agent.tool.AgentSearchTool;
import cn.edu.seig.vibemusic.agent.tool.AgentUserTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智能体相关 Bean 配置类
 *
 * 负责注册大语言模型实例以及面向业务使用的智能体助手。
 */
@Configuration
public class AgentConfig {

    /**
     * 创建通用聊天模型
     *
     * @param properties 模型参数配置
     * @return LangChain4j 聊天模型实例
     */
    @Bean
    public ChatLanguageModel chatLanguageModel(AgentLlmProperties properties) {
        return OpenAiChatModel.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .modelName(properties.getModelName())
                .temperature(properties.getTemperature())
                .build();
    }

    /**
     * 创建音乐智能体助手
     *
     * 这里将音乐、用户和页面导航等工具注入到智能体中，
     * 便于模型在对话过程中调用真实业务能力。
     *
     * @param chatLanguageModel 聊天模型
     * @param agentMusicTool 音乐工具
     * @param agentUserTool 用户工具
     * @param agentNavigationTool 页面导航工具
     * @return 音乐智能体助手实例
     */
    @Bean
    public MusicAgentAssistant musicAgentAssistant(ChatLanguageModel chatLanguageModel,
                                                   AgentMusicTool agentMusicTool,
                                                   AgentUserTool agentUserTool,
                                                   AgentNavigationTool agentNavigationTool,
                                                   AgentSearchTool agentSearchTool) {
        return AiServices.builder(MusicAgentAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(agentMusicTool, agentUserTool, agentNavigationTool, agentSearchTool)
                .build();
    }
}
