package com.li.liaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 让自主规划智能体合理的终端
 */
public class TerminateTool {

    @Tool(description = """
            Terminate the interaction when the request id met or if the assistant cannot proceed further with the task.
            When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate() {
        return "任务结束";
    }
}
