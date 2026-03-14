package me.laravieira.willy.chat.openai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.Chat;
import me.laravieira.willy.Context;
import me.laravieira.willy.Willy;
import me.laravieira.willy.WillyMessage;
import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.utils.TextContentNodeRendererLinkFactory;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OpenAiListener {
    private final Context context;

    OpenAiListener(UUID context) {
        this.context = Context.of(context);
    }

    public void whenCompletionComplete(@NotNull Chat chat) {
        if(chat.firstMessage().getToolCalls() == null || chat.firstMessage().getToolCalls().isEmpty()) {
            // Scape markdown, since Discord/Telegram/WhatsApp don't fully support it
            TextContentRenderer renderer = TextContentRenderer.builder().nodeRendererFactory(new TextContentNodeRendererLinkFactory()).build();
            Parser parser = Parser.builder().build();
            Node document = parser.parse(chat.firstContent());
            String text = renderer.render(document);

            WillyMessage message = new WillyMessage(chat.firstMessage());
            message.setExpire(PassedInterval.DISABLE);
            message.setText(text);
            context.respond(message);
            Willy.getLogger().fine("OpenAI chat completion "+chat.getId());
        }else {
            WillyMessage message = new WillyMessage(chat.firstMessage());
            message.setFrom(Willy.getBrain().getName());
            message.setTo("SYSTEM");
            message.setText(chat.firstContent());
            context.append(message);

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
}
