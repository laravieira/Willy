package me.laravieira.willy.chat.openai.function;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import me.laravieira.willy.Context;
import me.laravieira.willy.WillyMessage;
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
        Context context = Context.of(this.context);
        Object last = context.getMessages().getLast().getContent();
        if(!(last instanceof ChatMessage.ResponseMessage)) {
            return "Last message is not a ResponseMessage.";
        } else if (((ChatMessage.ResponseMessage) last).getToolCalls().isEmpty()) {
            return "Last message is not a ResponseMessage with tool calls.";
        }
        ChatMessage.ToolMessage toolMessage = ChatMessage.ToolMessage.of(message, this.call);

        WillyMessage result = new WillyMessage(toolMessage);
        result.setFrom("SYSTEM");
        result.setExpire(PassedInterval.DISABLE);
        result.setText(toolMessage.getContent());
        context.process(result);

        return message;
    }
}
