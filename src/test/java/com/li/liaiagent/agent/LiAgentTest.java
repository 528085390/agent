package com.li.liaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Profile("local")
class LiAgentTest {

    @Resource
    private LiAgent liAgent;

    @Test
    public void run(){
        String userPrompt = """
                我的另一半在广州海珠区，帮我找到赤岗 5km 以内的适合的约会地点，
                结合一些网络图片，制定详细的约会计划，要求包含餐厅、咖啡厅、公园等不同类型的地点，并且每个地点都要有图片链接和简短的介绍。
                以pdf格式输出
                """;
        String result = liAgent.run(userPrompt);
        Assertions.assertNotNull(result);
    }


}