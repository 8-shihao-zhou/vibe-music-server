package com.zsh.agentstudy.api.example;

//package com.zsh.agentstudy.api;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.function.BiFunction;

/**
 * 天气查询工具 - 用于获取指定城市的天气信息
 */
public class WeatherForLocationTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(
            @ToolParam(description = "城市名称") String city,
            ToolContext toolContext) {
        return "It's always sunny in " + city + "!";
    }

    /**
     * 创建天气查询工具的静态方法
     */
    public static FunctionToolCallback<?, ?> create() {
        return FunctionToolCallback
                .builder("getWeatherForLocation", new WeatherForLocationTool())
                .description("获取指定城市的天气信息")
                .inputType(String.class)
                .build();
    }
}
