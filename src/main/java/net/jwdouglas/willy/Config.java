package net.jwdouglas.willy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class Config {
	
	// To log files and prompt
	private static Logger log = MyLogger.getLogger();
	
	// Credentials
	private static String       name       = null;
	private static List<String> aliases    = null;
	private static String       dis_id     = null;
	private static String       dis_token  = null;
	private static String       wat_date   = null;
	private static String       wat_id     = null;
	private static String       wat_pass   = null;
	private static boolean      clear_chat = false;
	private static long         clear_time = 0;
	
	private boolean setConfig(File file) throws FileNotFoundException {
		GetterYAML config = new GetterYAML(file);
		if(config.load()) {
			
			// Check if maps and lists exists
			if(!config.hasString("willy_name"))          return false;
			if(!config.hasList("willy_aliases"))         return false;
			if(!config.hasBoolean("clear_public_chats")) return false;
			if(!config.hasString("clear_after_wait"))    return false;
			if(!config.hasMap("discord"))                return false;
			if(!config.hasMap("watson_assistant"))       return false;

			// Check if keys exist on maps
			if(!config.getMap("discord"         ).containsKey("client_id"))            return false;
			if(!config.getMap("discord"         ).containsKey("token"))                return false;
			if(!config.getMap("watson_assistant").containsKey("api_date"))             return false;
			if(!config.getMap("watson_assistant").containsKey("assistant_id"))         return false;
			if(!config.getMap("watson_assistant").containsKey("credentials_password")) return false;

			// Check if keys are empty
			if(config.getString("willy_name"   ).isEmpty()) return false;
			if(config.getMap("discord"         ).get("client_id"           ).toString().isEmpty()) return false;
			if(config.getMap("discord"         ).get("token"               ).toString().isEmpty()) return false;
			if(config.getMap("watson_assistant").get("api_date"            ).toString().isEmpty()) return false;
			if(config.getMap("watson_assistant").get("assistant_id"        ).toString().isEmpty()) return false;
			if(config.getMap("watson_assistant").get("credentials_password").toString().isEmpty()) return false;
			
			// Check exceptions
			if(config.getString("willy_name").length() < 3) return false;
			if(!(config.getMap("watson_assistant").get("api_date") instanceof Date)) return false;
			
			// Set variables
			if((config.getString("clear_after_wait").contains("s") && Integer.parseInt(config.getString("clear_after_wait").split("s")[0]) > 0)) {
				clear_time = 1000*Integer.parseInt(config.getString("clear_after_wait").split("s")[0]);
			}else if((config.getString("clear_after_wait").contains("m") && Integer.parseInt(config.getString("clear_after_wait").split("m")[0]) > 0)) {
				clear_time = 60000*Integer.parseInt(config.getString("clear_after_wait").split("m")[0]);
			}else if((config.getString("clear_after_wait").contains("h") && Integer.parseInt(config.getString("clear_after_wait").split("h")[0]) > 0)) {
				clear_time = 3600000*Integer.parseInt(config.getString("clear_after_wait").split("h")[0]);
			}else if((config.getString("clear_after_wait").contains("d") && Integer.parseInt(config.getString("clear_after_wait").split("d")[0]) > 0)) {
				clear_time = 86400000*Integer.parseInt(config.getString("clear_after_wait").split("d")[0]);
			}else if(config.getBoolean("clear_public_chats")) return false;
			name       = config.getString("willy_name");
			aliases    = config.getList("willy_aliases");
			clear_chat = config.getBoolean("clear_public_chats");
			dis_id     = config.getMap("discord"          ).get("client_id"           ).toString();
			dis_token  = config.getMap("discord"          ).get("token"               ).toString();
			wat_date   = new SimpleDateFormat("yyyy-MM-dd").format((Date)config.getMap("watson_assistant").get("api_date"));
			wat_id     = config.getMap("watson_assistant" ).get("assistant_id"        ).toString();
			wat_pass   = config.getMap("watson_assistant" ).get("credentials_password").toString();
			
			return true;
		}else return false;
	}

	public boolean loadConfig() {
		File confFile = new File("config.yml");
		if(confFile.exists() && confFile.isFile()) {
			try {
				if(!setConfig(confFile)) {
					log.severe("Can't read the config file. Please check it or delete it.");
					Willy.stop();
					return false;
				}else return true;
			}catch(YAMLException | FileNotFoundException e) {
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
			sconf += "clear_public_chats: true\r\n";
			sconf += "clear_after_wait: 20m\r\n";
			sconf += "\r\n";
			sconf += "discord:\r\n";
			sconf += "    client_id: \r\n";
			sconf += "    token: \r\n";
			sconf += "\r\n";
			sconf += "watson_assistant:\r\n";
			sconf += "    api_date: 2019-07-25\r\n";
			sconf += "    assistant_id: \r\n";
			sconf += "    credentials_password: \r\n";
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

	public static String       getName()           {return name;}
	public static List<String> getAliases()        {return aliases;}
	public static boolean      getClearChats()     {return clear_chat;}
	public static long         getClearTime()      {return clear_time;}
	public static String       getDiscordID()      {return dis_id;}
	public static String       getDiscordToken()   {return dis_token;}
	public static String       getWatsonAPIDate()  {return wat_date;}
	public static String       getWatsonID()       {return wat_id;}
	public static String       getWatsonPassword() {return wat_pass;}
}

class GetterYAML {
	private static Map<String, Object> yaml  = null;
	private        File                _file = null;
	
	public GetterYAML(File file) {
		if(file.exists() && file.isFile())
			_file = file;
	}
	
	public boolean load() {
		if(_file == null) return false;
		try {
			yaml = new Yaml().load(new FileReader(_file));
			return true;
		} catch (YAMLException | FileNotFoundException e) {
			return false;
		}
	}

	public boolean hasMap(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return false;
			if(object instanceof Map<?,?>) {
				return true;
			}else return false;
		}else return false;
	}

	public boolean hasList(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return false;
			if(object instanceof List<?>) {
				return true;
			}else return false;
		}else return false;
	}
	
	public boolean hasString(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return false;
			if(object instanceof String)
				return true;
			else return false;
		}else return false;
	}
	
	public boolean hasBoolean(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return false;
			if(object instanceof Boolean)
				return true;
			else return false;
		}else return false;
	}
	
	public boolean hasInt(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return false;
			if(object instanceof Integer)
				return true;
			else return false;
		}else return false;
	}
	
	public boolean hasDouble(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return false;
			if(object instanceof Double)
				return true;
			else return false;
		}else return false;
	}

	public Map<String, Object> getMap(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return new HashMap<String, Object>();
			if(object instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>)object;
				if(map.isEmpty()) return new HashMap<String, Object>();
					return map;
			}else return new HashMap<String, Object>();
		}else return new HashMap<String, Object>();
	}

	public List<String> getList(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return new ArrayList<String>();
			if(object instanceof List) {
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>)object;
				if(list.isEmpty()) return new ArrayList<String>();
					return list;
			}else return new ArrayList<String>();
		}else return new ArrayList<String>();
	}
	
	public String getString(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return "";
			if(object instanceof String)
				return object.toString();
			else return "";
		}else return "";
	}
	
	public boolean getBoolean(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return false;
			if(object instanceof Boolean)
				return (boolean)object;
			else return false;
		}else return false;
	}

	public int getInt(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return 0;
			if(object instanceof Integer)
				return (int)object;
			else return 0;
		}else return 0;
	}
	
	public double getDouble(String key) {
		if(yaml.containsKey(key)) {
			Object object = yaml.get(key);
			if(object == null) return 0;
			if(object instanceof Double)
				return (double)object;
			else return 0;
		}else return 0;
	}

}

