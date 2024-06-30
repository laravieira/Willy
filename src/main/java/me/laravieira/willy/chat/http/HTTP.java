package me.laravieira.willy.chat.http;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;
import org.eclipse.jetty.server.Server;

public class HTTP implements WillyChat {
    public static Server server = new Server();

    @Override
    public void connect() {
        if(!Config.has("http-api.enable") || !Config.getBoolean("http-api.enable")) {
            Willy.getLogger().info("HTTP API is disabled.");
            return;
        }
        if(!Config.has("http-api.port")) {
            Willy.getLogger().severe("HTTP API port is not defined.");
            return;
        }

        server = new Server(Config.getInt("http-api.port"));
        server.setHandler(new Handler());

        try {
            server.start();
            Willy.getLogger().info(STR."HTTP API listening on port \{Config.getInt("http-api.port")}.");
        }catch (Exception exception) {
            Willy.getLogger().severe(STR."HTTP API failed to start: \{exception.getMessage()}");
        }
    }

    @Override
    public void disconnect() {
        try {
            if(server.isRunning())
                server.stop();
        }catch (Exception ignored) {}
    }

    @Override
    public boolean isConnected() {
        try {
            if(server.isRunning())
                return true;
        }catch (Exception ignored) {}
        return false;
    }

    @Override
    public void refresh() {}
}
