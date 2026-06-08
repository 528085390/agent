package com.li.liaiagent.chatmemory;

import com.li.liaiagent.mapper.MessageMemoryMapper;
import com.li.liaiagent.pojo.MessageMemory;
import com.li.liaiagent.pojo.StoredMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostgresMemory implements ChatMemory {

    @Autowired
    private MessageMemoryMapper messageMemoryMapper;

    @Override
    public void add(String conversationId, Message message) {
        MessageMemory messageMemory = getOrCreateConversation(conversationId);
        List<StoredMessage> messages = messageMemory.getMessages();
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(toStoredMessage(message));
        messageMemory.setMessages(messages);
        messageMemoryMapper.updateById(messageMemory);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        MessageMemory messageMemory = getOrCreateConversation(conversationId);
        List<StoredMessage> existingMessages = messageMemory.getMessages();
        for (Message message : messages) {
            existingMessages.add(toStoredMessage(message));
        }
        messageMemory.setMessages(existingMessages);
        messageMemoryMapper.updateById(messageMemory);
    }

    @Override
    public List<Message> get(String conversationId) {
        MessageMemory messageMemory = getOrCreateConversation(conversationId);
        return messageMemory.getMessages().stream()
                .map(this::toSpringMessage)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        messageMemoryMapper.deleteById(conversationId);
    }

    public MessageMemory getOrCreateConversation(String conversationId) {
        MessageMemory messageMemory = messageMemoryMapper.selectById(conversationId);
        if (messageMemory == null) {
            messageMemory = new MessageMemory();
            messageMemory.setConversationId(conversationId);
            messageMemory.setMessages(new ArrayList<>());
            messageMemoryMapper.insert(messageMemory);
        } else {
            // 如果 DB 返回的对象 messages 为 null，初始化为空列表，防止后续 add 时 NPE 或覆盖历史
            if (messageMemory.getMessages() == null) {
                messageMemory.setMessages(new ArrayList<>());
            }
        }
        return messageMemory;
    }

    private StoredMessage toStoredMessage(Message message) {
        StoredMessage stored = new StoredMessage();
        stored.setContent(message.getText());
        if (message instanceof SystemMessage) {
            stored.setType("system");
        } else if (message instanceof UserMessage) {
            stored.setType("user");
        } else if (message instanceof AssistantMessage) {
            stored.setType("assistant");
        } else {
            stored.setType("unknown");
        }
        return stored;
    }

    private Message toSpringMessage(StoredMessage storedMessage) {
        return switch (storedMessage.getType()) {
            case "system" -> new SystemMessage(storedMessage.getContent());
            case "assistant" -> new AssistantMessage(storedMessage.getContent());
            case "user" -> new UserMessage(storedMessage.getContent());
            default -> new UserMessage(storedMessage.getContent());
        };
    }
}