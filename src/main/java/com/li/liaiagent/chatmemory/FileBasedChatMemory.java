package com.li.liaiagent.chatmemory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import com.esotericsoftware.kryo.kryo5.io.Input;
import org.springframework.ai.chat.messages.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_PATH;

    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemory(String basePath) {
        this.BASE_PATH = basePath;
        File file = new File(BASE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> existingMessages = getOrCreateConversation(conversationId);
        existingMessages.addAll(messages);
        saveConversation(conversationId, existingMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> existingMessages = getOrCreateConversation(conversationId);
        return existingMessages;
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void add(String conversationId, Message message) {
        saveConversation(conversationId,List.of(message));
    }


    /**
     * 获取或创造会话消息的列表
     *
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        ArrayList<Message> messages = new ArrayList<>();
        if (file.exists()) {
             try(Input input = new Input(new FileInputStream(file))){
                 Object object = kryo.readClassAndObject(input);
                 if (object instanceof ArrayList) {
                     messages = (ArrayList<Message>) object;
                 }
             }catch (Exception e) {
                 e.printStackTrace();
             }
        }
        return messages;
    }


    /**
     * 保存会话消息
     *
     * @param conversationId
     * @param messages
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try(Output output = new Output(new FileOutputStream(file))){
            kryo.writeClassAndObject(output, messages);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取会话文件
     *
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_PATH ,conversationId + ".kryo");
    }
}
