package me.laravieira.willy.chat.openai;

import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import me.laravieira.willy.WillyChannel;
import me.laravieira.willy.Willy;
import me.laravieira.willy.WillyMessage;
import me.laravieira.willy.Context;
import me.laravieira.willy.utils.WillyUtils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class OpenAiSender implements WillyChannel {
    private Context context;

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void send(WillyMessage message) {
        try {
            LinkedList<WillyMessage> messages = new LinkedList<>();
            messages.add(message);

            ChatRequest request = OpenAi.chat()
                .messages(WillyUtils.parseContextToOpenAIChat(messages, OpenAi.HISTORY_SIZE))
                .build();

            Chat chat = OpenAi.getService().chatCompletions().create(request).get();
            new OpenAiListener(context.getId()).whenCompletionComplete(chat);
            Willy.getLogger().fine("OpenAI send text success.");
        } catch (InterruptedException | ExecutionException | MalformedURLException | URISyntaxException e) {
            Willy.getLogger().warning("OpenAI send text fail: "+e.getMessage());
        }
    }

    @Override
    public void sendLast() {
        try {
            ChatRequest request = OpenAi.chat()
                .messages(WillyUtils.parseContextToOpenAIChat(context.getMessages(), OpenAi.HISTORY_SIZE))
                .build();
            Chat chat = OpenAi.getService().chatCompletions().create(request).get();
            new OpenAiListener(context.getId()).whenCompletionComplete(chat);
            Willy.getLogger().fine("OpenAI send text success.");
        } catch (InterruptedException | ExecutionException | MalformedURLException | URISyntaxException e) {
            Willy.getLogger().warning("OpenAI send text fail: "+e.getMessage());
        }
    }
}
