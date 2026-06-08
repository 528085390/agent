package com.li.liaiagent.app;

import com.li.liaiagent.rag.AppDocumentLoader;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Resource
    private AppDocumentLoader appDocumentLoader;

    @Resource
    private VectorStore vectorStore;

    @Test
    void TestDoChat() {
        String conversationId = UUID.randomUUID().toString();
        System.out.println("=========================================================================================");
        System.out.println("conversationId: " + conversationId);

        // 1轮
        String question = "我是uyh 我喜欢一个人，但不知道怎么表白，你有什么建议吗？";
        String answer = loveApp.doChat(question, conversationId);
        Assertions.assertNotNull(answer);


        // 2轮
        question = "zz是我喜欢的人，我们经常一起吃饭，但我不知道怎么进一步发展关系，你有什么建议吗？";
        answer = loveApp.doChat(question, conversationId);
        Assertions.assertNotNull(answer);


        // 3轮
        question = "我喜欢的人是谁 帮我回忆？";
        answer = loveApp.doChat(question, conversationId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void TestDoChat2() {
        String conversationId = UUID.randomUUID().toString();
        System.out.println("=========================================================================================");
        System.out.println("conversationId: " + conversationId);

        // 1轮
        String question = "我喜欢一个人，但不知道怎么表白，你有什么建议吗？";
        String answer = loveApp.doChat(question, conversationId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String conversationId = UUID.randomUUID().toString();
        System.out.println("=========================================================================================");

        // 1轮
        String question = "我是yut zz是我喜欢的人，我们经常一起吃饭，但我不知道怎么进一步发展关系，你有什么建议吗？";
        LoveApp.Report report = loveApp.doChatWithReport(question, conversationId);
        Assertions.assertNotNull(report);
    }

    @Test
    void doChatWithRag() {
        String conversationId = UUID.randomUUID().toString();
        System.out.println("=========================================================================================");

        String question = "婚后关系不和谐怎么办";
        String answer = loveApp.doChatWithRag(question, conversationId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void loadMarkdowns() {
        vectorStore.add(appDocumentLoader.loadMarkdowns());
    }

    @Test
    void doChatWithTools() {

//        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");


//        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");


//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");


//        testMessage("执行 Python3 脚本来生成数据分析报告,分析不同人群中星座和年龄的分布");


//        testMessage("保存我的恋爱档案为文件");


        testMessage("生成一份‘七夕约会计划.PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String question = "广州有什么约会地点";
        question = "帮我找一下广州塔的图片";
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithMcp(question, chatId);
        Assertions.assertNotNull(answer);


    }
}