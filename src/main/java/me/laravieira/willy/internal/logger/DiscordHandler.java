package me.laravieira.willy.internal.logger;

import me.laravieira.willy.chat.discord.Discord;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

class DiscordHandler extends Handler {
    @Override
    public void publish(LogRecord record) {
        if(record.getMessage().isEmpty())
            return;
        Discord.sendLog(record);
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}

    @Override
    public void setFormatter(Formatter formatter) {}

}