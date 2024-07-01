package me.laravieira.willy.chat.telegram;

import com.pengrad.telegrambot.TelegramBot;
import lombok.Getter;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

public class Telegram implements WillyChat {
    @Getter
    private static TelegramBot bot;

    @Override
    public void connect() {
        if(!Config.getBoolean("telegram.enable")) {
            Willy.getLogger().info("Telegram instance was disabled.");
            return;
        }
        if(!Config.has("telegram.token")) {
            Willy.getLogger().severe("Telegram token was not found.");
            return;
        }
        bot = new TelegramBot(Config.getString("telegram.token"));
        bot.setUpdatesListener(new TelegramListener());
        Willy.getLogger().info("Telegram instance connected.");
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
