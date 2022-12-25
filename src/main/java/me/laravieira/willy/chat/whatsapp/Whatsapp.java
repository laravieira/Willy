package me.laravieira.willy.chat.whatsapp;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

import java.util.UUID;

public class Whatsapp implements WillyChat {
    private static final int id = UUID.nameUUIDFromBytes("willy-whatsapp".getBytes()).hashCode();
    private static it.auties.whatsapp.api.Whatsapp whatsapp;
    private static final it.auties.whatsapp.api.Whatsapp.Options options = it.auties.whatsapp.api.Whatsapp.Options.newOptions()
            .id(id)
            .description(Willy.getWilly().getName() + " by L4R4")
            .autodetectListeners(false)
            .errorHandler(WhatsappHandler::onError)
            .qrHandler(WhatsappHandler::onQRCode)
            .build();

    @Override
    public void connect() {
        if(!Config.getBoolean("whatsapp.enable"))
            return;

        Thread whatsappThread = new Thread(() -> {
            if(it.auties.whatsapp.api.Whatsapp.listConnections(options).isEmpty()) {
                it.auties.whatsapp.api.Whatsapp.deleteConnections();
                Willy.getLogger().warning("There is no Whatsapp connection.");
                return;
            }

            whatsapp = it.auties.whatsapp.api.Whatsapp.lastConnection(options);
            whatsapp.addListener(new WhatsappListener());
            whatsapp.connect().whenComplete((whatsapp, error) -> {
                if(error == null)
                    Willy.getLogger().info("Last Whatsapp instance was successfully connected.");
                else {
                    it.auties.whatsapp.api.Whatsapp.deleteConnections();
                    Willy.getLogger().warning("Error on last Whatsapp instance connection: " + error.getMessage());
                }
            });
        });
        whatsappThread.setDaemon(true);
        whatsappThread.start();
    }

    @Override
    public void disconnect() {
        if(whatsapp == null)
            return;
        whatsapp.disconnect().join();
        Willy.getLogger().info("Whatsapp instance was disconnected.");
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
            if(whatsapp == null)
                return;
            whatsapp.reconnect().whenComplete((success, error) -> {
                if(error == null)
                    Willy.getLogger().info("Whatsapp instance was reconnected.");
                else
                    Willy.getLogger().warning("Whatsapp instance couldn't reconnect: "+error.getMessage());
            });
        });
        disconnect.setDaemon(false);
        disconnect.start();
    }

    public static void logout() {
        if(whatsapp == null)
            return;
        whatsapp.logout().whenComplete((success, error) -> {
            if(error == null)
                Willy.getLogger().info("Whatsapp instance successfully logout.");
            else
                Willy.getLogger().warning("Whatsapp instance couldn't logout: "+error.getMessage());
            new Whatsapp().disconnect();
        });
    }

    public static void chats() {
        whatsapp.store().chats().forEach(
                (chat) -> Willy.getLogger().getConsole().info(chat.name())
        );
    }

    public static void create() {
        try {
            it.auties.whatsapp.api.Whatsapp.deleteConnections();
        }catch (NullPointerException ignore) {
            it.auties.whatsapp.api.Whatsapp.listConnections().clear();
        }

        whatsapp = it.auties.whatsapp.api.Whatsapp.newConnection(options);

        whatsapp.addListener(new WhatsappListener());
        whatsapp.connect().whenComplete((whatsapp, error) -> {
            if(error == null)
                Willy.getLogger().info("New Whatsapp instance was successfully connected.");
            else
                Willy.getLogger().warning("Error on new Whatsapp instance connection: "+error.getMessage());
        });
    }

    public static it.auties.whatsapp.api.Whatsapp getApi() {
        return whatsapp;
    }
}
