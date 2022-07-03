package me.laravieira.willy.chat.command.commands;

import it.auties.whatsapp.model.contact.ContactJid;
import me.laravieira.willy.chat.command.Command;
import me.laravieira.willy.chat.command.CommandListener;
import me.laravieira.willy.chat.whatsapp.Whatsapp;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class CommandWhatsapp implements CommandListener {
    public static final String COMMAND = "whats";

    public void execute(@NotNull Logger console, int count, String[] args) {
        if(count < 2) {
            console.info("This command need an user id, type 'help' to see usage.");
            return;
        }
        if(args[1].equals("connect"))
            new Whatsapp().connect();
        if(args[1].equals("disconnect"))
            new Whatsapp().disconnect();
        if(args[1].equals("reconnect"))
            Whatsapp.reconnect();
        if(args[1].equals("logout"))
            Whatsapp.logout();
        if(args[1].equals("connections"))
            it.auties.whatsapp.api.Whatsapp.listConnections().forEach(
                    whatsapp -> console.info(whatsapp.toString())
            );
        if(args[1].equals("logoutall"))
            it.auties.whatsapp.api.Whatsapp.listConnections().forEach(
                    it.auties.whatsapp.api.Whatsapp::logout
            );
        if(args[1].equals("chats"))
            Whatsapp.chats();
        if(args[1].equals("talk") && count > 3) {
            Whatsapp.getApi().store().findChatByJid(ContactJid.of(args[2])).ifPresent(chat -> {
                try {
                    Whatsapp.getApi().sendMessage(chat, args[3]).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }else {
            Command.unknow();}
    }
}
