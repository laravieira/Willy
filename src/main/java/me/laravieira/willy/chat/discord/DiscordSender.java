package me.laravieira.willy.chat.discord;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import me.laravieira.willy.kernel.Sender;

import java.awt.*;

public class DiscordSender extends Sender {
    private DiscordContext context;

    public DiscordSender(DiscordContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public void send(String message) {
        try {
            Message msg = context.getChannel().createMessage(message).block();
            context.autoDeleteMessage(msg);
        }catch (NullPointerException e) {
            new Discord().connect();
            Message msg = context.getChannel().createMessage(message).block();
            context.autoDeleteMessage(msg);
        }
    }

    @Override
    public void sendImage(String message, Image image) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().image(image.toString()).build();
        Message msg = context.getChannel().createMessage(MessageCreateSpec
                .builder()
                .addEmbed(embed)
                .build()
        ).block();
        context.autoDeleteMessage(msg);
    }

    public void sendEmbedMessage(EmbedCreateSpec message) {
        try {
            Message msg = context.getChannel().createMessage(MessageCreateSpec
                    .builder()
                    .addEmbed(message)
                    .build()
            ).block();
            context.autoDeleteMessage(msg);
        }catch (NullPointerException e) {
            new Discord().connect();
            Message msg = context.getChannel().createMessage(MessageCreateSpec
                    .builder()
                    .addEmbed(message)
                    .build()
            ).block();
            context.autoDeleteMessage(msg);
        }
    }

    public Message sendSpecMessage(MessageCreateSpec message) {
        try {
            Message msg = context.getChannel().createMessage(message).block();
            context.autoDeleteMessage(msg);
            return msg;
        }catch (NullPointerException e) {
            new Discord().connect();
            Message msg = context.getChannel().createMessage(message).block();
            context.autoDeleteMessage(msg);
            return msg;
        }
    }
}
