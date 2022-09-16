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
import it.auties.whatsapp.api.QrHandler;
import it.auties.whatsapp.listener.Listener;
import it.auties.whatsapp.model.chat.Chat;
import it.auties.whatsapp.model.info.MessageInfo;
import it.auties.whatsapp.model.message.standard.TextMessage;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.chat.discord.DiscordSender;
import me.laravieira.willy.context.Message;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.utils.WillyUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class WhatsappListener implements Listener {

    @Override
    public void onNewMessage(@NotNull MessageInfo info) {
        if(!(info.message().content() instanceof TextMessage message) || message.text().isEmpty() || info.chat().isEmpty())
            return;
        Chat chat = info.chat().get();

        Thread messageHandler = new Thread(() -> {
            UUID id = UUID.nameUUIDFromBytes(("whatsapp-"+info.senderJid().user()).getBytes());
            String content = message.text();

            if(Config.getBoolean("whatsapp.shared_chat")
            && !WillyUtils.hasWillyCall(content)
            && !ContextStorage.has(id))
                return;

            content = content.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');

            WhatsappSender sender = new WhatsappSender(chat);
            ContextStorage.of(id).setSender(sender);

            WhatsappMessage whatsappMessage = new WhatsappMessage(id, info, content, PassedInterval.DISABLE);
            MessageStorage.add(whatsappMessage);
            ContextStorage.of(whatsappMessage.getContext()).getWatson().getSender().sendText(whatsappMessage.getText());
        });

        try {
            Thread.sleep(1000);
            //Whatsapp.getApi().markAsRead(chat).get();

            Whatsapp.getApi().markAsRead(chat.jid()).get();
            //Whatsapp.getApi().changePresence(chat, ContactStatus.COMPOSING).get();
            Willy.getLogger().info("Message from Whatsapp processed: "+info.status().name());
            Willy.getLogger().info("Chat status: " + chat.hasUnreadMessages());
            //Whatsapp.getApi().changePresence(chat, ContactStatus.AVAILABLE).get();
            //Willy.getLogger().info("Message from Whatsapp processed 2.");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        //messageHandler.setDaemon(true);
        //messageHandler.start();
    }

    public QrHandler onQRCode()
    {
        return (qr) -> {
            try {
                MultiFormatWriter writer = new MultiFormatWriter();
                BitMatrix matrix = writer.encode(qr, BarcodeFormat.QR_CODE, 500, 500, Map.of(EncodeHintType.MARGIN, 3, EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L));

                Path path = Files.createTempFile(UUID.randomUUID().toString(), ".png");
                MatrixToImageWriter.writeToPath(matrix, "png", path);

                Snowflake masterSnowflake = Snowflake.of(Config.getString("discord.keep_master_nick"));
                User master = Discord.getBotGateway().getUserById(masterSnowflake).block();
                if(master == null) {
                    Willy.getLogger().warning("Unable to send Whatsapp QR-Code to Discord master.");
                    return;
                }
                MessageChannel masterChannel = master.getPrivateChannel().block();
                UUID id = UUID.nameUUIDFromBytes(("discord-"+master.getId().asString()).getBytes());

                DiscordSender sender = new DiscordSender(id, masterChannel, PassedInterval.DISABLE);

                Message message = new Message(id);
                message.setExpire(PassedInterval.DISABLE);
                message.setTo(master.getUsername());
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
        };
    }

    @Override
    public void onLoggedIn() {
        Willy.getLogger().info("Whatsapp instance connected.");

        Snowflake masterSnowflake = Snowflake.of(Config.getString("discord.keep_master_nick"));
        User master = Discord.getBotGateway().getUserById(masterSnowflake).block();
        if(master == null)
            return;
        UUID id = UUID.nameUUIDFromBytes(("discord-"+master.getId().asString()).getBytes());

        Message message = ContextStorage.of(id).getLastMessage();
        message.delete();
        MessageStorage.remove(message.getId());
    }

    @Override
    public void onDisconnected(boolean reconnect) {
        Willy.getLogger().info("Whatsapp instance disconnected.");
    }
}
