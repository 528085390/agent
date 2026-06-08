package com.li.liaiagent.tools;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web 搜索工具
 */
public class WebSearchTool {

    private final HttpClient client = HttpClient.newHttpClient();

    private String engine = "baidu"; // default engine

    private final String  apiKey;

    public WebSearchTool(@Value("${webSearchTool.apiKey}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search information on the web")
    public String webSearch(@ToolParam(description = "search query") String query) {
        if(apiKey == null || apiKey.isBlank()) return "Error in webSearchTool: missing API key";
        if (query == null || query.isBlank()) return "Error in webSearchTool: missing query";
        if (engine == null || engine.isBlank()) engine = "baidu";

        try {
            String url = "https://www.searchapi.io/api/v1/search"
                    + "?api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                    + "&engine=" + URLEncoder.encode(engine, StandardCharsets.UTF_8)
                    + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            String body = resp.body();

            // 处理响应 解析前五条数据
            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONArray results = jsonObject.getJSONArray("organic_results");
            List<Object> objects = results.subList(0, 5);

            // 构建结果字符串
            String result = objects.stream()
                    .map(obj ->{
                        JSONObject jsonObj = (JSONObject) obj;
                        return jsonObj.toString();
                    }).collect(Collectors.joining(","));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error in webSearchTool: " + e.getMessage();
        }
    }
}