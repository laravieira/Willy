package me.laravieira.willy.internal.logger;

import discord4j.core.object.entity.channel.GuildMessageChannel;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

class DiscordHandler extends Handler {
    private final GuildMessageChannel channel;

    public DiscordHandler(GuildMessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public void publish(LogRecord record) {
        if(record.getMessage().isEmpty() || record.getLevel().intValue() < Level.INFO.intValue())
            return;

        if(channel == null) return;

        String message = "```yaml\r\n["+record.getLevel()+"] "+record.getMessage()+"```";
        channel.createMessage(message).block();
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}

    @Override
    public void setFormatter(Formatter formatter) {}

}