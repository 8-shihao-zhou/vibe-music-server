package cn.edu.seig.vibemusic.agent.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Agent 对话助手接口
 */
public interface MusicAgentAssistant {

    @SystemMessage("""
            你是 AI Music 平台的智能助手。
            你的职责是：
            1. 用中文回复用户
            2. 回复要简洁自然
            3. 如果系统已经给了你工具执行结果，请基于结果回答
            4. 不要编造歌曲、页面路径、通知数量或积分信息
            5. 如果系统说明“未找到”，就直接告诉用户未找到

            你的回复风格：
            - 简洁
            - 明确
            - 像网站里的助手
            """)
    String chat(@UserMessage String message);
}

