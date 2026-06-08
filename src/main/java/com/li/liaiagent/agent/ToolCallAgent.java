package com.li.liaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.li.liaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了think act方法 可以用作创作实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 可用工具
    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应结果 要调用的工具
    private ChatResponse toolCallResponse;

    // 工具调用管理
    private final ToolCallingManager toolCallingManager;

    // 禁用 StringAi 内置的工具调用，自己维护选项和信息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }


    /**
     * 处理并执行，决定是否下一步行动
     *
     * @return
     */
    @Override
    public boolean think() {
        try {
            // 校验提示词，拼接用户提示词
            if (StrUtil.isNotBlank(getNextStepPrompt())) {
                UserMessage userMessage = new UserMessage(getNextStepPrompt());
                getMessageList().add(userMessage);
            }

            List<Message> messageList = getMessageList();
            Prompt prompt = Prompt.builder()
                    .messages(messageList)
                    .chatOptions(chatOptions)
                    .build();
            // 调用 AI 模型，获取工具调用信息
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            // 解析工具调用信息，获得要调用的工具
            // 记录响应，用于act
            this.toolCallResponse = chatResponse;
            // 助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallsList = assistantMessage.getToolCalls();
            // 输出提示信息
            String result = assistantMessage.getText();
            log.info("{} AI 模型的思考: {}", getName(), result);
            log.info("{} AI 模型要调用的工具和参数: {}", getName(),
                    toolCallsList.stream()
                            .map(toolCall -> String.format("工具名称: %s, 参数: %s", toolCall.name(), toolCall.arguments()))
                            .collect(Collectors.joining("\n"))
            );
            // 如果没有工具调用信息，说明不需要调用工具，返回false
            if (toolCallsList.isEmpty()) {
                // 只有不调用工具时候才需要记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 调用工具时候不需要记录助手消息，工具调用时候会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error("{} 思考过程失败: {}", getName(), e.getMessage(), e);
            getMessageList().add(new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    /**
     * 执行工具调用
     *
     * @return
     */
    @Override
    public String act() {
        if (!toolCallResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }

        // 调用工具
        Prompt prompt = Prompt.builder()
                .messages(getMessageList())
                .chatOptions(chatOptions)
                .build();
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallResponse);

        // 记录工具调用结果到上下文中 conversationHistory包含了工具调用的上下文和结果，已经按照消息的形式组织好了，可以直接设置到messageList中
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> toolResponse.name().equals("doTerminate"));
        if (terminateToolCalled) {
            // 任务结束
            setState(AgentState.FINISHED);
        }
        String result = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具调用结果 - " + toolResponse.name() + ": " + toolResponse.responseData())
                .collect(Collectors.joining("\n"));
        log.info(result);
        return result;
    }
}
