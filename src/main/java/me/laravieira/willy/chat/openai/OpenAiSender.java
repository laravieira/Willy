package me.laravieira.willy.chat.openai;

import io.github.sashirestela.openai.domain.chat.ChatRequest;
import me.laravieira.willy.Willy;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.utils.WillyUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class OpenAiSender implements SenderInterface {
    private final UUID context;


    public OpenAiSender(UUID context) {
        this.context = context;
    }

    @Override
    public void send(Object message) {

    }

    @Override
    public void sendText(String message) {
        LinkedList<UUID> messages = ContextStorage.of(context).getMessages();
        ChatRequest request = OpenAi.chat()
            .messages(WillyUtils.parseContextToOpenAIChat(messages, OpenAi.HISTORY_SIZE))
            .build();
        try {
            OpenAi.getService().chatCompletions().create(request)
                .whenComplete((chat, throwable) -> OpenAiListener.whenCompletionComplete(chat, throwable, context)).get();
        } catch (InterruptedException | ExecutionException e) {
            Willy.getLogger().warning(e.getMessage());
        }
    }

    @Override
    public void sendLink(Message message) {

    }

    @Override
    public void sendStick(Message message) {

    }

    @Override
    public void sendGif(Message message) {

    }

    @Override
    public void sendImage(Message message) {

    }

    @Override
    public void sendVideo(Message message) {

    }

    @Override
    public void sendAudio(Message message) {

    }

    @Override
    public void sendLocation(Message message) {

    }

    @Override
    public void sendContact(Message message) {

    }

    @Override
    public void sendFile(File message) {

    }
}
