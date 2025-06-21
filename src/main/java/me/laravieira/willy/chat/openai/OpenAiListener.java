package me.laravieira.willy.chat.openai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.Chat;
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

    public void whenCompletionComplete(@NotNull Chat chat) {
        Message message = new Message(context);
        message.setExpire(PassedInterval.DISABLE);

        if(chat.firstMessage().getToolCalls() == null || chat.firstMessage().getToolCalls().isEmpty()) {
            String from = ContextStorage.of(context).getLastMessage().getFrom();

            // Scape markdown, since Discord/Telegram/WhatsApp don't fully support it
            TextContentRenderer renderer = TextContentRenderer.builder().nodeRendererFactory(new TextContentNodeRendererLinkFactory()).build();
            Parser parser = Parser.builder().build();
            Node document = parser.parse(chat.firstContent());
            String text = renderer.render(document);

            message.setTo(from);
            message.setFrom(Willy.getWilly().getName());
            message.setContent(chat.firstMessage());
            message.setText(text);
            MessageStorage.add(message);

            ContextStorage.of(context).getUserSender().send(message);
            Willy.getLogger().fine(STR."OpenAI chat completion \{chat.getId()}");
        }else {
            message.setTo("SYSTEM");
            message.setFrom(Willy.getWilly().getName());
            message.setContent(chat.firstMessage());
            message.setText(chat.firstContent());
            MessageStorage.add(message);

            for(ToolCall call : chat.firstMessage().getToolCalls()) {
                // Inject the context id into the functions
                FunctionCall function = call.getFunction();
                JSONObject temp = JSON.parseObject(function.getArguments());
                temp.put("context", context);
                temp.put("call", call.getId());
                function.setArguments(temp.toJSONString());

                OpenAi.getExecutor().execute(function);
            }
        }

    }

    OpenAiListener(UUID context) {
        this.context = context;
    }
}
