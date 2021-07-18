package me.laravieira.willy;

import java.util.Date;
import java.util.logging.Logger;

import me.laravieira.willy.config.Config;
import me.laravieira.willy.config.MyLogger;
import me.laravieira.willy.discord.Discord;
import me.laravieira.willy.discord.DiscordMessage;
import me.laravieira.willy.kernel.Context;
import me.laravieira.willy.watson.Watson;
import me.laravieira.willy.web.Server;

public class Willy {
	
	private static final String my_name    = "Willy";
	private static final int[]  my_version = {0,13,0};
	private static final String my_release = "SNAPSHOT";
	private static final String my_descrpt = "Willy it's your best, beautiful, little and cute friend. He will help to do everything possible.";
	private static final String my_image   = "/src/main/resources/profile.jpg";
	private static final long   start_time = new Date().getTime();

	private static Logger  log   = null;
	private static boolean stop  = false;
	
	private static void onStart() {
    	if(!stop) Watson.start();
    	if(!stop) Discord.login();
    	if(!stop) Server.load();
	}
	
	private static void onLoop() {
		DiscordMessage.refresh();
		Context.refresh();
		Watson.refresh();
		Server.refresh();
	}
	
	private static void onClose() {
    	Server.close();
    	Discord.logout();
    	Watson.finish();
	}

	public static void main(String[] args) throws InterruptedException {
		MyLogger.load();
		log = MyLogger.getLogger();
    	log.info("Willy is starting! "+getFullVersion());
    	Config.loadConfig();
    	
    	onStart();
    	
    	if(!stop) {Command.startLineReader();}
    	if(!stop) {log.info("Welcome to Willy bot. Talk with him on Discord.");}
    	
    	while(!stop) {onLoop(); Thread.sleep(1);}
    	
    	log.info("Willy is goin down, please! Come back Willy!");
    	onClose();
    	log.info("Willy has been stopped.");
    	MyLogger.close();
    	System.exit(0);
    }
	
    public static void stop() {
    	stop = true;
    }
    
    public static String  getName()         {return my_name;}
    public static int     getVersion(int i) {return my_version[i];}
    public static String  getDescription()  {return my_descrpt;}
    public static String  getRelease()      {return my_release.isEmpty()?"":"-"+my_release;}
    public static String  getFullVersion()  {return "v"+my_version[0]+"."+my_version[1]+"."+my_version[2];}
    public static String  getProfilePath()  {return my_image;}
    public static long    getStartTime()    {return start_time;}
    public static boolean getStop()         {return stop;}
    
}