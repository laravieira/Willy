package me.laravieira.willy.chat.openai;

import com.theokanning.openai.completion.CompletionResult;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OpenAiListener {
    public void onCompletionResponse(@NotNull CompletionResult result, UUID context) {
        String response = result.getChoices().get(0).getText().trim();
        String from = ContextStorage.of(context).getLastMessage().getFrom();

        Message message = new Message(context);
        message.setExpire(PassedInterval.DISABLE);
        message.setTo(from);
        message.setFrom(Willy.getWilly().getName());
        message.setContent(result.getChoices().get(0));
        message.setText(response);
        MessageStorage.add(message);

        ContextStorage.of(context).getSender().sendText(response);
    }
}
