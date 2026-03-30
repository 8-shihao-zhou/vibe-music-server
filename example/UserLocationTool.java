package com.zsh.agentstudy.api.example;

//package com.zsh.agentstudy.api.example;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * 用户位置工具 - 用于根据用户ID获取用户位置
 * 演示如何使用 ToolContext 获取运行时上下文信息
 */
public class UserLocationTool implements BiFunction<String, ToolContext, String> {

    private static final String AGENT_CONFIG_CONTEXT_KEY = "config";

    @Override
    public String apply(
            @ToolParam(description = "用户查询") String query,
            ToolContext toolContext) {
        // 从上下文中获取用户信息
        String userId = "";
        if (toolContext != null && toolContext.getContext() != null) {
            Object config = toolContext.getContext().get(AGENT_CONFIG_CONTEXT_KEY);
            if (config instanceof RunnableConfig) {
                RunnableConfig runnableConfig = (RunnableConfig) config;
                Optional<Object> userIdObjOptional = runnableConfig.metadata("user_id");
                if (userIdObjOptional.isPresent()) {
                    userId = (String) userIdObjOptional.get();
                }
            }
        }
        if (userId == null || userId.isEmpty()) {
            userId = "1";
        }
        // 根据用户ID返回不同的位置
        return "1".equals(userId) ? "Florida" : "San Francisco";
    }

    /**
     * 创建用户位置工具的静态方法
     */
    public static FunctionToolCallback<?, ?> create() {
        return FunctionToolCallback
                .builder("getUserLocation", new UserLocationTool())
                .description("根据用户ID获取用户位置")
                .inputType(String.class)
                .build();
    }
}
