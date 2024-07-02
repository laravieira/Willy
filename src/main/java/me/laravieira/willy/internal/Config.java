package me.laravieira.willy.internal;

import me.laravieira.willy.Willy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
public class Config {
	private final static int TYPE_INT = 1;
	private final static int TYPE_STRING = 2;
	private final static int TYPE_BOOLEAN = 3;
	private final static int TYPE_LONG = 4;
	private final static int TYPE_LIST = 5;
	private final static int TYPE_TIME = 6;

	private final static Map<String, Object> settings = new HashMap<>();
	private static Yaml configFile;
	private static boolean firstLoad = true;

	public static void load() {
		loadConfigFile();

		List<String> names = new ArrayList<>();
		names.add(Willy.getWilly().getName());
		names.add(Willy.getWilly().getName().toLowerCase());

		List<String> ignore = new ArrayList<>();
		ignore.add("!");
		ignore.add("?");
		ignore.add("@");
		ignore.add("/");
		ignore.add("\\");
		ignore.add("//");
		ignore.add("#");

		// Overall Settings
		set("environment",           "ENVIRONMENT",                    "environment",             TYPE_STRING, "development");
		set("context_lifetime",      "WILLY_CONTEXT_LIFETIME",         "context-life-time",       TYPE_TIME,   parseTime("5m"));

		// Discord settings
		set("discord.enable",           "WILLY_DISCORD_ENABLE",           "discord.enable",           TYPE_BOOLEAN, false);
		set("discord.client_id",        "WILLY_DISCORD_CLIENT_ID",        "discord.client-id",        TYPE_LONG,    null);
		set("discord.token",            "WILLY_DISCORD_TOKEN",            "discord.token",            TYPE_STRING,  null);
		set("discord.keep_willy_nick",  "WILLY_DISCORD_KEEP_NICK_WILLY",  "discord.keep-willy-nick",  TYPE_BOOLEAN, true);
		set("discord.keep_master_nick", "WILLY_DISCORD_KEEP_NICK_MASTER", "discord.keep-master-nick", TYPE_LONG,    null);
		set("discord.admin.guild",      "WILLY_DISCORD_ADMIN_GUILD",      "discord.admin.guild",      TYPE_LONG,    null);
		set("discord.admin.command",    "WILLY_DISCORD_ADMIN_COMMAND",    "discord.admin.command",    TYPE_LONG,    null);
		set("discord.admin.log",        "WILLY_DISCORD_ADMIN_LOG",        "discord.admin.log",        TYPE_LONG,    null);
		set("discord.public_chat.enable",                        "WILLY_DISCORD_PUBLIC_CHAT_ENABLE",                        "discord.public-chat.enable",                        TYPE_BOOLEAN, true);
		set("discord.public_chat.willy_names",                   "WILLY_DISCORD_PUBLIC_CHAT_WILLY_NAMES",                   "discord.public-chat.willy-names",                   TYPE_LIST, names);
		set("discord.public_chat.ignore_start_with",             "WILLY_DISCORD_PUBLIC_CHAT_IGNORE_START_WITH",             "discord.public-chat.ignore-start-with",             TYPE_LIST, ignore);
		set("discord.public_chat.auto_delete.willy_messages",    "WILLY_DISCORD_PUBLIC_CHAT_AUTO_DELETE_WILLY_MESSAGES",    "discord.public-chat.auto-delete.willy-messages",    TYPE_BOOLEAN, false);
		set("discord.public_chat.auto_delete.delete_after_wait", "WILLY_DISCORD_PUBLIC_CHAT_AUTO_DELETE_DELETE_AFTER_WAIT", "discord.public-chat.auto-delete.delete-after-wait", TYPE_TIME, parseTime("10m"));

		// OpenAI settings
		set("openai.enable", "WILLY_OPENAI_ENABLE", "openai.enable", TYPE_BOOLEAN, false);
		set("openai.token", "WILLY_OPENAI_TOKEN", "openai.token", TYPE_STRING, null);
		set("openai.dall_e", "WILLY_OPENAI_DALL_E", "openai.dall-e", TYPE_BOOLEAN, false);

		// Whatsapp Settings
		set("whatsapp.enable",                              "WILLY_WHATSAPP_ENABLE",                              "whatsapp.enable",                              TYPE_BOOLEAN, false);
		set("whatsapp.phone_number",                        "WILLY_WHATSAPP_PHONE_NUMBER",                        "whatsapp.phone-number",                        TYPE_LONG, null);
		set("whatsapp.group.enable",                        "WILLY_WHATSAPP_GROUP_ENABLE",                        "whatsapp.group.enable",                        TYPE_BOOLEAN, true);
		set("whatsapp.group.willy_names",                   "WILLY_WHATSAPP_GROUP_WILLY_NAMES",                   "whatsapp.group.willy-names",                   TYPE_LIST, names);
		set("whatsapp.group.ignore_start_with",             "WILLY_WHATSAPP_GROUP_IGNORE_START_WITH",             "whatsapp.group.ignore-start-with",             TYPE_LIST, ignore);
		set("whatsapp.group.auto_delete.willy_messages",    "WILLY_WHATSAPP_GROUP_AUTO_DELETE_WILLY_MESSAGES",    "whatsapp.group.auto-delete.willy-messages",    TYPE_BOOLEAN, false);
		set("whatsapp.group.auto_delete.delete_after_wait", "WILLY_WHATSAPP_GROUP_AUTO_DELETE_DELETE_AFTER_WAIT", "whatsapp.group.auto-delete.delete-after-wait", TYPE_TIME, parseTime("10m"));

		// Telegram Settings
		set("telegram.enable",                              "WILLY_TELEGRAM_ENABLE",                              "telegram.enable",                              TYPE_BOOLEAN, false);
		set("telegram.token",                               "WILLY_TELEGRAM_TOKEN",                               "telegram.token",                               TYPE_STRING,  null);
		set("telegram.group.enable",                        "WILLY_TELEGRAM_GROUP_ENABLE",                        "telegram.group.enable",                        TYPE_BOOLEAN, true);
		set("telegram.group.willy_names",                   "WILLY_TELEGRAM_GROUP_WILLY_NAMES",                   "telegram.group.willy-names",                   TYPE_LIST, names);
		set("telegram.group.ignore_start_with",             "WILLY_TELEGRAM_GROUP_IGNORE_START_WITH",             "telegram.group.ignore-start-with",             TYPE_LIST, ignore);
		set("telegram.group.auto_delete.willy_messages",    "WILLY_TELEGRAM_GROUP_AUTO_DELETE_WILLY_MESSAGES",    "telegram.group.auto-delete.willy-messages",    TYPE_BOOLEAN, false);
		set("telegram.group.auto_delete.delete_after_wait", "WILLY_TELEGRAM_GROUP_AUTO_DELETE_DELETE_AFTER_WAIT", "telegram.group.auto-delete.delete-after-wait", TYPE_TIME, parseTime("10m"));

		// Bitly Settings
		set("bitly.enable", "WILLY_BITLY_ENABLE", "bitly.enable", TYPE_BOOLEAN, false);
		set("bitly.token",  "WILLY_BITLY_TOKEN",  "bitly.token",  TYPE_STRING,  null);

		// HTTP API Settings
		set("http_api.enable", "WILLY_HTTP_API_ENABLE", "http-api.enable", TYPE_BOOLEAN, false);
		set("http_api.port",   "WILLY_HTTP_API_PORT",   "http-api.port",   TYPE_INT,     8080);
	}

