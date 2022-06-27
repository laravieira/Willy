package me.laravieira.willy;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import me.laravieira.willy.internal.WillyChat;
import me.laravieira.willy.command.Command;
import me.laravieira.willy.internal.Config;
import me.laravieira.willy.chat.discord.Discord;
import me.laravieira.willy.chat.telegram.Telegram;
import me.laravieira.willy.internal.logger.WillyLogger;
import me.laravieira.willy.kernel.Context;
import me.laravieira.willy.openai.OpenAi;
import me.laravieira.willy.watson.Watson;
import me.laravieira.willy.chat.whatsapp.Whatsapp;

public class Willy {
	
	private static final String my_name    = "Willy";
	private static final int[]  my_version = {0,15,1};
	private static final String my_release = "SNAPSHOT";
	private static final String my_descrpt = "Willy it's your best, beautiful, little and cute friend. He will help to do everything possible.";
	private static final String my_image   = "/src/main/resources/profile.jpg";
	private static final long   start_time = new Date().getTime();

	private static final Willy willy = new Willy();
	private static final WillyLogger logger = new WillyLogger();
	private static Config config;

	private final ArrayList<WillyChat> chats = new ArrayList<>();
	private boolean stop  = false;

	public static void main(String[] args) {
		willy.run();
    }

    public static Willy       getWilly()  {return willy;}
	public static Config      getConfig() {return config;}
	public static WillyLogger getLogger() {return logger;}

	public void stop() {
    	stop = true;
    }
    
    public String  getName()         {return my_name;}
    public int     getVersion(int i) {return my_version[i];}
    public String  getDescription()  {return my_descrpt;}
    public String  getRelease()      {return my_release.isEmpty()?"":"-"+my_release;}
    public String  getFullVersion()  {return "v"+my_version[0]+"."+my_version[1]+"."+my_version[2];}
    public String  getProfilePath()  {return my_image;}
    public long    getStartTime()    {return start_time;}
	public boolean getStop() {return stop;}

	private void registryChats() {
		willy.addWillyChatInstance(new OpenAi());
		willy.addWillyChatInstance(new Watson());
		willy.addWillyChatInstance(new Discord());
		willy.addWillyChatInstance(new Whatsapp());
		willy.addWillyChatInstance(new Telegram());
	}

	private void run() {
		logger.info("Starting Willy! ("+getFullVersion()+")");

		Config.load();
		logger.info("Configurations loaded.");

		registryChats();
		logger.info("Registered "+chats.size()+" chat instances.");

		logger.info("Trying to connect chat instances.");
		connectWillyChatInstances();

		Command.startLineReader();
		logger.info("Startup completed.");
		logger.info("Welcome to Willy bot!");

		while(!stop) {
			refreshWillyChatInstances();
			Context.refresh();

			try {Thread.sleep(1);
			}catch (InterruptedException ignore) {}
		}

		logger.info("Closing Willy!");
		disconnectWillyChatInstances();
		logger.info("Closing process finished.");
		logger.close();
		System.exit(0);
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

	public void removeWillyChatInstance(String name) {
		chats.remove(name);
	}

	public ArrayList<WillyChat> getWillyChatInstances() {return chats;}
}