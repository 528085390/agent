package com.li.liaiagent.demo.invoke;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;

import java.util.function.BiFunction;


// SpringAi是一个示例类，展示了如何在Spring框架中调用dashscope SDK进行文本生成。请确保已正确设置环境变量DASHSCOPE_API_KEY或直接在代码中替换为您的API Key。

public class SpringAi  {

    @Resource
    private ChatModel dashScopeChatModel;



    public void run(String... args) {
        AssistantMessage assistantMessage = dashScopeChatModel.call(new Prompt("你是谁"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());


    }


}
