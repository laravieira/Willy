package me.laravieira.willy.kernel;

import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import me.laravieira.willy.Willy;
import me.laravieira.willy.chat.discord.DiscordContext;
import me.laravieira.willy.feature.player.DiscordPlayer;
import me.laravieira.willy.internal.Config;

import java.util.List;

public class Kernel {

	private static MessageChannel verbuse = null;

	public static boolean checkForPlayQuestion(DiscordContext context, String content) {
		for(String key : (List<String>) Config.getList("ap.blends_for_play"))
			if(content.toLowerCase().startsWith(key.toLowerCase())) {
				DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(context);
				String data = content.substring(key.length()).trim();
				System.out.println(data);
				if(player != null && !data.isEmpty())
					player.search(data);
				else
					context.getSender().send("Desculpa, acho que eu não peguei seu pedido. 🐕");
				return true;
			}
		return false;
	}
	
	
	public static void onMemberUpdate(MemberUpdateEvent event) {
		String master = Config.getString("discord.keep_master_nick");
		boolean willy = Config.getBoolean("discord.keep_willy_nick");
		
		if(master != null && event.getMemberId().asString().equals(master) && event.getCurrentNickname().isPresent()) {
			try {
				event.getMember().block().edit(spec -> spec.setNickname(null)).doOnError(data -> {
					Willy.getLogger().warning("Master nickname reset falied caused by: "+data.getMessage());
				}).block();
			}catch(RuntimeException e) {
				if(e.getMessage().contains("Missing Permissions"))
					Willy.getLogger().warning("Master nickname reset falied caused by missing permission.");
				else
					Willy.getLogger().warning("Master nickname reset exception: "+e.getMessage());
			}
		}
		
		if(willy && event.getMemberId().asString().equals(Config.getString("discord.client_id")) && event.getCurrentNickname().isPresent()) {
			try {
				event.getMember().block().edit(spec -> spec.setNickname(null)).doOnError(data -> {
					Willy.getLogger().warning("Willy nickname reset falied caused by: "+data.getMessage());
				}).block();
			}catch(RuntimeException e) {
				if(e.getMessage().contains("Missing Permissions"))
					Willy.getLogger().warning("Willy nickname reset falied caused by missing permission.");
				else
					Willy.getLogger().warning("Willy nickname reset exception: "+e.getMessage());
			}
		}
	}
}
