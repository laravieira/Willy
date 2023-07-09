package me.laravieira.willy.chat.whatsapp;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import it.auties.whatsapp.api.ClientType;
import it.auties.whatsapp.api.ErrorHandler;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.chat.discord.DiscordSender;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class WhatsappHandler {
    public static ErrorHandler.Result onError(ClientType ignore, ErrorHandler.Location location, Throwable throwable) {
        if(Willy.getWilly().getStop())
            return ErrorHandler.Result.DISCONNECT;
        if(location.name().equals("LOGGED_OUT")) {
            new Whatsapp().disconnect();
            Snowflake consoleSnowflake = Snowflake.of(Config.getLong("discord.admin.command"));
            Snowflake masterSnowflake = Snowflake.of(Config.getString("discord.keep_master_nick"));
            MessageChannel console = (MessageChannel) Discord.getBotGateway().getChannelById(consoleSnowflake).block();
            User master = Discord.getBotGateway().getUserById(masterSnowflake).block();
            if(console == null || master == null)
                return ErrorHandler.Result.DISCARD;
            console.createMessage(MessageCreateSpec.builder()
                .content(master.getMention()+" Whatsapp is disconnected.").build())
                .subscribe();
            return ErrorHandler.Result.RECONNECT;
        }
        Willy.getLogger().severe(location.name()+": "+throwable.getMessage());
        return ErrorHandler.Result.DISCARD;
    }

    public static void onQRCode(String qr) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(qr, BarcodeFormat.QR_CODE, 500, 500, Map.of(EncodeHintType.MARGIN, 3, EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L));

            Path path = Files.createTempFile(UUID.randomUUID().toString(), ".png");
            MatrixToImageWriter.writeToPath(matrix, "png", path);

            Snowflake settingsSnowflake = Snowflake.of(Config.getLong("discord.admin.command"));
            MessageChannel masterChannel = (MessageChannel) Discord.getBotGateway().getChannelById(settingsSnowflake).block();
            if(masterChannel == null)
                return;

            UUID id = UUID.nameUUIDFromBytes(("discord-"+masterChannel.getId().asString()).getBytes());

            DiscordSender sender = new DiscordSender(id, masterChannel, PassedInterval.DISABLE);
            ContextStorage.of(id).setSender(sender);

            Message message = new Message(id);
            message.setExpire(PassedInterval.DISABLE);
            message.setTo(masterChannel.getId().asString());
            message.setFrom(Willy.getWilly().getName());
            message.setContent(MessageCreateSpec.builder()
                    .addEmbed(EmbedCreateSpec.builder()
                            .title("Willy likes Whatsapp")
                            .description("Read this QR Code on your WhatsApp app to let Willy use it")
                            .thumbnail("https://github.com/laravieira/Willy/raw/master/assets/whatsapp.png")
                            .image("attachment://whatsapp-qrcode.png")
                            .color(Color.GREEN)
                            .build())
                    .addFile("whatsapp-qrcode.png", new FileInputStream(path.toFile()))
                    .build());
            message.setText("Whatsapp QR-Code.");
            MessageStorage.add(message);

            sender.sendEmbed((MessageCreateSpec)message.getContent());
        } catch (IOException | WriterException exception) {
            Willy.getLogger().warning("Unable to handle WhatsApp QR Code.");
        }
    }
}