	private static void set(String key, String envKey, String fileKey, int type, Object defaultValue) {
		if(configFile.has(fileKey)) {
			switch (type) {
				case TYPE_STRING  -> { if(configFile.isString(fileKey))  settings.put(key, configFile.asString(fileKey)); }
				case TYPE_INT     -> { if(configFile.isInt(fileKey))     settings.put(key, configFile.asInt(fileKey)); }
				case TYPE_LONG    -> { if(configFile.isLong(fileKey))    settings.put(key, configFile.asLong(fileKey)); }
				case TYPE_BOOLEAN -> { if(configFile.isBoolean(fileKey)) settings.put(key, configFile.asBoolean(fileKey)); }
				case TYPE_TIME    -> { if(configFile.isString(fileKey))  settings.put(key, parseTime(configFile.asString(fileKey))); }
				case TYPE_LIST    -> { if(configFile.isList(fileKey))    settings.put(key, configFile.asList(fileKey)); }
				default -> settings.put(key, configFile.asObject(fileKey));
			}
		}
		if(System.getenv().get(envKey) != null) {
			switch (type) {
				case TYPE_INT     -> settings.put(key, Integer.parseInt(System.getenv(envKey)));
				case TYPE_LONG    -> settings.put(key, Long.parseLong(System.getenv(envKey)));
				case TYPE_BOOLEAN -> settings.put(key, Boolean.parseBoolean(System.getenv(envKey)));
				case TYPE_TIME    -> settings.put(key, parseTime(System.getenv(envKey)));
				case TYPE_LIST    -> settings.put(key, Arrays.stream(System.getenv(envKey).split("\\|")).toList());
				default -> settings.put(key, System.getenv(envKey));
			}
		}
		settings.putIfAbsent(key, defaultValue);
	}

