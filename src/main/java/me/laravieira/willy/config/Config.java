package me.laravieira.willy.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.yaml.snakeyaml.error.YAMLException;

import me.laravieira.willy.Willy;

public class Config {
	
	// To log files and prompt
	private static Logger log = MyLogger.getLogger();
	
	// Credentials
	private static String       name        = null;
	private static List<String> aliases     = null;
	private static List<String> ignore      = null;
	private static String       dis_id      = null;
	private static String       dis_token   = null;
	private static String       dis_verbuse = null;
	private static String       pl_channel  = null;
	private static boolean      pl_activity = false;
	private static List<String> pl_blends   = null;
	private static String       master_nick = null;
	private static boolean      willy_nick  = false;
	private static String       wat_date    = null;
	private static String       wat_id      = null;
	private static String       wat_pass    = null;
	private static boolean      wat_alive   = false;
	private static boolean      clear_chat  = false;
	private static long         ctxt_life   = 0;
	private static long         wat_live    = 0;
	private static long         clear_time  = 0;
	private static boolean      web_use     = false;
	private static String       web_uri     = null;
	private static int          web_port    = 0;
	private static boolean      bitly_use   = false;
	private static String       bitly_key   = null;
	private static boolean      ytd_vpn     = false;
	private static boolean      ytd_bitly   = false;
	
	private static boolean setConfig(File file) throws YAMLException, FileNotFoundException, NullPointerException {
		Yaml config = new Yaml(file);
		
		name        = config.get("willy_name").asString();
		aliases     = config.get("willy_aliases").asStringList();
		ignore      = config.get("ignore_start_with").asStringList();
		clear_chat  = config.get("clear_public_chats").asBoolean();  
		dis_id      = config.get("discord").get("client_id").asLong().toString();
		dis_token   = config.get("discord").get("token").asString();
		dis_verbuse = config.get("discord").get("verbuse").asLong().toString();
		pl_activity = config.get("audio_player").get("change_activity").asBoolean();
		pl_channel  = config.get("audio_player").get("command_default_channel_id").asLong().toString();
		pl_blends   = config.get("audio_player").get("blends_for_play").asStringList();
		willy_nick  = config.get("discord").get("keep_willy_nick").asBoolean();
		master_nick = config.get("discord").get("keep_master_nick").asLong().toString();
		wat_alive   = config.get("watson_assistant").get("keep_alive").asBoolean();
		wat_date    = config.get("watson_assistant").get("api_date").asString();
		wat_id      = config.get("watson_assistant").get("assistant_id").asString();
		wat_pass    = config.get("watson_assistant").get("credentials_password").asString();
		bitly_use   = config.get("bitly").get("use").asBoolean();
		bitly_key   = config.get("bitly").get("token").asString();
		ytd_vpn     = config.get("youtube_downloader").get("willy_vpn").asBoolean();
		ytd_bitly   = config.get("youtube_downloader").get("use_bitly").asBoolean();
		web_use     = config.get("web_server").get("use").asBoolean();
		web_uri     = config.get("web_server").get("uri").asString();
		web_port    = config.get("web_server").get("port").asInt();

		// Parse time variables
		ctxt_life  = parseTime(config.get("user_context_life_time").asString());
		clear_time = parseTime(config.get("clear_after_wait").asString());
		wat_live   = parseTime(config.get("watson_assistant").get("session_live").asString());

		// Check exceptions
		if(ctxt_life < 1000*30) return false;
		if(clear_time == 0 && clear_chat) return false;
		if(wat_live == 0) return false;
		if(name.length() < 3) return false;
		if(!(config.get("watson_assistant").get("api_date").asObject() instanceof Date)) return false;
		if(bitly_use && bitly_key.isEmpty()) return false;

		return true;
	}

