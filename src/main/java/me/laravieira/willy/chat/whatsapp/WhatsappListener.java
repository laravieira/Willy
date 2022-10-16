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
import it.auties.whatsapp.api.DisconnectReason;
import it.auties.whatsapp.api.QrHandler;
import it.auties.whatsapp.listener.Listener;
import it.auties.whatsapp.model.chat.Chat;
import it.auties.whatsapp.model.contact.ContactStatus;
import it.auties.whatsapp.model.info.MessageInfo;
import it.auties.whatsapp.model.message.model.MessageStatus;
import it.auties.whatsapp.model.message.standard.TextMessage;
import it.auties.whatsapp.model.request.RequestException;
import lombok.SneakyThrows;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.chat.discord.DiscordSender;
import me.laravieira.willy.context.Context;
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
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WhatsappListener implements Listener {

    @Override
    public void onChats() {
        Listener.super.onChats();

        Whatsapp.getApi().store().chats().forEach(chat -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            if(chat.lastMessage().isEmpty() || chat.lastMessageFromMe().isEmpty())
                return;

            MessageInfo last = chat.lastMessage().get();
            MessageInfo myLast = chat.lastMessageFromMe().get();

            // chat.hasUnreadMessages() is not reliable
            if(last == myLast || last.status() == MessageStatus.READ)
                return;

            last.key(last.key().chat(chat));
            Willy.getLogger().info("Unread messages on chat "+chat.jid().user());
            onNewMessage(last);
        });
    }

    @Override
    public void onChatMessages(Chat chat, boolean last) {
        Listener.super.onChats();
        if(!last)
            return;

        if(chat.hasUnreadMessages() && chat.lastMessage().isPresent())
            onNewMessage(chat.lastMessage().get());
    }

    @Override
    public void onNewMessage(@NotNull MessageInfo info) {
        Listener.super.onNewMessage(info);

        if(!(info.message().content() instanceof TextMessage message) || message.text().isEmpty())
            return;
        Chat chat = info.chat();

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
            ContextStorage.of(id).setApp("whatsapp");

            WhatsappMessage whatsappMessage = new WhatsappMessage(id, info, content, PassedInterval.DISABLE);
            MessageStorage.add(whatsappMessage);
            ContextStorage.of(whatsappMessage.getContext()).getWatson().getSender().sendText(whatsappMessage.getText());
        });

        Thread messageStatusUpdate = new Thread(() -> {
            try {
                Whatsapp.getApi().markRead(chat).get(5, TimeUnit.SECONDS);
                Whatsapp.getApi().clear(chat, false).get(5, TimeUnit.SECONDS);
                Whatsapp.getApi().changePresence(chat, ContactStatus.COMPOSING).get(5, TimeUnit.SECONDS);
            }catch(CompletionException | InterruptedException | TimeoutException | ExecutionException ignored) {}
        });

        messageStatusUpdate.setDaemon(true);
        messageStatusUpdate.start();
        messageHandler.setDaemon(true);
        messageHandler.start();
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
                ContextStorage.of(id).setSender(sender);

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

    @SneakyThrows
    @Override
    public void onLoggedIn() {
        Willy.getLogger().info("Whatsapp instance connected.");

        Snowflake masterSnowflake = Snowflake.of(Config.getString("discord.keep_master_nick"));
        User master = Discord.getBotGateway().getUserById(masterSnowflake).block();
        if(master == null)
            return;
        UUID id = UUID.nameUUIDFromBytes(("discord-"+master.getId().asString()).getBytes());

        if(ContextStorage.has(id)) {
            Message message = ContextStorage.of(id).getLastMessage();
            message.delete();
            MessageStorage.remove(message.getId());
        }
    }

    @Override
    public void onDisconnected(DisconnectReason reason) {
        Listener.super.onDisconnected(reason);
        Willy.getLogger().info("Whatsapp instance disconnected.");
    }
}
