package com.li.liaiagent.agent;

import com.li.liaiagent.advisor.MyLoggerAdvisor;
import jdk.jfr.Category;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 超级智能体，拥有自主规划能力，能够根据用户需求自主规划行动步骤，调用工具获取信息并执行操作，直到完成任务
 */
@Component
public class LiAgent extends ToolCallAgent{

    public LiAgent(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("LiAgent");

        String systemPrompt = "你是一个AI助手，你的名字是LiAgent，你的任务是帮助用户完成任务";
        this.setSystemPrompt(systemPrompt);

        String nextStepPrompt = "请根据用户的需求，决定下一步行动。你可以选择调用工具来获取信息或执行操作，或者直接给出答案。如果需要调用工具，请明确说明要调用哪个工具以及传递什么参数。";
        this.setNextStepPrompt(nextStepPrompt);

        this.setMaxSteps(20);

        //初始化大模型
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
//                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
