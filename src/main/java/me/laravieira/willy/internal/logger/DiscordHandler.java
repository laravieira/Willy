package me.laravieira.willy.internal.logger;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.internal.Config;
import reactor.core.scheduler.Schedulers;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

class DiscordHandler extends Handler {
    @Override
    public void publish(LogRecord record) {
        if(record.getMessage().isEmpty() || record.getLevel().intValue() < Level.INFO.intValue())
            return;

        GatewayDiscordClient gateway = Discord.getBotGateway();
        if(gateway == null || !Config.has("discord.admin.log"))
            return;

        String message = STR."```yaml\r\n[\{record.getLevel()}] \{record.getMessage()}```";
        gateway.getChannelById(Snowflake.of(Config.getLong("discord.admin.log")))
            .publishOn(Schedulers.boundedElastic())
            .doOnError(error -> Willy.getLogger().fine(error.getMessage()))
            .doOnSuccess(channel -> ((MessageChannel)channel).createMessage(message).subscribe())
            .block();
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}

    @Override
    public void setFormatter(Formatter formatter) {}

}