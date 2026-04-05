package me.laravieira.willy;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import me.laravieira.willy.chat.http.HTTP;
import me.laravieira.willy.chat.telegram.Telegram;
import me.laravieira.willy.chat.whatsapp.WhatsApp;
import me.laravieira.willy.chat.openai.OpenAi;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.internal.logger.WillyLogger;

public class Willy {
	private static final String my_name    = "Willy";
	private static final int[]  my_version = {0,18,0};
	private static final long   start_time = new Date().getTime();

	@Getter
	private static final WillyLogger logger = new WillyLogger();
    @Getter
	private static ArrayList<WillyChat> chats = new ArrayList<>();
    @Getter
    private static WillyBrain brain = null;
	private static boolean stop = false;

    public static String getName()        { return my_name; }
    public static String getFullVersion() { return "v"+my_version[0]+"."+my_version[1]+"."+my_version[2]; }
    public static long   getStartTime()   { return start_time; }
    public static void   stop() { stop = true; }

	private static void onShutdown() {
		if(!stop) stop = true;
		logger.info("Shutting down Willy!");
        chats.forEach(WillyChat::disconnect);
		logger.info("Willy processes shutdown.");
		logger.close();
	}

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Willy::onShutdown));
        logger.info("Initializing Willy! ("+getFullVersion()+")");

        Config.load();
        logger.info("Configurations loaded.");

        OpenAi brain = new OpenAi();
        Willy.brain = brain;

        chats.add(brain);
        chats.add(new Discord());
        chats.add(new WhatsApp());
		chats.add(new Telegram());
		chats.add(new HTTP());
        logger.info("Registered "+chats.size()+" chat instances.");

        logger.info("Trying to connect chat instances.");
        chats.forEach(WillyChat::connect);

        logger.info("Initialization successful.");
        logger.info("Welcome to Willy bot!");

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if(stop) executor.shutdown();
            chats.forEach(WillyChat::refresh);
            Context.refresh();
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }
}