package com.li.liaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.li.liaiagent.tools.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类 读 写
 */
public class FileOperationTool {

    private final String FILE_PATH = FileConstant.FILE_SAVE_PATH + "/file";

    @Tool(description = "read content from file")
    public String readFile(@ToolParam(description = "name of a file to read") String fileName) {
        String filePath = FILE_PATH + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }

    }

    @Tool(description = "write content to file")
    public String writeFile(@ToolParam(description = "name of a file to write") String fileName,
                            @ToolParam(description = "content to write to the file") String content) {
        String filePath = FILE_PATH + "/" + fileName;
        try {
            FileUtil.mkdir(FILE_PATH);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to " + filePath;
        } catch (Exception e) {
            return "Error writing to file: " + e.getMessage();
        }
    }
}
