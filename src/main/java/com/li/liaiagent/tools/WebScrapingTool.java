package com.li.liaiagent.tools;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 网页抓取工具类
 */
public class WebScrapingTool {

    @Tool(description = "Scrape web content")
    public String scrapeWebPage(@ToolParam(description = "url of the web to scrape") String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.html();
        }catch (Exception e){
            return "Error in webScrapingTool: " + e.getMessage();
        }

    }
}