	public static void loadConfigFile() {
		File confFile = new File("config.yml");
		Willy.getLogger().info("Config path is: "+confFile.getAbsolutePath());
		if(confFile.exists() && confFile.isFile())
			configFile = new Yaml("config.yml", false);
		else {
			String sconf = "";
			sconf += "# ---------------------------------------------------------- #\r\n";
			sconf += "#                                                            #\r\n";
			sconf += "#                    Willy Bot Config File                   #\r\n";
			sconf += "#                                                            #\r\n";
			sconf += "#     For help check https://github.com/laravieira/Willy     #\r\n";
			sconf += "#                                                            #\r\n";
			sconf += "# ---------------------------------------------------------- #\r\n";
			sconf += "\r\n";
			sconf += "environment: development\r\n";
			sconf += "context-life-time: 5m\r\n";
			sconf += "\r\n";
			sconf += "discord:\r\n";
			sconf += "    enable: false\r\n";
			sconf += "    client-id: \r\n";
			sconf += "    token: \r\n";
			sconf += "    keep-willy-nick: true\r\n";
			sconf += "    keep-master-nick: \r\n";
			sconf += "    public-chat:\r\n";
			sconf += "        enable: true\r\n";
			sconf += "        willy-names:\r\n";
			sconf += "            - Willy\r\n";
			sconf += "            - willy\r\n";
			sconf += "        ignore-start-with:\r\n";
			sconf += "            - \"!\"\r\n";
			sconf += "            - \"?\"\r\n";
			sconf += "            - \"@\"\r\n";
			sconf += "            - \"/\"\r\n";
			sconf += "            - \"#\"\r\n";
			sconf += "        auto-delete:\r\n";
			sconf += "            willy-messages: false\r\n";
			sconf += "            delete-after-wait: 10m\r\n";
			sconf += "    admin:\r\n";
			sconf += "        guild: \r\n";
			sconf += "        command: \r\n";
			sconf += "        log: \r\n";
			sconf += "\r\n";
			sconf += "openai:\r\n";
			sconf += "    enable: false\r\n";
			sconf += "    token: \r\n";
			sconf += "\r\n";
			sconf += "whatsapp:\r\n";
			sconf += "    enable: false\r\n";
			sconf += "    phone-number: \r\n";
			sconf += "    group: # Not implemented yet\r\n";
			sconf += "        enable: false\r\n";
			sconf += "        willy-names:\r\n";
			sconf += "            - Willy\r\n";
			sconf += "            - willy\r\n";
			sconf += "        ignore-start-with:\r\n";
			sconf += "            - \"!\"\r\n";
			sconf += "            - \"?\"\r\n";
			sconf += "            - \"@\"\r\n";
			sconf += "            - \"/\"\r\n";
			sconf += "            - \"#\"\r\n";
			sconf += "        auto-delete:\r\n";
			sconf += "            willy-messages: false\r\n";
			sconf += "            delete-after-wait: 10m\r\n";
			sconf += "\r\n";
			sconf += "telegram:\r\n";
			sconf += "    enable: false\r\n";
			sconf += "    token: \r\n";
			sconf += "    group: # Not implemented yet\r\n";
			sconf += "        enable: false\r\n";
			sconf += "        willy-names:\r\n";
			sconf += "            - Willy\r\n";
			sconf += "            - willy\r\n";
			sconf += "        ignore-start-with:\r\n";
			sconf += "            - \"!\"\r\n";
			sconf += "            - \"?\"\r\n";
			sconf += "            - \"@\"\r\n";
			sconf += "            - \"/\"\r\n";
			sconf += "            - \"#\"\r\n";
			sconf += "        auto-delete:\r\n";
			sconf += "            willy-messages: false\r\n";
			sconf += "            delete-after-wait: 10m\r\n";
			sconf += "\r\n";
			sconf += "bitly:\r\n";
			sconf += "    enable: false\r\n";
			sconf += "    token: \r\n";
			sconf += "\r\n";
			sconf += "http-api:\r\n";
			sconf += "    enable: true\r\n";
			sconf += "    port: 7001\r\n";

			try {
				FileWriter confWriter = new FileWriter(confFile);
				confWriter.write(sconf, 0, sconf.length());
				confWriter.close();
				Willy.getLogger().warning("Config file has been created on application directory.");
				Willy.getLogger().warning("Setup config file and restart this application to apply new configs.");
				Thread.sleep(1000);
				if(firstLoad) {
					firstLoad = false;
					loadConfigFile();
				}
			} catch (IOException | InterruptedException e) {
				Willy.getLogger().severe("Can't create the config file, please check write and read system permissions.");
				Willy.getLogger().severe(e.getMessage());
				Willy.getWilly().stop();
			}
		}
	}

