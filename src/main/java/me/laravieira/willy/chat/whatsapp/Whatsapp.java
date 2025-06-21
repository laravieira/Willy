package me.laravieira.willy.chat.whatsapp;

import it.auties.whatsapp.api.MediaProxySetting;
import it.auties.whatsapp.api.QrHandler;
import it.auties.whatsapp.api.TextPreviewSetting;
import it.auties.whatsapp.api.WebHistorySetting;
import it.auties.whatsapp.model.companion.CompanionDevice;
import it.auties.whatsapp.model.signal.auth.Version;
import lombok.Getter;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;

import static it.auties.whatsapp.api.Whatsapp.webBuilder;

public class Whatsapp implements WillyChat {
    @Getter
    private static it.auties.whatsapp.api.Whatsapp whatsapp = null;

    @Override
    public void connect() {
        if(!Config.getBoolean("whatsapp.enable")) {
            Willy.getLogger().info("Whatsapp service is disabled.");
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
            .mediaProxySetting(MediaProxySetting.NONE)
            .automaticMessageReceipts(false)
            .textPreviewSetting(TextPreviewSetting.DISABLED)
            .historySetting(WebHistorySetting.discard(true))
            .name(STR."\{Willy.getWilly().getName()} \{Config.getString("environment")}")
            .errorHandler(WhatsappHandler::onError)
            .unregistered(QrHandler.toFile(WhatsappHandler::onQRCode ))
            .addListener(new WhatsappListener());

        //TODO Temp fix till version 0.0.10 is released
        whatsapp.store()
            .setDevice(CompanionDevice.web(Version.of("2.3000.1023231279")));

        whatsapp.connect().join();
    }

    @Override
    public void disconnect() {
        if(whatsapp == null || !whatsapp.isConnected())
            return;
        whatsapp.disconnect().join();
    }

    @Override
    public boolean isConnected() {
        return whatsapp != null && whatsapp.isConnected();
    }

    @Override
    public void refresh() {
    }

    public static void logout() {
        try {
            if(whatsapp == null) {
                Willy.getLogger().info("Whatsapp service was already null.");
                return;
            }
            whatsapp.logout().join();
        } catch (Exception e) {
            Willy.getLogger().warning(STR."Error on Whatsapp logout: \{e.getMessage()}");
        }
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
