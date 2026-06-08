package com.li.liaiagent.app;


import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.li.liaiagent.advisor.MyLoggerAdvisor;
import com.li.liaiagent.advisor.ReReadingAdvisor;
import com.li.liaiagent.chatmemory.FileBasedChatMemory;
import com.li.liaiagent.chatmemory.PostgresMemory;
import com.li.liaiagent.rag.AppDocumentLoader;
import com.li.liaiagent.rag.AppRagCustomAdvisorFactory;
import com.li.liaiagent.rag.QueryRewriter;
import dev.langchain4j.service.V;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private ChatMemory chatMemory;

    @Autowired
    private AppDocumentLoader appDocumentLoader;

    private static final String SYSTEM_PROMPT = "你是一个恋爱专家，帮助用户解决恋爱中的问题。";


    public LoveApp(ChatModel dashscopeChatModel, ChatMemory chatMemory) {
        this.chatMemory = chatMemory;

        // 基于文件的聊天记录
//        String filePath = "./tmp/chat_memory";
//        chatMemory = new FileBasedChatMemory(filePath);
//        InMemoryChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();

        // 基于内存的聊天记录
//        chatMemory = MessageWindowChatMemory.builder()
//                .maxMessages(10)
//                .build();


        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build(),
                        // 日志拦截器，记录每次调用的请求和响应
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * description: 基础聊天
     *
     * @param question       用户输入的问题
     * @param conversationId 会话ID，用于关联聊天记录
     */
    public String doChat(String question, String conversationId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId).param("maxMessages", 5))
                .advisors(new ReReadingAdvisor())
                .call()
                .chatResponse();
        String text = chatResponse.getResult().getOutput().getText();
        return text;
    }

    record Report(String title, List<String> suggestions) {
    }

    /**
     * description: 流式聊天，用于实时返回聊天结果
     *
     * @param question       用户输入的问题
     * @param conversationId 会话ID，用于关联聊天记录
     */
    public Flux<String> doChatByStream(String question, String conversationId) {
        Flux<String> response = chatClient
                .prompt()
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId).param("maxMessages", 5))
                .advisors(new ReReadingAdvisor())
                .stream()
                .content();
        return response;
    }


    /**
     * description: 带报告功能的聊天,格式化输出
     *
     * @param question       用户输入的问题
     * @param conversationId 会话ID，用于关联聊天记录
     */
    public Report doChatWithReport(String question, String conversationId) {
        Report chatResponse = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId).param("maxMessages", 5))
                .advisors(new ReReadingAdvisor())
                .call()
                .entity(Report.class);
        return chatResponse;
    }


    @Resource
    private VectorStore vectorStore; // pg向量数据库

    @Resource
    private VectorStore appVectorStore; // 内存

    @Resource
    private Advisor appRagCloudAdvisor; // rag检索增强 云知识库

    @Resource
    private QueryRewriter queryRewriter; // 查询重写工具，将用户的问题进行改写以适应检索需求

    /**
     * description: 带rag功能的聊天
     *
     * @param question       用户输入的问题
     * @param conversationId 会话ID，用于关联聊天记录
     */
    public String doChatWithRag(String question, String conversationId) {

        // 查询重写
        question = queryRewriter.doQueryRewrite(question);

        String answer = chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId).param("maxMessages", 5))

                //  应用rag 内存向量数据库
//                .advisors(QuestionAnswerAdvisor.builder(appVectorStore).build())

                // 应用rag检索增强 云知识库服务
//                .advisors(appRagCloudAdvisor)

                // 应用rag检索增强 pgvector服务 可选本地也可选云数据库
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())

                // 应用自定义的rag检索增强 文档查询器 + 上下文增强
//                .advisors(AppRagCustomAdvisorFactory.createAppRagCustomAdvisor(vectorStore, "单身"))

                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        return answer;

    }


    @Resource
    private ToolCallback[] allTools;

    /**
     * description: 带tools的聊天
     *
     * @param question       用户输入的问题
     * @param conversationId 会话ID，用于关联聊天记录
     */
    public String doChatWithTools(String question, String conversationId) {
        String result = chatClient
                .prompt()
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId).param("maxMessages", 5))
                .toolCallbacks(allTools)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        return result;
    }

    // m
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * description: 带mcp的聊天，使用ToolCallbackProvider动态提供mcp服务
     *
     * @param question       用户输入的问题
     * @param conversationId 会话ID，用于关联聊天记录
     */
    public String doChatWithMcp(String question, String conversationId) {
        String result = chatClient
                .prompt()
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId).param("maxMessages", 5))
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        return result;
    }
}