	private static long parseTime(String time) {
		if(time == null)
			return 0;
		long value = 0; time = time.toLowerCase();
		if((time.contains("s") && Integer.parseInt(time.split("s")[0]) > 0)) {
			value = 1000L * Integer.parseInt(time.split("s")[0]);
		}else if((time.contains("m") && Integer.parseInt(time.split("m")[0]) > 0)) {
			value = 60000L * Integer.parseInt(time.split("m")[0]);
		}else if((time.contains("h") && Integer.parseInt(time.split("h")[0]) > 0)) {
			value = 3600000L * Integer.parseInt(time.split("h")[0]);
		}else if((time.contains("d") && Integer.parseInt(time.split("d")[0]) > 0)) {
			value = 86400000L * Integer.parseInt(time.split("d")[0]);
		}
		return value;
	}

	public static boolean has(String keyword) {
		return settings.containsKey(keyword);
	}

	public static Object get(String keyword) {
		return settings.get(keyword);
	}

	public static String getString(String keyword) {
		return (String)settings.get(keyword);
	}

	public static int getInt(String keyword) {
		return (int)settings.get(keyword);
	}

	public static long getLong(String keyword) {
		return Long.parseLong(""+settings.get(keyword));
	}

	public static float getFloat(String keyword) {
		return (float)settings.get(keyword);
	}

	public static boolean getBoolean(String keyword) {
		return (boolean)settings.get(keyword);
	}

	@NotNull
	@Contract("_ -> new")
	public static List<Object> getList(String keyword) {
		Object raw = settings.get(keyword);
		if(raw instanceof List list)
			return new ArrayList<Object>(list);
		return new ArrayList<>();
	}

	@NotNull
	@Contract("_ -> new")
	@SuppressWarnings("unchecked")
	public static List<String> getStringList(String keyword) {
		Object raw = settings.get(keyword);
		if(raw instanceof List list)
			return new ArrayList<String>(list);
		return new ArrayList<>();
	}

	public static boolean isString(String keyword) {
		return settings.get(keyword) instanceof String;
	}

	public static boolean isInt(String keyword) {
		return settings.get(keyword) instanceof Integer;
	}

	public static boolean isLong(String keyword) {
		return settings.get(keyword) instanceof Long;
	}

	public static boolean isFloat(String keyword) {
		return settings.get(keyword) instanceof Float;
	}

	public static boolean isBoolean(String keyword) {
		return settings.get(keyword) instanceof Boolean;
	}

	public static boolean isList(String keyword) {
		return settings.get(keyword) instanceof List;
	}
}
