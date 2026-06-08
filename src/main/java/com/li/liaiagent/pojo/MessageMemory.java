package com.li.liaiagent.pojo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

@Data
@TableName("message_memory")
public class MessageMemory {
    @TableId("conversation_id")
    String conversationId;

    @TableField(typeHandler = StoredMessageListTypeHandler.class,jdbcType = JdbcType.VARCHAR)
    private List<StoredMessage> messages;
}
