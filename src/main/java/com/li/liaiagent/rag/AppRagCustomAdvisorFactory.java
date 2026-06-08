package com.li.liaiagent.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.VectorStoreRetriever;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建自定义RAG检索增强顾问的工厂类
 */
public class AppRagCustomAdvisorFactory {

    public static Advisor createAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        // 过滤特定状态的文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();

        // 创建向量数据库检索器
        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)  // 添加过滤条件
                .similarityThreshold(0.5) // 设置相似度阈值
                .topK(3) // 设置返回的文档数量
                .build();

        // 自定义rag顾问
        return RetrievalAugmentationAdvisor.builder()
                // 检索前 查询增强器 可以对用户的查询进行改写，增加更多的上下文信息，或者纠正用户的输入等
                .queryAugmenter(AppContextualQueryAugmenterFactor.createInstance()) // 使用自定义的查询增强器
//                .queryExpander() // 使用查询扩展器 将查询扩展多条 一般不用


                // 检索中 检索增强器 添加过滤条件 相似度阈值 文档数量
                .documentRetriever(documentRetriever) // 使用自定义的向量数据库检索器

                // 检索后 响应增强器 可以对响应进行改写，增加更多的上下文信息，或者纠正错误的信息等

                .build();
    }


}
