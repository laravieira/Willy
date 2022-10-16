package me.laravieira.willy.chat.whatsapp;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Whatsapp implements WillyChat {
    private static it.auties.whatsapp.api.Whatsapp whatsapp;

    @Override
    public void connect() {
        if(!Config.getBoolean("whatsapp.enable"))
            return;

        Thread whatsappThread = new Thread(() -> {
            try {
                if(it.auties.whatsapp.api.Whatsapp.listConnections().isEmpty()) {
                    it.auties.whatsapp.api.Whatsapp.Options options = it.auties.whatsapp.api.Whatsapp.Options.newOptions()
                            .description(Willy.getWilly().getName() + " by L4R4")
                            .qrHandler(new WhatsappListener().onQRCode())
                            .build();
                    whatsapp = it.auties.whatsapp.api.Whatsapp.newConnection(options);
                }else whatsapp = it.auties.whatsapp.api.Whatsapp.lastConnection();

                whatsapp.addListener(new WhatsappListener());
                whatsapp.connect().get();
            }catch(InterruptedException | ExecutionException e) {
                it.auties.whatsapp.api.Whatsapp.listConnections().clear();
                Willy.getLogger().warning(e.getMessage());
            }
        });
        whatsappThread.setDaemon(true);
        whatsappThread.start();
    }

    @Override
    public void disconnect() {
        try {
            if(whatsapp != null)
                whatsapp.disconnect().get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {}

        Willy.getLogger().info("Whatsapp instance was requested to close.");
    }

    @Override
    public boolean isConnected() {
        return whatsapp != null;
    }

    @Override
    public void refresh() {
    }

    public static void reconnect() {
        Thread disconnect = new Thread(() -> {
            try{
                if(whatsapp != null)
                    whatsapp.reconnect().get();
            }catch (IllegalStateException ignored) {
            }catch (ExecutionException | InterruptedException | CompletionException e) {
                e.printStackTrace();
            }
        });
        disconnect.setDaemon(false);
        disconnect.start();
    }

    public static void logout() {
        try{
            if(whatsapp != null)
                whatsapp.logout().get();
        }catch (IllegalStateException ignored) {
        } catch (ExecutionException | InterruptedException | CompletionException e) {
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
