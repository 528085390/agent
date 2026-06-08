package com.li.liaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * 终端操作工具类
 * 用于执行系统命令并返回结果
 */
public class TerminalOperationTool {


    /**
     * 执行终端命令
     *
     * @param command 要执行的命令
     * @return 命令执行结果（标准输出和错误输出）
     */
    @Tool(description = "Execute a system command in the terminal and return the output. Use with caution as it can modify system state.")
    public String executeCommand(@ToolParam(description = "The system command to execute") String command) {
        if (command == null || command.trim().isEmpty()) {
            return "Error: Command cannot be empty";
        }

        command = command.trim();
        StringBuilder result = new StringBuilder();
        try {
            ProcessBuilder precessBuilder = new ProcessBuilder("cmd", "/c", command);
            Process process = precessBuilder.start();


            // 读取标准输出
            String stdout = readStream(process.getInputStream());
            if (!stdout.isEmpty()) {
                result.append("Output:\n").append(stdout);
            }

            // 读取错误输出
            String stderr = readStream(process.getErrorStream());
            if (!stderr.isEmpty()) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append("Error Output:\n").append(stderr);
            }

            int exitCode = process.exitValue();
            result.insert(0, "Exit Code: " + exitCode + "\n");

            return result.toString();

        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
    }

    /**
     * 读取输入流内容
     */
    private String readStream(java.io.InputStream inputStream) throws Exception {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }

}
