package me.laravieira.willy.chat.whatsapp;

import it.auties.whatsapp.api.DisconnectReason;
import it.auties.whatsapp.api.SocketEvent;
import it.auties.whatsapp.listener.Listener;
import it.auties.whatsapp.model.chat.Chat;
import it.auties.whatsapp.model.contact.ContactStatus;
import it.auties.whatsapp.model.info.ChatMessageInfo;
import it.auties.whatsapp.model.info.MessageInfo;
import it.auties.whatsapp.model.message.model.MessageStatus;
import it.auties.whatsapp.model.message.standard.TextMessage;
import lombok.SneakyThrows;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.utils.WillyUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.*;

public class WhatsappListener implements Listener {

    @Override
    public void onChats(Collection<Chat> chats) {
        Listener.super.onChats(chats);

        chats.forEach(chat -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            if(chat.newestMessage().isEmpty() || chat.newestMessageFromMe().isEmpty())
                return;

            ChatMessageInfo last = chat.newestMessage().get();
            ChatMessageInfo myLast = chat.newestMessageFromMe().get();

            // chat.hasUnreadMessages() is not reliable
            if(last == myLast || last.status() == MessageStatus.READ)
                return;

//            last.key(last.key().chat(chat));
            Willy.getLogger().info("Unread messages on chat "+chat.jid().user());
            onNewMessage(last);
        });
    }

    @Override
    public void onChatMessagesSync(Chat chat, boolean last) {
        Listener.super.onChatMessagesSync(chat, last);
        if(!last)
            return;

        if(chat.hasUnreadMessages() && chat.newestMessage().isPresent())
            onNewMessage(chat.newestMessage().get());
    }

    @Override
    public void onNewMessage(@NotNull MessageInfo _info) {
        ChatMessageInfo info = (ChatMessageInfo) _info;
        Listener.super.onNewMessage(info);

        if(!(info.message().content() instanceof TextMessage message) || message.text().isEmpty())
            return;
        if(info.fromMe())
            return;

        if(info.chat().isEmpty())
            return;

        Thread messageHandler = new Thread(() -> {
            Chat chat = info.chat().get();
            UUID id = UUID.nameUUIDFromBytes(("whatsapp-"+info.senderJid().user()).getBytes());
            String content = message.text();

            if(Config.getBoolean("whatsapp.group.enable")
            && !WillyUtils.hasWillyName(content, Config.getStringList("whatsapp.group.willy_names"))
            && !ContextStorage.has(id))
                return;

            content = content.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');

            WhatsappSender sender = new WhatsappSender(chat);
            ContextStorage.of(id).setUserSender(sender);
            ContextStorage.of(id).setApp("whatsapp");

            WhatsappMessage whatsappMessage = new WhatsappMessage(id, info, content, PassedInterval.DISABLE);
            MessageStorage.add(whatsappMessage);
            ContextStorage.of(whatsappMessage.getContext()).getSender().sendText(whatsappMessage.getText());

            Thread messageStatusUpdate = new Thread(() -> {
                try {
                    Whatsapp.getApi().markChatRead(chat).get(5, TimeUnit.SECONDS);
                    Whatsapp.getApi().clearChat(chat, false).get(5, TimeUnit.SECONDS);
                    Whatsapp.getApi().changePresence(chat, ContactStatus.COMPOSING).get(5, TimeUnit.SECONDS);
                }catch(CompletionException | InterruptedException | TimeoutException | ExecutionException ignored) {}
            });

            messageStatusUpdate.setDaemon(true);
            messageStatusUpdate.start();
        });
        messageHandler.setDaemon(true);
        messageHandler.start();
    }

    @SneakyThrows
    @Override
    public void onLoggedIn() {
        Willy.getLogger().info("Whatsapp instance connected.");
    }

    @Override
    public void onDisconnected(DisconnectReason reason) {
        Listener.super.onDisconnected(reason);
        Willy.getLogger().info("Whatsapp instance disconnected.");
    }

    @Override
    public void onSocketEvent(SocketEvent event) {
        try {
            Listener.super.onSocketEvent(event);
        }catch (CompletionException | CancellationException e) {
            Willy.getLogger().warning(event.name());
        }
    }
}
