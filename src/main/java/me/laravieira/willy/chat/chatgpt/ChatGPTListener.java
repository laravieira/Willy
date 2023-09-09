package me.laravieira.willy.chat.chatgpt;

import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChatGPTListener {
    public void onCompletionResponse(@NotNull String response, UUID context) {
        String from = ContextStorage.of(context).getLastMessage().getFrom();

        Message message = new Message(context);
        message.setExpire(PassedInterval.DISABLE);
        message.setTo(from);
        message.setFrom(Willy.getWilly().getName());
        message.setContent(response);
        message.setText(response);
        MessageStorage.add(message);

        ContextStorage.of(context).getUserSender().sendText(response);
    }
}
