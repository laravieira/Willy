package me.laravieira.willy.chat.discord;

import java.util.*;

import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel.Type;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.core.spec.MessageCreateSpec;
import me.laravieira.willy.Willy;
import me.laravieira.willy.feature.player.DiscordPlayer;
import me.laravieira.willy.internal.Config;
import discord4j.core.object.entity.channel.MessageChannel;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.utils.WillyUtils;
import org.jetbrains.annotations.NotNull;

public class DiscordListener {
	public static void onMessage(Message message) {
		try {
			if(message.getAuthor().isPresent()) {
				message.getContent();
				MessageChannel channel = message.getChannel().block();
				User author = message.getAuthor().get();
				if (author.isBot()) return;

				assert channel != null;
				if (channel.getType().equals(Type.DM) || channel.getType().equals(Type.GROUP_DM))
					onPrivateTextChannelMessage(channel, author, message);
				else
					onPublicTextChannelMessage(channel, author, message);
			}
		}catch(Exception e) {
			Willy.getLogger().severe(e.getMessage());
			for(StackTraceElement elem : e.getStackTrace())
				Willy.getLogger().severe(elem.getMethodName()+" ("+elem.getLineNumber()+")");
		}
	}
	
	public static void onPrivateTextChannelMessage(MessageChannel channel, @NotNull User user, @NotNull Message message) {
		String content = parseWillyDiscordId(message.getContent());
		UUID id = UUID.nameUUIDFromBytes(("discord-"+user.getId().asString()).getBytes());
		if(startWith(content)) return;

		DiscordMessage discordMessage = clearContent(channel, user, message, content, id, PassedInterval.DISABLE);

		ContextStorage.of(discordMessage.getContext()).getWatson().getSender().sendText(content);
	}

	@NotNull
	private static DiscordMessage clearContent(MessageChannel channel, User user, Message message, String content, UUID id, long expire) {
		content = content.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');

		DiscordSender sender = new DiscordSender(id, channel, expire);

		ContextStorage.of(id).setSender(sender);
		DiscordMessage discordMessage = new DiscordMessage(id, user, message, content, expire);
		MessageStorage.add(discordMessage);
		return discordMessage;
	}

	public static void onPublicTextChannelMessage(MessageChannel channel, @NotNull User user, @NotNull Message message) {
		long expire = Config.getBoolean("discord.clear_public_chats") ? Config.getLong("discord.clear_after_wait") : PassedInterval.DISABLE;
		String content = parseWillyDiscordId(message.getContent());
		UUID id = UUID.nameUUIDFromBytes(("discord-"+user.getId().asString()).getBytes());
		
		if(content.startsWith("!help")) {
			content = content.substring(5);
			floodChat(channel, user);
		}else if(startWith(content)) return;
		if(content.isEmpty()) return;
		
		if(!WillyUtils.hasWillyCall(content) && !ContextStorage.has(id)) return;

		DiscordMessage discordMessage= clearContent(channel, user, message, content, id, expire);

		if(!checkForPlayQuestion(id, content))
			ContextStorage.of(discordMessage.getContext()).getWatson().getSender().sendText(content);
		Willy.getLogger().info("Message transaction in a public chat.");
	}
	
	@NotNull
	private static String parseWillyDiscordId(@NotNull String message) {
		long id = Config.getLong("discord.client_id");
		String name = Config.getString("willy-name");
		return message.replaceAll("<@"+ id +">", name);
	}
	
	private static boolean startWith(String message) {
		for(String prefix : Config.getStringList("ignore_if_start_with"))
			if(message.startsWith(prefix))
				return true;
		return false;
	}

	public static boolean checkForPlayQuestion(UUID context, String content) {
		//TODO Fix context on music player
		for(String key : Config.getStringList("ap.blends_for_play"))
			if(content.toLowerCase().startsWith(key.toLowerCase())) {
				DiscordPlayer player = DiscordPlayer.getDiscordPlayerFromContext(null);
				String data = content.substring(key.length()).trim();
				System.out.println(data);
				if(player != null && !data.isEmpty())
					player.search(data);
				else
					ContextStorage.of(context).getSender().sendText("Desculpa, acho que eu não peguei seu pedido. 🐕");
				return true;
			}
		return false;
	}

	public static void onMemberUpdate(MemberUpdateEvent event) {
		String master = Config.getString("discord.keep_master_nick");
		boolean willy = Config.getBoolean("discord.keep_willy_nick");

		try {
			if(master != null && event.getMemberId().asString().equals(master) && event.getCurrentNickname().isPresent()) {
				Member member = event.getMember().block();
				if(member != null) {
					member.edit(GuildMemberEditSpec.builder()
							.nicknameOrNull(null)
							.build())
						.doOnError(data -> Willy.getLogger().warning("Master nickname reset failed caused by: "+data.getMessage()))
						.block();
				}
			}

			if(willy && event.getMemberId().asString().equals(Config.getString("discord.client_id")) && event.getCurrentNickname().isPresent()) {
				Member member = event.getMember().block();
				if(member != null) {
					member.edit(GuildMemberEditSpec.builder()
									.nicknameOrNull(null)
									.build())
							.doOnError(data -> Willy.getLogger().warning("Willy nickname reset failed caused by: "+data.getMessage()))
							.block();
				}
			}
		}catch(RuntimeException e) {
			if(e.getMessage().contains("Missing Permissions"))
				Willy.getLogger().warning("Nickname reset failed caused by missing permission.");
			else
				Willy.getLogger().warning("Nickname reset exception: "+e.getMessage());
		}
	}

	private static void floodChat(MessageChannel channel, User user) {
		try {
			UUID id = UUID.nameUUIDFromBytes(("discord-"+user.getId().asString()).getBytes());
			DiscordSender sender = new DiscordSender(id, channel, Config.getLong("discord.clear_after_wait"));

			sender.sendEmbed(MessageCreateSpec.builder()
					.addEmbed(EmbedCreateSpec.builder()
							.image("https://github.com/laravieira/Willy/raw/master/assets/help/helps.png")
							.build())
					.build()
			);
			sender.sendEmbed(MessageCreateSpec.builder()
					.addEmbed(EmbedCreateSpec.builder()
							.image("https://github.com/laravieira/Willy/raw/master/assets/help/help.png")
							.build())
					.build()
			);
			sender.sendEmbed(MessageCreateSpec.builder()
					.addEmbed(EmbedCreateSpec.builder()
							.image("https://github.com/laravieira/Willy/raw/master/assets/help/be-help.png")
							.build())
					.build()
			);
			sender.sendEmbed(MessageCreateSpec.builder()
					.addEmbed(EmbedCreateSpec.builder()
							.thumbnail("https://github.com/laravieira/Willy/raw/master/assets/help/piscadela.jpg")
							.build())
					.build()
			);
			Willy.getLogger().info("Chat flood sent to "+channel.getId().asString()+".");
			
		}catch(RuntimeException e) {
			Willy.getLogger().warning("Flood failure by an exception: "+e.getMessage());
		}
	}
}
