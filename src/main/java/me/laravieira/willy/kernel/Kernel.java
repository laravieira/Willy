package me.laravieira.willy.kernel;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import me.laravieira.willy.config.Config;
import me.laravieira.willy.config.MyLogger;
import me.laravieira.willy.discord.Discord;
import me.laravieira.willy.player.DiscordPlayer;

public class Kernel {

	private static MessageChannel verbuse = null;

	public static void channelVerbuseUpdate(String message) {
		Thread sendVerbuse = new Thread(() -> {
			if(verbuse == null) {
				Snowflake channelId = Snowflake.of(Config.getDiscordVerbuse());
				verbuse = (MessageChannel)Discord.getBotGateway().getChannelById(channelId).block();
			}
			verbuse.createMessage(message).block();
		});
		sendVerbuse.setDaemon(true);
		sendVerbuse.start();
	}

	public static boolean checkForPlayQuestion(Context context, String content) {
		for(String key : Config.getPlayerPlayAskKeys())
			if(content.toLowerCase().startsWith(key.toLowerCase())) {
				DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
				String data = content.substring(key.length()).trim();
				System.out.println(data);
				if(player != null && !data.isEmpty())
					player.search(data);
				else
					new Messager(context).sendDiscordTextMessage("Desculpa, acho que eu não peguei seu pedido. 🐕");
				return true;
			}
		return false;
	}
	
	
	public static void onMemberUpdate(MemberUpdateEvent event) {
		String master = Config.getDiscordKeepNickMaster();
		boolean willy = Config.getDiscordKeepNickWilly();
		
		if(master != null && event.getMemberId().asString().equals(master) && event.getCurrentNickname().isPresent()) {
			try {
				event.getMember().block().edit(spec -> spec.setNickname(null)).doOnSuccess(data -> {
					MyLogger.getDiscordLogger().info("Master nickname reseted.");
				}).doOnError(data -> {
					MyLogger.getLogger().warning("Master nickname reset falied caused by: "+data.getMessage());
				}).block();
			}catch(RuntimeException e) {
				if(e.getMessage().contains("Missing Permissions"))
					MyLogger.getLogger().warning("Master nickname reset falied caused by missing permission.");
				else
					MyLogger.getLogger().warning("Master nickname reset exception: "+e.getMessage());
			}
		}
		
		if(willy && event.getMemberId().asString().equals(Config.getDiscordID()) && event.getCurrentNickname().isPresent()) {
			try {
				event.getMember().block().edit(spec -> spec.setNickname(null)).doOnSuccess(data -> {
					MyLogger.getDiscordLogger().info("Willy nickname reseted.");
				}).doOnError(data -> {
					MyLogger.getLogger().warning("Willy nickname reset falied caused by: "+data.getMessage());
				}).block();
			}catch(RuntimeException e) {
				if(e.getMessage().contains("Missing Permissions"))
					MyLogger.getLogger().warning("Willy nickname reset falied caused by missing permission.");
				else
					MyLogger.getLogger().warning("Willy nickname reset exception: "+e.getMessage());
			}
		}
	}

//	private static InputStream getFileInputStream(String path) {
//		try {
//			String filepath = (new File(".").getCanonicalPath())+File.separator+"help"+File.separator;
//			File file = new File(filepath+path);
//			if(file.isFile()) {
//				return new FileInputStream(file);
//			}else {
//				MyLogger.getLogger().info("Flood failed because files not found.");
//				return null;
//			}
//		} catch (IOException e) {
//			MyLogger.getLogger().info("Flood failed because an exception on files: "+e.getMessage());
//			return null;
//		}
//	}
}
