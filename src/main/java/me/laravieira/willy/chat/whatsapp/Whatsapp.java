package me.laravieira.willy.chat.whatsapp;

import it.auties.whatsapp4j.manager.WhatsappDataManager;
import it.auties.whatsapp4j.whatsapp.WhatsappAPI;
import it.auties.whatsapp4j.whatsapp.WhatsappConfiguration;
import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.WillyChat;

public class Whatsapp implements WillyChat {
    private static WhatsappAPI api;
    private static WhatsappDataManager manager;

    @Override
    public void connect() {
        if(!Willy.getConfig().asBoolean("whatsapp.enable"))
            return;
        WhatsappConfiguration configuration = WhatsappConfiguration.builder()
                .description("Willy by L4R4V131R4")
                .shortDescription("Wly")
                .reconnectWhenDisconnected((reason) -> true)
                .async(true)
                .build();
        api = new WhatsappAPI(configuration);
        api.registerListener(new WhatsappListener());
        Willy.getLogger().info("Waiting Whatsapp app to respond.");
        api.connect();
        manager = api.manager();
    }

    @Override
    public void disconnect() {
        try{
            if(api != null)
                api.disconnect();
        }catch (IllegalStateException ignored) {}
        api = null;
    }

    @Override
    public boolean isConnected() {
        return api != null;
    }

    @Override
    public void refresh() {

    }

    public static void reconnect() {
        try{
            if(api != null)
                api.reconnect();
        }catch (IllegalStateException ignored) {}
    }

    public static void logout() {
        try{
            if(api != null)
                api.logout();
        }catch (IllegalStateException ignored) {}
        api = null;
    }

    public static void chats() {
        manager.chats().forEach(
                (chat) -> Willy.getLogger().getConsole().info(chat.displayName())
        );
    }

    public static WhatsappAPI getApi() {
        return api;
    }
}
