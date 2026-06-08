package com.li.liaiagent.rag;


import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.stereotype.Component;

/**
 * 创建自定义的上下文查询增强器工厂类 空上下文时 返回特定模板
 */
@Component
public class AppContextualQueryAugmenterFactor {

    public static ContextualQueryAugmenter createInstance() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                你应该输出以下内容：
                抱歉，我只能回答恋爱相关话题 别的不相关话题我无法回答哦！
                有问题联系aaa
                """
        );

        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }
}
