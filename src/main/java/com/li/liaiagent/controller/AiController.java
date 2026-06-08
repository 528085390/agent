package com.li.liaiagent.controller;

import com.li.liaiagent.agent.LiAgent;
import com.li.liaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    /**
     * 同步调用应用
     *
     * @param question
     * @param conversationId
     * @return
     */
    @GetMapping("/app/chat/sync")
    public String doChatWithAppSync(String question, String conversationId) {
        return loveApp.doChat(question, conversationId);
    }


    /**
     * SSE流式调用应用
     *
     * @param question
     * @param conversationId
     * @return
     */
    @GetMapping(value = "/app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithAppSSE(String question, String conversationId) {
        return loveApp.doChatByStream(question, conversationId);
    }


    /**
     * ServerSentEvent 流式调用应用
     *
     * @param question
     * @param conversationId
     * @return
     */
    @GetMapping(value = "/app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithAppServerSentEvent(String question, String conversationId) {
        return loveApp.doChatByStream(question, conversationId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }


    @GetMapping(value = "/app/chat/sseEmitter")
    public SseEmitter doChatWithAppServerSseEmitter(String question, String conversationId) {
        // 创建SseEmitter对象，超时时间三分钟
        SseEmitter sseEmitter = new SseEmitter(180000L);
        // 订阅流式数据，并通过订阅推送给SseEmitter
        loveApp.doChatByStream(question, conversationId)
                .subscribe(
                        chunk -> {
                            try {
                                sseEmitter.send(chunk);
                            } catch (Exception e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        sseEmitter::completeWithError,
                        sseEmitter::complete
                );
        return sseEmitter;
    }

    /**
     * 流式调用超级智能体
     *
     * @param question
     * @return
     */
    @GetMapping("/agent/chat")
    public SseEmitter doChatWithAgent(String question) {
        LiAgent liagent = new LiAgent(allTools, dashscopeChatModel);
        return liagent.runStream(question);
    }
}
