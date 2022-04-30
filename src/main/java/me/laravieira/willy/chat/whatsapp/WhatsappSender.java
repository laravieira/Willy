package me.laravieira.willy.chat.whatsapp;

import me.laravieira.willy.Willy;
import me.laravieira.willy.kernel.Sender;

import java.util.concurrent.ExecutionException;

public class WhatsappSender extends Sender {
    private WhatsappContext context;

    public WhatsappSender(WhatsappContext context) {
        super(context);
        this.context = context;
    }

    public void send(String message) {
        try {
            Whatsapp.getApi().sendMessage(context.getChat(), message).get();
        } catch (InterruptedException | ExecutionException e) {
            Willy.getLogger().warning("Fail when sending Whatsapp message.");
        }
    }

}
