package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.TelegramBot;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.WillyChat;

public class Telegram implements WillyChat {
    private static TelegramBot bot;

    @Override
    public void connect() {
        if(!Willy.getConfig().asBoolean("telegram.enable"))
            return;
        bot = new TelegramBot(Willy.getConfig().asString("telegram.token"));
        bot.setUpdatesListener(new TelegramListener());
        Willy.getLogger().info("Telegram instance connected.");
    }

    public static TelegramBot getBot() {
        return bot;
    }


    @Override
    public void disconnect() {
        if(bot != null) {
            bot.shutdown();
            bot = null;
        }
        Willy.getLogger().info("Telegram instance was closed.");
    }

    @Override
    public boolean isConnected() {
        return bot != null;
    }

    @Override
    public void refresh() {

    }
}
