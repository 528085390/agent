package com.li.liaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.li.liaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/*
    抽象基础代理类，用于管理状态和执行状态流程

    提供状态转换,内存管理和基于步骤的循环执行的基础功能
    子类不惜实现step方法
 */
@Data
@Slf4j
public abstract class BaseAgent {

    private String name;

    private String systemPrompt;
    private String nextStepPrompt;

    private AgentState state = AgentState.IDLE;

    private int currentStep = 0;
    private int maxSteps = 0;

    // 大模型
    private ChatClient chatClient;

    // Memory 自主维护上下文
    private List<Message> messageList = new ArrayList<>();


    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        // 状态转换
        this.state = AgentState.RUNNING;
        // 记录上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();

        try {
            // 执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add(("Terminated Reached max steps: " + maxSteps));
            }
            return String.join(",", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error running agent", e);
            return "执行错误: " + e.getMessage();

        } finally {
            this.cleanup();
        }
    }

    /**
     * 运行代理，流式输出
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public SseEmitter runStream(String userPrompt) {

        // 创建SseEmitter对象，超时3分钟
        SseEmitter sseEmitter = new SseEmitter(3000000L);
        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("Cannot run agent from state: " + this.state);
                    sseEmitter.completeWithError(new RuntimeException("Cannot run agent from state: " + this.state));
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("Cannot run agent with empty user prompt");
                    sseEmitter.completeWithError(new RuntimeException("Cannot run agent with empty user prompt"));
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }

            // 状态转换
            this.state = AgentState.RUNNING;
            // 记录上下文
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();

            try {
                // 执行循环
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step {}/{}", stepNumber, maxSteps);
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);
                    // 输出当前每一步的结果到sse
                    sseEmitter.send(result);
                }
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    results.add(("Terminated Reached max steps: " + maxSteps));
                    sseEmitter.send("执行结束：达到最大步骤数: " + maxSteps);
                }
                // 正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error running agent", e);
                try {
                    sseEmitter.send("执行错误: " + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                this.cleanup();
                sseEmitter.complete();
            }
        });

        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (this.state != AgentState.FINISHED) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return sseEmitter;
    }

    /**
     * 单个步骤
     *
     * @return
     */
    public abstract String step();

    protected void cleanup() {
        // 子类重写方法清理资源
    }
}
