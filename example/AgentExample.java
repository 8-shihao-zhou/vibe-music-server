package com.zsh.agentstudy.api.example;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.zsh.agentstudy.api.ResponseFormat;
//import com.zsh.agentstudy.api.UserLocationTool;
//import com.zsh.agentstudy.api.WeatherForLocationTool;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.function.FunctionToolCallback;
import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.ai.chat.messages.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AgentExample - 演示如何构建一个功能完整的天气预报Agent
 * 包含以下生产级特性：
 * - 详细的系统提示
 * - 自定义工具
 * - 模型配置
 * - 结构化输出
 * - 对话记忆
 */
public class AgentExample {

    // 1. 定义系统提示 - 定义Agent的角色和行为
    private static final String SYSTEM_PROMPT = """
            You are an expert weather forecaster, who speaks in puns.

            You have access to two tools:

            - get_weather_for_location: use this to get the weather for a specific location
            - get_user_location: use this to get the user's location

            If a user asks you for the weather, make sure you know the location.
            If you can tell from the question that they mean wherever they are,
            use the get_user_location tool to find their location.
            """;

    // 2. 初始化 DashScope API
    private static final DashScopeApi dashScopeApi = DashScopeApi.builder()
            .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
            .build();

    // 3. 配置模型 - 设置温度和最大token数
    private static final ChatModel chatModel = DashScopeChatModel.builder()
            .dashScopeApi(dashScopeApi)
            .defaultOptions(DashScopeChatOptions.builder()
                    .withModel(DashScopeChatModel.DEFAULT_MODEL_NAME)
                    .withTemperature(0.5)
                    .withMaxToken(1000)
                    .build())
            .build();

    // 创建工具回调
    //private static final FunctionToolCallback<?, ?> getWeatherTool = WeatherForLocationTool.create();
    //private static final FunctionToolCallback<?, ?> getUserLocationTool = UserLocationTool.create();

    // threadId 是给定对话的唯一标识符，用于维护对话记忆
    private static final String threadId = "weather-thread-1";



    //outputSchema自定义JSON格式
    String customSchema = """
    请按照以下JSON格式输出：
    {
        "title": "标题",
        "content": "内容",
        "style": "风格"
    }
    """;

    //ModelCallLimitHook限制模型最多调用次数
    ModelCallLimitHook hook = ModelCallLimitHook.builder()
            .runLimit(5)  // 限制最多调用 5 次
            .exitBehavior(ModelCallLimitHook.ExitBehavior.ERROR)  // 超出限制时抛出异常
            .build();


    //ReactAgent 支持通过 Hooks 扩展功能，例如人机协同、工具注入等
    // 创建 hook
    Hook humanInTheLoopHook = HumanInTheLoopHook.builder()
            .approvalOn("getWeatherTool", ToolConfig.builder().description("Please confirm tool execution.")
                    .build())
            .build();



    public AgentExample() throws GraphRunnerException {
        // 6. 创建和运行 Agent
        ReactAgent agent = ReactAgent.builder()
                .name("weather_pun_agent")
                .model(chatModel)
                .systemPrompt(SYSTEM_PROMPT)
                //.tools(getUserLocationTool, getWeatherTool)
                .outputType(ResponseFormat.class)
                .outputSchema(customSchema)
                .hooks(hook)
                .hooks(humanInTheLoopHook)
                .saver(new MemorySaver())
                .build();

        // 配置运行时参数，包含threadId和用户ID
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata("user_id", "1")
                .build();

        // 第一次调用
        System.out.println("=== 第一次调用 ===");
        AssistantMessage response1 = agent.call("what is the weather in San Francisco today.", runnableConfig);
        System.out.println("回复: " + response1.getText());

        // 第二次调用 - 使用同一个threadId来保持对话记忆
        System.out.println("\n=== 第二次调用 ===");
        AssistantMessage response2 = agent.call("How about the weather tomorrow", runnableConfig);
        System.out.println("回复: " + response2.getText());

        AssistantMessage message = agent.call("帮我写一首关于春天的诗歌。");
        System.out.println(message.getText());

        // 使用 invoke 方法获取完整状态
        System.out.println("\n=== 使用 invoke 方法获取完整状态 ===");
        Optional<OverAllState> result = agent.invoke("帮我写一首诗。");

        if (result.isPresent()) {
            OverAllState state = result.get();
            // 访问消息历史
            List<Message> messages = state.value("messages", new ArrayList<>());
            // 访问其他状态信息
            System.out.println("消息历史长度: " + messages.size());
            System.out.println("完整状态: " + state);
        }

    }
}
