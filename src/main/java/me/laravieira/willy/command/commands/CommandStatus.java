package me.laravieira.willy.command.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.http.HTTP;
import me.laravieira.willy.chat.http.Status;
import me.laravieira.willy.chat.openai.OpenAi;
import me.laravieira.willy.chat.telegram.Telegram;
import me.laravieira.willy.chat.whatsapp.Whatsapp;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.feature.bitly.Bitly;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.storage.ContextStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class CommandStatus implements CommandListener {
    public static final String COMMAND = "status";
    public static final String DESCRIPTION = "Willy's status";

    @Override
    public ApplicationCommandRequest register() {
        return ApplicationCommandRequest.builder()
            .name(COMMAND)
            .description(DESCRIPTION)
            .build();
    }

    @Override
    public void execute(@NotNull ChatInputInteractionEvent event) {
        boolean status = Status.generalStatus();
        StringBuilder list = new StringBuilder();

        long time  = (new Date().getTime()- Willy.getWilly().getStartTime())/1000;
        list.append(status ? ":white_check_mark:" : ":broken_heart:").append("   ");
        list.append("**Willy**").append("   ");
        list.append(Willy.getWilly().getFullVersion()).append("   ");
        list.append("`").append(Config.getString("environment")).append("`   ");
        list.append("`").append(time / (3600 * 24)).append("d ").append((time % (3600 * 24)) / (3600)).append("h ").append(time % (3600) / 60).append("m ").append(time % 60).append("s").append("`\r\n");

        list.append("```yaml").append("\r\n");
        list.append("context-life-time: ").append(Config.getLong("context_lifetime")).append("\r\n");
        list.append("openai:").append("\r\n");
        list.append("    enabled: ").append(Config.getBoolean("openai.enable")).append("\r\n");
        list.append("    connected: ").append(new OpenAi().isConnected()).append("\r\n");
        list.append("discord:").append("\r\n");
        list.append("    enabled: ").append(Config.getBoolean("discord.enable")).append("\r\n");
        list.append("    connected: ").append(new Discord().isConnected()).append("\r\n");
        list.append("    keep-willy-nick: ").append(Config.getBoolean("discord.keep_willy_nick")).append("\r\n");
        list.append("    keep-master-nick: ").append(Config.getLong("discord.keep_master_nick")).append("\r\n");
        list.append("    public-chat:").append("\r\n");
        list.append("        enabled: ").append(Config.getBoolean("discord.public_chat.enable")).append("\r\n");
        list.append("        auto-delete: ").append(Config.getBoolean("discord.public_chat.auto_delete.willy_messages")).append("\r\n");
        list.append("whatsapp:").append("\r\n");
        list.append("    enabled: ").append(Config.getBoolean("whatsapp.enable")).append("\r\n");
        list.append("    connected: ").append(new Whatsapp().isConnected()).append("\r\n");
        list.append("    phone-number: ").append(Config.getLong("whatsapp.phone_number")).append("\r\n");
        list.append("telegram:").append("\r\n");
        list.append("    enabled: ").append(Config.getBoolean("telegram.enable")).append("\r\n");
        list.append("    connected: ").append(new Telegram().isConnected()).append("\r\n");
        list.append("http-api:").append("\r\n");
        list.append("    enabled: ").append(Config.getBoolean("http_api.enable")).append("\r\n");
        list.append("    connected: ").append(new HTTP().isConnected()).append("\r\n");
        list.append("    port: ").append(Config.getInt("http_api.port")).append("\r\n");
        list.append("bitly:").append("\r\n");
        list.append("    enabled: ").append(Bitly.canUse).append("\r\n");
        list.append("contexts: ").append(ContextStorage.size()).append(" in use").append("\r\n");

        list.append("```");
        event.reply(list.toString()).subscribe();
    }

    @Override
    public String getName() {
        return COMMAND;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
