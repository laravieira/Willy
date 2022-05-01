package me.laravieira.willy.internal;

import me.laravieira.willy.Willy;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
	public static Config config;

	public static void load() {
		File confFile = new File("config.yml");
		if(confFile.exists() && confFile.isFile())
			config = new Config("config.yml", false);
		else {
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
			sconf += "# General Config #\r\n";
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
			sconf += "\r\n";
			sconf += "# Watson Assistant Config #\r\n";
			sconf += "watson_assistant:\r\n";
			sconf += "    session_live: 5m\r\n";
			sconf += "    keep_alive: false\r\n";
			sconf += "    api_date: 2019-07-25\r\n";
			sconf += "    assistant_id: \r\n";
			sconf += "    credentials_password: \r\n";
			sconf += "\r\n";
			sconf += "\r\n";
			sconf += "# Chats Config #\r\n";
			sconf += "discord:\r\n";
			sconf += "    enable: false\r\n";
			sconf += "    client_id: \r\n";
			sconf += "    token: \r\n";
			sconf += "    verbuse: \r\n";
			sconf += "    keep_willy_nick: true\r\n";
			sconf += "    keep_master_nick: \r\n";
			sconf += "\r\n";
			sconf += "whatsapp:\r\n";
			sconf += "    enable: false\r\n";
			sconf += "    shared_chat: false\r\n";
			sconf += "\r\n";
			sconf += "telegram:\r\n";
			sconf += "    enable: false\r\n";
			sconf += "    token: \r\n";
			sconf += "\r\n";
			sconf += "\r\n";
			sconf += "# Features Config #\r\n";
			sconf += "audio_player:\r\n";
			sconf += "    enable: false\r\n";
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
			sconf += "bitly:\r\n";
			sconf += "    enable: false\r\n";
			sconf += "    token: \r\n";
			sconf += "\r\n";
			sconf += "youtube_downloader:\r\n";
			sconf += "    enable: false\r\n";
			sconf += "    willy_vpn: false\r\n";
			sconf += "    use_bitly: false\r\n";
			sconf += "\r\n";
			sconf += "web_server:\r\n";
			sconf += "    enable: true\r\n";
			sconf += "    uri: http://localhost/\r\n";
			sconf += "    port: 80\r\n";
			sconf += "\r\n";

			try {
				FileWriter confWriter = new FileWriter(confFile);
				confWriter.write(sconf, 0, sconf.length());
				confWriter.close();
				Willy.getLogger().warning("Config file has been created on application directory.");
				Willy.getLogger().warning("Setup config file and restart this application to apply new configs.");
				Willy.getWilly().stop();
				return;
			} catch (IOException e) {
				Willy.getLogger().severe("Can't create the config file, please check write and read system permissions.");
				Willy.getLogger().severe(e.getMessage());
				Willy.getWilly().stop();
				return;
			}
		}
	}

	private final Map<String, Object> yaml = new HashMap<>();

	Config(String path, boolean resource) {
		try {
			InputStream stream;
			if(resource)
				stream = Config.class.getClassLoader().getResource(path).openStream();
			else
				stream = new FileInputStream(path);
			Load load = new Load(LoadSettings.builder().build());
			yaml.putAll((Map<String, Object>)load.loadFromInputStream(stream));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Config(Object object) {
		yaml.putAll((Map<String, Object>)object);
	}

	private Object parse(String path) {
		String[] keywords = path.split("\\.");
		Object object = this.yaml;
		for(String keyword : keywords) {
			if(object instanceof Map) {
				Config yaml = new Config(object);
				object = yaml.has(keyword)?yaml.asObject(keyword):object;
			}
		}
		return object;
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

	public boolean has(String keyword) {
		return yaml.containsKey(keyword);
	}

	public Object asObject(String keyword) {
		return yaml.get(keyword);
	}
	public Config get(String keyword) {
		return new Config(parse(keyword));
	}
	public String asString(String keyword) {
		return (String)parse(keyword);
	}
	public int asInt(String keyword) {
		return (int)parse(keyword);
	}
	public long asLong(String keyword) {return Long.valueOf(""+parse(keyword));}
	public long asTimestamp(String keyword) {return parseTime((String)parse(keyword));}
	public float asFloat(String keyword) {
		return (float)parse(keyword);
	}
	public boolean asBoolean(String keyword) {
		return (boolean)parse(keyword);
	}
	public List asList(String keyword) {
		return (List)parse(keyword);
	}

	public boolean isString(String keyword) {
		return parse(keyword) instanceof String;
	}
	public boolean isInt(String keyword) {
		return parse(keyword) instanceof Integer;
	}
	public boolean isLong(String keyword) {
		return parse(keyword) instanceof Long;
	}
	public boolean isFloat(String keyword) {
		return parse(keyword) instanceof Float;
	}
	public boolean isBoolean(String keyword) {
		return parse(keyword) instanceof Boolean;
	}
	public boolean isList(String keyword) {
		return parse(keyword) instanceof List;
	}
}
