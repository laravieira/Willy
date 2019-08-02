package net.jwdouglas.willy;

import java.util.Scanner;
import java.util.logging.Logger;

public class Willy {
	
	// Java Application Willy
	private static final String my_name    = "Willy";
	private static final int[]  my_version = {0,0,1};
	private static final String my_release = "SNAPSHOT";
	private static final String my_descrpt = "Willy it's your best, beautiful, little and cute friend. He will help to do everything possible.";
	private static final String my_image   = "/src/main/resources/profile.jpg";
	
	// Application Variables
	private static Logger  log        = null;
	private static boolean stop       = false;
	
	public static void main(String[] args) throws InterruptedException {
		MyLogger.load();
		log = MyLogger.getLogger();
    	log.info("Willy is starting!");
    	new Config().loadConfig();
    	
    	if(!stop) {Watson.start();}
    	if(!stop) {log.info("Watson instance has been openned.");}
    	
    	if(!stop) {Discord.login();}
    	if(!stop) {log.info("Discord instance has benn openned.");}
    	
    	if(!stop) {lineReaderStart();}
    	if(!stop) {log.info("Welcome to Willy bot. Talk with him on Discord.");}
    	
    	while(!stop) {loop(); Thread.sleep(1);}
    	
    	log.info("Willy is goin down, please! Come back Willy!");
    	Discord.logout();
    	Watson.finish();
    	log.info("Willy has been stopped.");
    }
	
	private static void loop() {
		Watson.refreshSession();
		Core.clearChannel();
	}
    
    private static void lineReaderStart() {
    	Thread lineReader = new Thread(() -> {
	    	@SuppressWarnings("resource")
			Scanner input   = new Scanner(System.in);
    		while(!stop && input.hasNextLine()) {
    			String commandLine = input.nextLine();
    			if(!commandLine.isEmpty()) {
    				Thread command = new Thread(() -> {
    					Command.onCommand(commandLine.split(" "));
    				});
    				command.setDaemon(true);
    				command.start();
    				try {
    					command.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    			}
    		}
    	});
    	lineReader.setDaemon(true);
    	lineReader.start();
    }
    
    public static void stop() {
    	stop = true;
    }
    
    // Get Application
    public static String getName()         {return my_name;}
    public static int    getVersion(int i) {return my_version[i];}
    public static String getDescription()  {return my_descrpt;}
    public static String getRelease()      {return my_release.isEmpty()?"":"-"+my_release;}
    public static String getFullVersion()  {return "v"+my_version[0]+"."+my_version[1]+"."+my_version[2];}
    public static String getProfilePath()  {return my_image;}
    
}