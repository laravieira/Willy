package me.laravieira.willy.chat.whatsapp;

import it.auties.whatsapp.api.WhatsappOptions;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

import java.util.concurrent.ExecutionException;

public class Whatsapp implements WillyChat {
    private static it.auties.whatsapp.api.Whatsapp whatsapp;

    @Override
    public void connect() {
        if(!Config.getBoolean("whatsapp.enable"))
            return;

        try {
            if(it.auties.whatsapp.api.Whatsapp.listConnections().isEmpty()) {
                WhatsappOptions options = WhatsappOptions.newOptions()
                        .description("Willy by L4R4V131R4")
                        .create();
                whatsapp = it.auties.whatsapp.api.Whatsapp.newConnection(options);
            }else whatsapp = it.auties.whatsapp.api.Whatsapp.lastConnection();

            whatsapp.registerListener(new WhatsappListener());
            whatsapp.connect().get();
        }catch(InterruptedException | ExecutionException e) {
            Willy.getLogger().warning(e.getMessage());
        }
        Willy.getLogger().info("Connecting to Whatsapp.");
    }

    @Override
    public void disconnect() {
        try{
            if(whatsapp != null)
                whatsapp.disconnect().get();
        }catch (IllegalStateException ignored) {
        }catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }finally {
            Willy.getLogger().info("Whatsapp instance was closed. ");
        }
        whatsapp = null;
    }

    @Override
    public boolean isConnected() {
        return whatsapp != null;
    }

    @Override
    public void refresh() {
    }

    public static void reconnect() {
        try{
            if(whatsapp != null)
                whatsapp.reconnect().get();
        }catch (IllegalStateException ignored) {
        }catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void logout() {
        try{
            if(whatsapp != null)
                whatsapp.logout().get();
        }catch (IllegalStateException ignored) {
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        whatsapp = null;
    }

    public static void chats() {
        whatsapp.store().chats().forEach(
                (chat) -> Willy.getLogger().getConsole().info(chat.name())
        );
    }

    public static it.auties.whatsapp.api.Whatsapp getApi() {
        return whatsapp;
    }
}
