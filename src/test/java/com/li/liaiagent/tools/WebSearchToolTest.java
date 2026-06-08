package com.li.liaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {


    @Test
    void webSearch() {
        WebSearchTool webSearchTool = new WebSearchTool("yHagvDiVXqszY26Nq7Zf8xta");
        String s = webSearchTool.webSearch("如何提高自己的专业技能");
        Assertions.assertNotNull(s);
        System.out.println(s);
    }
}