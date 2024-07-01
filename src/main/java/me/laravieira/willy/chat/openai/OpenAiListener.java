package me.laravieira.willy.chat.openai;

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

    public static void whenCompletionComplete(@NotNull Chat chat, Throwable throwable, UUID context) {
        if(throwable != null) {
            Willy.getLogger().warning(STR."Error on OpenAI chat completion: \{throwable.getMessage()}");
            return;
        }
        if(chat.firstMessage().getToolCalls() == null || chat.firstMessage().getToolCalls().isEmpty()) {
            Willy.getLogger().fine(STR."OpenAI chat completion \{chat.getId()}");
            new OpenAiListener(context).onCompletionResponse(chat);
            return;
        }

        for(ToolCall call : chat.firstMessage().getToolCalls()) {
            Message response = new Message(context);
            response.setExpire(PassedInterval.DISABLE);
            response.setTo("SYSTEM");
            response.setFrom(Willy.getWilly().getName());
            response.setContent(chat.firstMessage());
            response.setText(chat.firstContent());
            MessageStorage.add(response);

            Object result = OpenAi.getExecutor().execute(call.getFunction());
            ToolMessage toolMessage = ToolMessage.of(result.toString(), call.getId());

            Message function = new Message(context);
            function.setExpire(PassedInterval.DISABLE);
            function.setTo(Willy.getWilly().getName());
            function.setFrom("SYSTEM");
            function.setContent(toolMessage);
            function.setText(toolMessage.getContent());
            MessageStorage.add(function);

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

        ContextStorage.of(context).getUserSender().sendText(text);
    }
}
