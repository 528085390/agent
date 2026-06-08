package com.li.liimagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImageSearchTool {

    @Value("${pexels.api-key}")
    private String API_KEY;

    @Value("${pexels.base-url:https://api.pexels.com/v1/search}")
    private String BASE_URL;


    @Tool(description = "search images from web by the keyword")
    public String searchImage(@ToolParam(description = "the query keyword") String query){
        try{
            return doSearchImage(query);
        }catch (Exception e){
            return  "Error search image: " + e.getMessage();
        }
    }


    public String doSearchImage(String query) {

        if (StrUtil.isBlank(query)) {
            throw new IllegalArgumentException("query 不能为空");
        }
        if (StrUtil.isBlank(API_KEY)) {
            throw new IllegalStateException("pexels.api-key 未配置");
        }


        try (HttpResponse response = HttpRequest.get(BASE_URL)
                .header("Authorization", API_KEY)
                .header("Accept", "application/json")
                .form("query", query)
                .charset(StandardCharsets.UTF_8)
                .timeout(10000)
                .execute()) {

            if (!response.isOk()) {
                throw new RuntimeException("调用图片搜索接口失败，状态码：" + response.getStatus());
            }

            JSONObject body = JSONUtil.parseObj(response.body());
            JSONArray photos = body.getJSONArray("photos");
            List<String> result = photos.stream()
                    .map(photoObj -> (JSONObject) photoObj)
                    .map(photoObj -> photoObj.getJSONObject("src"))
                    .map(photo -> photo.getStr("medium"))
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());

            return String.join(",", result);
        }
    }
}