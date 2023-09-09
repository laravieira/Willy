package me.laravieira.willy;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import me.laravieira.willy.chat.chatgpt.ChatGPT;
import me.laravieira.willy.chat.whatsapp.Whatsapp;
import me.laravieira.willy.internal.WillyChat;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.chat.telegram.Telegram;
import me.laravieira.willy.internal.logger.WillyLogger;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;

public class Willy {
	
	private static final String my_name    = "Willy";
	private static final int[]  my_version = {0,17,0};
	private static final long   start_time = new Date().getTime();

	private static final Willy willy = new Willy();
	private static final WillyLogger logger = new WillyLogger();

	private final ArrayList<WillyChat> chats = new ArrayList<>();
	private boolean stop  = false;

	public static void main(String[] args) {
		willy.run();
    }

    public static Willy       getWilly()  {return willy;}
	public static WillyLogger getLogger() {return logger;}

	public void stop() {
    	stop = true;
    }
    
    public String  getName()         {return my_name;}
    public String  getFullVersion()  {return "v"+my_version[0]+"."+my_version[1]+"."+my_version[2];}
    public long    getStartTime()    {return start_time;}
	public boolean getStop() {return stop;}

	private void registryChats() {
		willy.addWillyChatInstance(new ChatGPT());
		willy.addWillyChatInstance(new Discord());
		willy.addWillyChatInstance(new Whatsapp());
		willy.addWillyChatInstance(new Telegram());
	}

	private void onShutdown() {
		if(!stop)
			stop = true;

		logger.info("Closing Willy!");
		disconnectWillyChatInstances();
		logger.info("Closing process finished.");
		logger.close();
	}

	private void run() {
		Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));
		logger.info("Starting Willy! ("+getFullVersion()+")");

		Config.load();
		logger.info("Configurations loaded.");

		registryChats();
		logger.info("Registered "+chats.size()+" chat instances.");

		logger.info("Trying to connect chat instances.");
		connectWillyChatInstances();

		logger.info("Startup completed.");
		logger.info("Welcome to Willy bot!");

		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> {
			if(stop)
				executor.shutdown();
			refreshWillyChatInstances();
			ContextStorage.refresh();
			MessageStorage.refresh();
		}, 0, 1000, TimeUnit.MILLISECONDS);
	}

	private void connectWillyChatInstances() {
		chats.forEach(WillyChat::connect);
	}

	private void refreshWillyChatInstances() {
		chats.forEach(WillyChat::refresh);
	}

	private void disconnectWillyChatInstances() {
		chats.forEach(WillyChat::disconnect);
	}

	public void addWillyChatInstance(WillyChat chat) {
		chats.add(chat);
	}
}