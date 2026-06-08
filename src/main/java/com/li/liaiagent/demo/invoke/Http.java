package com.li.liaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class Http {

    public static String callDashScopeApi(String apiKey) {
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 构建消息数组
        JSONArray messages = new JSONArray();
        messages.add(new JSONObject()
                .set("role", "system")
                .set("content", "You are a helpful assistant."));
        messages.add(new JSONObject()
                .set("role", "user")
                .set("content", "你是谁？"));

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "qwen-plus");
        requestBody.set("input", new JSONObject().set("messages", messages));
        requestBody.set("parameters", new JSONObject().set("result_format", "message"));

        // 发送 POST 请求
        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .execute();

        // 获取响应结果
        if (response.isOk()) {
            return response.body();
        } else {
            throw new RuntimeException("请求失败，状态码：" + response.getStatus() + "，响应：" + response.body());
        }
    }

    public static void main(String[] args) {
        String apiKey = ApiKey.API_KEY;
        String result = callDashScopeApi(apiKey);
        System.out.println(result);
    }
}
