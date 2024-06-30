package me.laravieira.willy.chat.openai;

import io.github.sashirestela.openai.domain.chat.Chat;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OpenAiListener {
    private final UUID context;

    public static void whenCompletionComplete(@NotNull Chat chat, Throwable throwable, UUID context) {
        if(throwable != null) {
            Willy.getLogger().warning(STR."Error on OpenAI chat completion: \{throwable.getMessage()}");
            return;
        }
        new OpenAiListener(context).onCompletionResponse(chat);
    }

    OpenAiListener(UUID context) {
        this.context = context;
    }

    public void onCompletionResponse(@NotNull Chat chat) {
        String from = ContextStorage.of(context).getLastMessage().getFrom();

        Message message = new Message(context);
        message.setExpire(PassedInterval.DISABLE);
        message.setTo(from);
        message.setFrom(Willy.getWilly().getName());
        message.setContent(chat.firstMessage());
        message.setText(chat.firstContent());
        MessageStorage.add(message);

        ContextStorage.of(context).getSender().sendText(chat.firstContent());
    }
}