	public static boolean loadConfig() {
		File confFile = new File("config.yml");
		if(confFile.exists() && confFile.isFile()) {
			try {
				if(!setConfig(confFile)) {
					log.severe("Can't read the config file. Please check it or delete it.");
					Willy.stop();
					return false;
				}else return true;
			}catch(Exception e) {
				log.severe("Can't read the config file. Please check it or delete it.");
				log.severe(e.getMessage());
				Willy.stop();
				return false;
			}
		}else {
			
			String sconf = "";
			sconf += "# ---------------------------------------------------------- #\r\n";
			sconf += "#                                                            #\r\n";
			sconf += "#                    Willy Bot Config File                   #\r\n";
			sconf += "#                                                            #\r\n";
			sconf += "#                                                            #\r\n";
			sconf += "#      For help check https://github.com/JWDouglas/Willy     #\r\n";
			sconf += "#                                                            #\r\n";
			sconf += "# ---------------------------------------------------------- #\r\n";
			sconf += "\r\n";
			sconf += "\r\n";
			sconf += "willy_name: Willy\r\n";
			sconf += "willy_aliases:\r\n";
			sconf += "    - willy\r\n";
			sconf += "    - Wily\r\n";
			sconf += "    - wily\r\n";
			sconf += "    - Wil\r\n";
			sconf += "    - wil\r\n";
			sconf += "    - illy\r\n";
			sconf += "    - ily\r\n";
			sconf += "\r\n";
			sconf += "user_context_life_time: 1h\r\n";
			sconf += "\r\n";
			sconf += "clear_public_chats: true\r\n";
			sconf += "clear_after_wait: 20m\r\n";
			sconf += "\r\n";
			sconf += "discord:\r\n";
			sconf += "    client_id: \r\n";
			sconf += "    token: \r\n";
			sconf += "    verbuse: \r\n";
			sconf += "    keep_willy_nick: true\r\n";
			sconf += "    keep_master_nick: \r\n";
			sconf += "\r\n";
			sconf += "audio_player:\r\n";
			sconf += "    change_activity: true\r\n";
			sconf += "    command_default_channel_id: \r\n";
			sconf += "    \r\n";
			sconf += "    blends_for_play:\r\n";
			sconf += "        - Willy play\r\n";
			sconf += "        - Willy toca\r\n";
			sconf += "        - Willy adiciona\r\n";
			sconf += "        - Willy reproduz\r\n";
			sconf += "        - Willy reproduza\r\n";
			sconf += "        - Willy toque\r\n";
			sconf += "        - Willy coloca\r\n";
			sconf += "\r\n";
			sconf += "watson_assistant:\r\n";
			sconf += "    session_live: 5m\r\n";
			sconf += "    keep_alive: false\r\n";
			sconf += "    api_date: 2019-07-25\r\n";
			sconf += "    assistant_id: \r\n";
			sconf += "    credentials_password: \r\n";
			sconf += "\r\n";
			sconf += "bitly:\r\n";
			sconf += "    use: false\r\n";
			sconf += "    token: \r\n";
			sconf += "\r\n";
			sconf += "youtube_downloader:\r\n";
			sconf += "    willy_vpn: false\r\n";
			sconf += "    use_bitly: false\r\n";
			sconf += "\r\n";
			sconf += "web_server:\r\n";
			sconf += "    use: true\r\n";
			sconf += "    uri: http://localhost/\r\n";
			sconf += "    port: 80\r\n";
			sconf += "\r\n";
			
			try {
				FileWriter confWriter = new FileWriter(confFile);
				confWriter.write(sconf, 0, sconf.length());
				confWriter.close();
				log.warning("Config file has been created on application directory.");
				log.warning("Setup config file and restart this application to apply new configs.");
				Willy.stop();
				return false;
			} catch (IOException e) {
				log.severe("Can't create the config file, please check write and read system permissions.");
				log.severe(e.getMessage());
				Willy.stop();
				return false;
			}
		}
	}
	
	private static long parseTime(String time) {
		long value = 0; time = time.toLowerCase();
		if((time.contains("s") && Integer.parseInt(time.split("s")[0]) > 0)) {
			value = 1000*Integer.parseInt(time.split("s")[0]);
		}else if((time.contains("m") && Integer.parseInt(time.split("m")[0]) > 0)) {
			value = 60000*Integer.parseInt(time.split("m")[0]);
		}else if((time.contains("h") && Integer.parseInt(time.split("h")[0]) > 0)) {
			value = 3600000*Integer.parseInt(time.split("h")[0]);
		}else if((time.contains("d") && Integer.parseInt(time.split("d")[0]) > 0)) {
			value = 86400000*Integer.parseInt(time.split("d")[0]);
		}
		return value;
	}

	public static String       getName()                   {return name;}
	public static List<String> getAliases()                {return aliases;}
	public static List<String> getIgnoreStartWith()        {return ignore;}
	public static boolean      getClearChats()             {return clear_chat;}
	public static long         getContextLifeTime()        {return ctxt_life;}
	public static long         getClearTime()              {return clear_time;}
	public static String       getDiscordID()              {return dis_id;}
	public static String       getDiscordToken()           {return dis_token;}
	public static String       getDiscordVerbuse()         {return dis_verbuse;}
	public static boolean      getPlayerChangeActivity()   {return pl_activity;}
	public static String       getPlayerDefaultChannel()   {return pl_channel;}
	public static List<String> getPlayerPlayAskKeys()      {return pl_blends;}
	public static long         getWatsonSessionLive()      {return wat_live;}
	public static boolean      getDiscordKeepNickWilly()   {return willy_nick;}
	public static String       getDiscordKeepNickMaster()  {return master_nick;}
	public static String       getWatsonAPIDate()          {return wat_date;}
	public static String       getWatsonID()               {return wat_id;}
	public static String       getWatsonPassword()         {return wat_pass;}
	public static boolean      getWatsonKeepSessionAlive() {return wat_alive;}
	public static boolean      getWebUse()                 {return web_use;}
	public static boolean      getBitlyUse()               {return bitly_use;}
	public static String       getBitlyToken()             {return bitly_key;}
	public static boolean      getYoutubeUseWillyVPN()     {return ytd_vpn;}
	public static boolean      getYoutubeUseBitly()        {return ytd_bitly;}
	public static String       getWebUri()                 {return web_uri;}
	public static int          getWebPort()                {return web_port;}
}