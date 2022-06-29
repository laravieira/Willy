package me.laravieira.willy.chat.openai;

import com.theokanning.openai.completion.CompletionResult;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OpenAiListener {
    public void onCompletionResponse(@NotNull CompletionResult result, UUID context) {
        String text = result.getChoices().get(0).getText();
        String from = ContextStorage.of(context).getLastMessage().getFrom();
        String response = text.substring(text.indexOf(":")+1).trim();

        Message message = new Message(context);
        message.setExpire(PassedInterval.DISABLE);
        message.setTo(from);
        message.setFrom("Willy");
        message.setContent(text);
        message.setText(response);
        MessageStorage.add(message);

        ContextStorage.of(context).getSender().sendText(response);
    }
}
