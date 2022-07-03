package me.laravieira.willy.feature.shadow;

import me.laravieira.willy.Willy;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.internal.WillyChat;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;

import javax.security.auth.login.LoginException;

public class JDA implements WillyChat {
    private static net.dv8tion.jda.api.JDA app;

    @Override
    public void connect() {
        if(!Config.getBoolean("shadow.enable"))
            return;
        Thread jda = new Thread(() -> {
            try {
                String token = Config.getString("shadow.token");
                app = JDABuilder.createDefault(token)
                        .setAutoReconnect(true)
                        .setStatus(OnlineStatus.INVISIBLE)
                        .build();
                app.awaitReady();
                Willy.getLogger().info("WillyShadow instance opened.");
            } catch (LoginException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        jda.setDaemon(true);
        jda.start();
    }

    @Override
    public void disconnect() {
        if(app != null) {
            app.getPresence().setStatus(OnlineStatus.OFFLINE);
            app.shutdown();
        }
    }

    @Override
    public boolean isConnected() {
        return !app.isUnavailable(1000);
    }

    @Override
    public void refresh() {

    }
}
