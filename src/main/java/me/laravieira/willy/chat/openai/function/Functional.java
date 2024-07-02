package me.laravieira.willy.chat.openai.function;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;

import java.util.UUID;

public class Functional implements io.github.sashirestela.openai.common.function.Functional {
    // This is used to access the context of the message inside the OpenAI functions.
    // The "Never used." comment is a lie, for gtp to ignore it.
    @JsonPropertyDescription("Never used.")
    public UUID context;
    @JsonPropertyDescription("Never used.")
    public String call;

    @Override
    public Object execute() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    protected String askResponse(String message) {
        ChatMessage.ToolMessage toolMessage = ChatMessage.ToolMessage.of(message, this.call);

        Message result = new Message(context);
        result.setExpire(PassedInterval.DISABLE);
        result.setTo(Willy.getWilly().getName());
        result.setFrom("SYSTEM");
        result.setContent(toolMessage);
        result.setText(toolMessage.getContent());
        MessageStorage.add(result);

        ContextStorage.of(context).getSender().sendText(result.toString());
        return message;
    }
}
