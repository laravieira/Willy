package me.laravieira.willy.chat.bloom;

import me.laravieira.willy.context.Message;
import me.laravieira.willy.context.SenderInterface;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.utils.WillyUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BloomSender implements SenderInterface {
    private final UUID context;

    public BloomSender(UUID context) {
        this.context = context;
    }

    public void sendPrompt(String prompt) {
        if(!new Bloom().isConnected())
            return;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(Bloom.buildCommand(prompt));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            process.onExit().whenComplete((r, t) -> new BloomListener(this.context).onResponse(r, t));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(Object message) {

    }

    @Override
    public void sendText(String message) {
        BloomHeader headerBuilder = new BloomHeader(context);
        LinkedList<UUID> messages = ContextStorage.of(context).getMessages();
        String prompt = headerBuilder.build()+WillyUtils.buildConversation(messages, Bloom.HISTORY_SIZE);
        sendPrompt(prompt);
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
