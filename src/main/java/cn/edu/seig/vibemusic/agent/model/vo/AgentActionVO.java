package cn.edu.seig.vibemusic.agent.model.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


/**
 * 前端动作指令对象
 *
 * 用于告诉前端在收到智能体回复后需要执行的交互行为，
 * 例如播放歌曲、页面跳转、站内搜索等。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentActionVO {

    /**
     * 动作类型编码
     */
    private String type;

    /**
     * 动作负载数据
     */
    private Map<String, Object> payload;
}

