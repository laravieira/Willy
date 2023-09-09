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

		List<String> aliases = new ArrayList<>();
		aliases.add(Willy.getWilly().getName().toLowerCase());

		List<String> ignore = new ArrayList<>();
		ignore.add("!");
		ignore.add("?");
		ignore.add("@");
		ignore.add("/");
		ignore.add("\\");
		ignore.add("//");
		ignore.add("#");

		List<String> apBlends = new ArrayList<>();
		apBlends.add(Willy.getWilly().getName() + " play");
		apBlends.add(Willy.getWilly().getName() + " toca");
		apBlends.add(Willy.getWilly().getName() + " adiciona");
		apBlends.add(Willy.getWilly().getName() + " reproduz");
		apBlends.add(Willy.getWilly().getName() + " reproduza");
		apBlends.add(Willy.getWilly().getName() + " toque");
		apBlends.add(Willy.getWilly().getName() + " coloca");

		// Overall Settings
		set("name",                  "WILLY_NAME",                     "willy-name",              TYPE_STRING, Willy.getWilly().getName());
		set("aliases",               "WILLY_ALIASES",                  "willy-aliases",           TYPE_LIST,    aliases);
		set("context_lifetime",      "WILLY_CONTEXT_LIFETIME",         "context-life-time",       TYPE_TIME,    parseTime("5m"));
		set("ignore_if_start_with",  "WILLY_IGNORE_IF_START_WITH",     "ignore-start-with",       TYPE_LIST,    ignore);

		// Discord settings
		set("discord.enable",             "WILLY_DISCORD_ENABLE",                   "discord.enable",                  TYPE_BOOLEAN, true);
		set("discord.client_id",          "WILLY_DISCORD_CLIENT_ID",                "discord.client-id",               TYPE_STRING,  null);
		set("discord.token",              "WILLY_DISCORD_TOKEN",                    "discord.token",                   TYPE_STRING,  null);
		set("discord.verbose",            "WILLY_DISCORD_VERBOSE",                  "discord.verbose-channel",         TYPE_STRING,  null);
		set("discord.keep_willy_nick",    "WILLY_DISCORD_KEEP_NICK_WILLY",          "discord.keep-willy-nick",         TYPE_BOOLEAN, true);
		set("discord.keep_master_nick",   "WILLY_DISCORD_KEEP_NICK_MASTER",         "discord.keep-master-nick",        TYPE_STRING,  null);
		set("discord.clear_public_chats", "WILLY_DISCORD_CLEAR_PUBLIC_CHATS",       "discord.clear-public-chats",      TYPE_BOOLEAN, true);
		set("discord.clear_after_wait",   "WILLY_DISCORD_CLEAR_PUBLIC_CHATS_AFTER", "discord.clear-after-wait",        TYPE_TIME,    parseTime("10m"));
		set("discord.admin.guild",        "WILLY_DISCORD_ADMIN_GUILD",              "discord.admin.guild",             TYPE_LONG, null);
		set("discord.admin.command",      "WILLY_DISCORD_ADMIN_COMMAND",            "discord.admin.command",           TYPE_LONG, null);
		set("discord.admin.log",          "WILLY_DISCORD_ADMIN_LOG",                "discord.admin.log",               TYPE_LONG, null);

		// ChatGPT settings
		set("chatgpt.enable", "WILLY_CHATGPT_ENABLE", "chatgpt.enable", TYPE_BOOLEAN, true);
		set("chatgpt.token", "WILLY_CHATGPT_TOKEN", "chatgpt.token", TYPE_STRING, null);

		// Whatsapp Settings
		set("whatsapp.enable",      "WILLY_WHATSAPP_ENABLE", "whatsapp.enable",      TYPE_BOOLEAN, true);
		set("whatsapp.shared_chat", "WILLY_WHATSAPP_SHARED", "whatsapp.shared-chat", TYPE_BOOLEAN, false);

		// Telegram Settings
		set("telegram.enable", "WILLY_TELEGRAM_ENABLE", "telegram.enable", TYPE_BOOLEAN, true);
		set("telegram.token",  "WILLY_TELEGRAM_TOKEN",  "telegram.token",  TYPE_STRING,  null);

		// Audio Player Settings
		set("ap.enable",                     "WILLY_AP_ENABLE",          "audio-player.enable",                     TYPE_BOOLEAN, true);
		set("ap.change_activity",            "WILLY_AP_CHANGE_ACTIVITY", "audio-player.change-activity",            TYPE_BOOLEAN, true);
		set("ap.command_default_channel_id", "WILLY_AP_DEFAULT_CHANNEL", "audio-player.command-default-channel-id", TYPE_STRING,  null);
		set("ap.blends_for_play",            "WILLY_AP_BLENDS",          "audio-player.blends-for-play",            TYPE_LIST,    apBlends);

		// Bitly Settings
		set("bitly.enable", "WILLY_BITLY_ENABLE", "bitly.enable", TYPE_BOOLEAN, true);
		set("bitly.token",  "WILLY_BITLY_TOKEN",  "bitly.token",  TYPE_STRING,  null);

		// YouTube Downloader Settings
		set("ytd.enable",    "WILLY_YTD_ENABLE",    "youtube-downloader.enable",    TYPE_BOOLEAN, true);
		set("ytd.willy_vpn", "WILLY_YTD_LOCAL",     "youtube-downloader.willy-vpn", TYPE_BOOLEAN, false);
		set("ytd.use_bitly", "WILLY_YTD_USE_BITLY", "youtube-downloader.use-bitly", TYPE_BOOLEAN, true);

		// Willy Shadow Settings
		set("shadow.enable", "WILLY_SHADOW_ENABLE", "shadow.enable", TYPE_BOOLEAN, false);
		set("shadow.token",  "WILLY_SHADOW_TOKEN",  "shadow.token",  TYPE_STRING,  null);
	}

	private static void set(String key, String envKey, String fileKey, int type, Object defaultValue) {
		if(configFile.has(fileKey)) {
			switch (type) {
				case TYPE_STRING  -> settings.put(key, configFile.asString(fileKey));
				case TYPE_INT     -> settings.put(key, configFile.asInt(fileKey));
				case TYPE_LONG    -> settings.put(key, configFile.asLong(fileKey));
				case TYPE_BOOLEAN -> settings.put(key, configFile.asBoolean(fileKey));
				case TYPE_TIME    -> settings.put(key, parseTime(configFile.asString(fileKey)));
				case TYPE_LIST    -> settings.put(key, configFile.asList(fileKey));
				default -> settings.put(key, configFile.asObject(fileKey));
			}
		}
		if(System.getenv().get(envKey) != null) {
			switch (type) {
				case TYPE_INT     -> settings.put(key, Integer.parseInt(System.getenv(envKey)));
				case TYPE_LONG    -> settings.put(key, Long.parseLong(System.getenv(envKey)));
				case TYPE_BOOLEAN -> settings.put(key, Boolean.parseBoolean(System.getenv(envKey)));
				case TYPE_TIME    -> settings.put(key, parseTime(System.getenv(envKey)));
				case TYPE_LIST    -> settings.put(key, Arrays.stream(System.getenv(envKey).split(";")).toList());
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
			// TODO Implement a better cofig file generator
			String sconf = "";
			sconf += "# ---------------------------------------------------------- #\r\n";
			sconf += "#                                                            #\r\n";
			sconf += "#                    Willy Bot Config File                   #\r\n";
			sconf += "#                                                            #\r\n";
			sconf += "#     For help check https://github.com/laravieira/Willy     #\r\n";
			sconf += "#                                                            #\r\n";
			sconf += "# ---------------------------------------------------------- #\r\n";
			sconf += "\r\n";
			sconf += "willy-name: Willy\r\n";

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
