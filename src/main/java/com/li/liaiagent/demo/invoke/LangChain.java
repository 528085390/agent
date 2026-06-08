package com.li.liaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;

public class LangChain {

    public static void main(String[] args) {
        QwenChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(ApiKey.API_KEY)
                .modelName("qwen-plus")
                .build();

        String answer = qwenChatModel.chat("你是谁？");
        System.out.println( answer);


    }
}
