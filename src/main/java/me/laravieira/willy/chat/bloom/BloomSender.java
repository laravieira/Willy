package me.laravieira.willy.chat.bloom;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.openai.OpenAi;
import me.laravieira.willy.chat.openai.OpenAiHeader;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BloomSender implements SenderInterface {
    private final UUID context;

    public BloomSender(UUID context) {
        this.context = context;
    }

    public String buildConversation(@NotNull LinkedList<UUID> messages) {
        StringBuilder conversation = new StringBuilder();
        LinkedList<UUID> lastMessages = new LinkedList<>(messages);
        LinkedList<Message> descendingHistory = new LinkedList<>();
        for(int i = 0; i < lastMessages.size() && i < OpenAi.HISTORY_SIZE; i++)
            descendingHistory.add(MessageStorage.of(lastMessages.pollLast()));
        Iterator<Message> history = descendingHistory.descendingIterator();
        while(history.hasNext()) {
            Message message = history.next();
            conversation.append(message.getFrom());
            conversation.append(": ");
            conversation.append(message.getText());
            conversation.append("\r\n");
        }
        conversation.append(Willy.getWilly().getName());
        conversation.append(": ");
        return conversation.toString();
    }

    public void onResponse(@NotNull Process response, Throwable throwable) {
        String data = response.inputReader().lines().collect(Collectors.joining());
        JSONObject json = JSONArray.parseArray(data).getJSONObject(0);
        if(json != null && json.containsKey("generated_text"))
            new BloomListener().onMessage(json.getString("generated_text"), context);
    }

    public void sendPrompt(String prompt) {
        if(!new Bloom().isConnected())
            return;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", Bloom.BLOOM_PATH.toString(), "-c", prompt);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            process.onExit().whenComplete(this::onResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(Object message) {

    }

    @Override
    public void sendText(String message) {
        BloomHeader headerBuilder = new BloomHeader(context);
        LinkedList<UUID> messages = ContextStorage.of(context).getMessages();
        List<String> stopList = new ArrayList<>();
        stopList.add("\r\n" + ContextStorage.of(context).getLastMessage().getFrom() + ": ");
        stopList.add("\r\n" + ContextStorage.of(context).getLastMessage().getTo() + ": ");
        String prompt = headerBuilder.build()+buildConversation(messages);
        sendPrompt(prompt);
    }

    @Override
    public void sendLink(Message message) {

    }

    @Override
    public void sendStick(Message message) {

    }

    @Override
    public void sendGif(Message message) {

    }

    @Override
    public void sendImage(Message message) {

    }

    @Override
    public void sendVideo(Message message) {

    }

    @Override
    public void sendAudio(Message message) {

    }

    @Override
    public void sendLocation(Message message) {

    }

    @Override
    public void sendContact(Message message) {

    }

    @Override
    public void sendFile(File message) {

    }
}
