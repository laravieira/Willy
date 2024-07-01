package me.laravieira.willy.chat.whatsapp;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

import static it.auties.whatsapp.api.Whatsapp.webBuilder;

public class Whatsapp implements WillyChat {
    private static it.auties.whatsapp.api.Whatsapp whatsapp = null;

    @Override
    public void connect() {
        if(!Config.getBoolean("whatsapp.enable")) {
            Willy.getLogger().info("Whatsapp instance was disabled.");
            return;
        }
        if(!Config.has("whatsapp.phone_number")) {
            Willy.getLogger().severe("Whatsapp phone number was not found.");
            return;
        }

        Thread whatsappThread = new Thread(Whatsapp::onConnectionRequest);
        whatsappThread.setDaemon(true);
        whatsappThread.start();
    }

    private static void onConnectionRequest() {
        whatsapp = webBuilder()
            .newConnection(Willy.getWilly().getName())
            .autodetectListeners(false)
            .name(STR."\{Willy.getWilly().getName()} \{Config.getString("environment")}")
            .errorHandler(WhatsappHandler::onError)
            .unregistered(WhatsappHandler::onQRCode)
            .addListener(new WhatsappListener());
        whatsapp
            .connect()
            .whenComplete((whatsapp, error) -> {
                if(error == null)
                    return;
                Willy.getLogger().warning(STR."Error on Whatsapp connection: \{error.getMessage()}");
                Whatsapp.whatsapp = null;
            });
    }

    @Override
    public void disconnect() {
        if(whatsapp == null || !whatsapp.isConnected())
            return;
        whatsapp.disconnect().join();
        Willy.getLogger().info("Whatsapp instance was disconnected.");
    }

    @Override
    public boolean isConnected() {
        return whatsapp != null && whatsapp.isConnected();
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
                    Willy.getLogger().warning(STR."Whatsapp instance couldn't reconnect: \{error.getMessage()}");
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
                Willy.getLogger().warning(STR."Whatsapp instance couldn't logout: \{error.getMessage()}");
            new Whatsapp().disconnect();
        });
    }

    public static void chats() {
        whatsapp.store().chats().forEach(
            (chat) -> Willy.getLogger().getConsole().info(chat.name())
        );
    }

    public static void create() {
        if(!Config.getBoolean("whatsapp.enable"))
            return;
        onConnectionRequest();
    }

    public static it.auties.whatsapp.api.Whatsapp getApi() {
        return whatsapp;
    }
}
