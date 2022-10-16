package me.laravieira.willy.chat.bloom;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.utils.WillyUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

public class BloomListener {
    private final UUID context;

    BloomListener(UUID context) {
        this.context = context;
    }

    public void onResponse(@NotNull Process response, Throwable throwable) {
        String data = response.inputReader().lines().collect(Collectors.joining());
        Willy.getLogger().info("Bloom data: " + data);

        JSONObject json = JSONArray.parseArray(data).getJSONObject(0);
        if(json != null && json.containsKey("generated_text"))
            this.onMessage(json.getString("generated_text"));
    }

    public void onMessage(String response) {
        Message last = ContextStorage.of(this.context).getLastMessage();
        BloomHeader headerBuilder = new BloomHeader(context);
        LinkedList<UUID> messages = ContextStorage.of(context).getMessages();
        String prompt = headerBuilder.build()+ WillyUtils.buildConversation(messages, Bloom.HISTORY_SIZE);

        response = response.substring(prompt.length(), response.length()-1).trim();
        response = response.split(last.getTo() + ": ")[0];
        response = response.split(last.getFrom() + ": ")[0];
        response = response.trim();

        Willy.getLogger().info("Bloom response: " + response);

        Message message = new Message(this.context);
        message.setExpire(PassedInterval.DISABLE);
        message.setTo(last.getFrom());
        message.setFrom(Willy.getWilly().getName());
        message.setContent(response);
        message.setText(response);
        MessageStorage.add(message);

        ContextStorage.of(this.context).getSender().sendText(response);
    }
}
