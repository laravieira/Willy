package me.laravieira.willy.chat.openai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage.ToolMessage;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.utils.TextContentNodeRendererLinkFactory;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OpenAiListener {
    private final UUID context;

    public static void whenCompletionComplete(@NotNull Chat chat, UUID context) {
        if(chat.firstMessage().getToolCalls() == null || chat.firstMessage().getToolCalls().isEmpty()) {
            Willy.getLogger().fine(STR."OpenAI chat completion \{chat.getId()}");
            new OpenAiListener(context).onCompletionResponse(chat);
            return;
        }

        Message response = new Message(context);
        response.setExpire(PassedInterval.DISABLE);
        response.setTo("SYSTEM");
        response.setFrom(Willy.getWilly().getName());
        response.setContent(chat.firstMessage());
        response.setText(chat.firstContent());
        MessageStorage.add(response);

        for(ToolCall call : chat.firstMessage().getToolCalls()) {
            // Inject the context id into the functions
            FunctionCall function = call.getFunction();
            JSONObject temp = JSON.parseObject(function.getArguments());
            temp.put("context", context);
            function.setArguments(temp.toJSONString());

            Object object = OpenAi.getExecutor().execute(function);
            ToolMessage toolMessage = ToolMessage.of(object.toString(), call.getId());

            Message result = new Message(context);
            result.setExpire(PassedInterval.DISABLE);
            result.setTo(Willy.getWilly().getName());
            result.setFrom("SYSTEM");
            result.setContent(toolMessage);
            result.setText(toolMessage.getContent());
            MessageStorage.add(result);

            Willy.getLogger().fine(STR."OpenAI tool call \{call.getId()}");
            ContextStorage.of(context).getSender().sendText(result.toString());
        }
    }

    OpenAiListener(UUID context) {
        this.context = context;
    }

    public void onCompletionResponse(@NotNull Chat chat) {
        String from = ContextStorage.of(context).getLastMessage().getFrom();

        // Scape markdown, since Discord/Telegram/WhatsApp don't fully support it
        TextContentRenderer renderer = TextContentRenderer.builder().nodeRendererFactory(new TextContentNodeRendererLinkFactory()).build();
        Parser parser = Parser.builder().build();
        Node document = parser.parse(chat.firstContent());
        String text = renderer.render(document);

        Message message = new Message(context);
        message.setExpire(PassedInterval.DISABLE);
        message.setTo(from);
        message.setFrom(Willy.getWilly().getName());
        message.setContent(chat.firstMessage());
        message.setText(text);
        MessageStorage.add(message);

        ContextStorage.of(context).getUserSender().send(message);
    }
}
