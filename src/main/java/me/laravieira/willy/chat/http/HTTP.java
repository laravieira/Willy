package me.laravieira.willy.chat.http;

import me.laravieira.willy.internal.WillyChat;
import org.eclipse.jetty.server.Server;

public class HTTP implements WillyChat {
    public static Server server = new Server();

    @Override
    public void connect() {
        server = new Server(7001);
        server.setHandler(new Handler());
        try {
            server.start();
        }catch (Exception ignored) {}
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
    public void refresh() {

    }
}
