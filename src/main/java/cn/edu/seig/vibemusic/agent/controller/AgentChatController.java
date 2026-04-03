package cn.edu.seig.vibemusic.agent.controller;

import cn.edu.seig.vibemusic.agent.model.dto.AgentChatRequest;
import cn.edu.seig.vibemusic.agent.model.vo.AgentChatResponse;
import cn.edu.seig.vibemusic.agent.service.AgentChatService;
import cn.edu.seig.vibemusic.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
/**
 * 智能体聊天控制器
 *
 * 负责接收前端发送的对话请求，并返回智能体的统一响应结果。
 */
public class AgentChatController {

    /**
     * 智能体聊天业务服务
     */
    private final AgentChatService agentChatService;

    /**
     * 处理用户与智能体的聊天请求
     *
     * @param request 前端传入的聊天请求体
     * @return 智能体回复、动作指令以及工具查询结果
     */
    @PostMapping("/chat")
    public Result<AgentChatResponse> chat(@RequestBody AgentChatRequest request) {
        return Result.success(agentChatService.chat(request));
    }
}
