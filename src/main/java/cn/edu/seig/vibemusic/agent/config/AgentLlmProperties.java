package cn.edu.seig.vibemusic.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 智能体大模型配置属性
 *
 * 对应配置文件中的 agent.llm 前缀。
 */
@Data
@Component
@ConfigurationProperties(prefix = "agent.llm")
public class AgentLlmProperties {

    /**
     * 模型服务基础地址
     */
    private String baseUrl;

    /**
     * 模型服务访问密钥
     */
    private String apiKey;

    /**
     * 使用的模型名称
     */
    private String modelName;

    /**
     * 模型温度参数，越高越发散
     */
    private Double temperature;
}
