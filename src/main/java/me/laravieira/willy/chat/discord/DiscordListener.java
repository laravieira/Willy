package me.laravieira.willy.chat.discord;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel.Type;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.core.spec.MessageCreateSpec;
import me.laravieira.willy.Willy;
import me.laravieira.willy.command.Command;
import me.laravieira.willy.internal.Config;
import discord4j.core.object.entity.channel.MessageChannel;
import me.laravieira.willy.storage.ContextStorage;
import me.laravieira.willy.storage.MessageStorage;
import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.utils.WillyUtils;
import org.jetbrains.annotations.NotNull;

public class DiscordListener {

	public static void onCommand(ChatInputInteractionEvent event) {
		try {
			Command.adminCommandsList().forEach(command -> {
				if (command.getName().equals(event.getCommandName()))
					command.execute(event);
			});
			Command.globalCommandsList().forEach(command -> {
				if (command.getName().equals(event.getCommandName()))
					command.execute(event);
			});
		}catch(Exception e) {
			event.reply(STR."Something went wrong: \{e.getMessage()}").subscribe();
		}
	}

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
				Willy.getLogger().severe(STR."\{elem.getMethodName()} (\{elem.getLineNumber()})");
		}
	}
	
	public static void onPrivateTextChannelMessage(MessageChannel channel, @NotNull User user, @NotNull Message message) {
		String content = parseWillyDiscordId(message.getContent());
		UUID id = UUID.nameUUIDFromBytes((STR."discord-\{user.getId().asString()}").getBytes());

		if(content.isEmpty())
			return;
		if(WillyUtils.startsWith(content, Config.getStringList("discord.public_chat.ignore_start_with")))
			return;

		Willy.getLogger().fine(STR."Msg on Discord dm \{channel.getId().asLong()} by \{user.getId().asLong()}");
		DiscordMessage discordMessage = buildMessage(channel, user, message, content, id, PassedInterval.DISABLE);
		ContextStorage.of(discordMessage.getContext()).getSender().send(discordMessage);
	}

	@NotNull
	private static DiscordMessage buildMessage(MessageChannel channel, User user, Message message, String content, UUID id, long expire) {
		ContextStorage.of(id).setUserSender(new DiscordSender(id, channel, expire));
		ContextStorage.of(id).setApp("discord");

		DiscordMessage discordMessage = new DiscordMessage(id, user, message, content, expire);
		for(Attachment attachment : message.getAttachments()) {
			try {
				if(attachment.getContentType().isPresent() && attachment.getContentType().get().toLowerCase().startsWith("image"))
					discordMessage.addUrl(attachment.getUrl());
			}catch(Exception e) {
				Willy.getLogger().warning(STR."Error on attachment proccessing: \{e.getMessage()}");
			}
		}
		MessageStorage.add(discordMessage);
		return discordMessage;
	}

	public static void onPublicTextChannelMessage(MessageChannel channel, @NotNull User user, @NotNull Message message) {
		if(!Config.getBoolean("discord.public_chat.enable"))
			return;
		String content = parseWillyDiscordId(message.getContent());
		UUID id = UUID.nameUUIDFromBytes((STR."discord-\{channel.getId()}").getBytes());

		if(content.isEmpty())
			return;
		if(WillyUtils.startsWith(content, Config.getStringList("discord.public_chat.ignore_start_with")))
			return;
		if(!WillyUtils.hasWillyName(content, Config.getStringList("discord.public_chat.willy_names")) && !ContextStorage.has(id))
			return;

		long expire = Config.getBoolean("discord.public_chat.auto_delete.willy_messages")
				? Config.getLong("discord.public_chat.auto_delete.delete_after_wait")
				: PassedInterval.DISABLE;

		Willy.getLogger().fine(STR."Msg on Discord public \{channel.getId().asLong()} by \{user.getId().asLong()}");
		DiscordMessage discordMessage = buildMessage(channel, user, message, content, id, expire);
		ContextStorage.of(discordMessage.getContext()).getSender().send(discordMessage);
	}
	
	@NotNull
	private static String parseWillyDiscordId(@NotNull String message) {
		long id = Config.getLong("discord.client_id");
		String name = Config.getStringList("discord.public_chat.willy_names").getFirst();
		return message.replaceAll(STR."<@\{id}>", name);
	}

	public static void onMemberUpdate(MemberUpdateEvent event) {
		boolean willy = Config.getBoolean("discord.keep_willy_nick");
		String master = Config.getString("discord.keep_master_nick");

		try {
			if(master != null && event.getMemberId().asString().equals(master) && event.getCurrentNickname().isPresent()) {
				Member member = event.getMember().block();
				if(member != null) {
					member.edit(GuildMemberEditSpec.builder()
							.nicknameOrNull(null)
							.build())
						.doOnError(data -> Willy.getLogger().warning(STR."Master nickname reset failed caused by: \{data.getMessage()}"))
						.block();
				}
			}

			if(willy && event.getMemberId().asString().equals(Config.getString("discord.client_id")) && event.getCurrentNickname().isPresent()) {
				Member member = event.getMember().block();
				if(member != null) {
					member.edit(GuildMemberEditSpec.builder()
									.nicknameOrNull(null)
									.build())
							.doOnError(data -> Willy.getLogger().warning(STR."Willy nickname reset failed caused by: \{data.getMessage()}"))
							.block();
				}
			}
		}catch(RuntimeException e) {
			if(e.getMessage().contains("Missing Permissions"))
				Willy.getLogger().warning("Nickname reset failed caused by missing permission.");
			else
				Willy.getLogger().warning(STR."Nickname reset exception: \{e.getMessage()}");
		}
	}

	private static void floodChat(MessageChannel channel, User user) {
		//TODO Make this a command
		try {
			UUID id = UUID.nameUUIDFromBytes((STR."discord-\{channel.getId()}").getBytes());
			DiscordSender sender = new DiscordSender(id, channel, Config.getLong("discord.public_chat.auto_delete.delete_after_wait"));
			ContextStorage.of(id).setUserSender(sender);
			ContextStorage.of(id).setApp("discord");

			me.laravieira.willy.context.Message message = new me.laravieira.willy.context.Message(id);
			message.setExpire(Config.getLong("discord.public_chat.auto_delete.delete_after_wait"));
			message.setContent(MessageCreateSpec.builder()
					.addEmbed(EmbedCreateSpec.builder()
							.image("https://raw.laravieira.me/willy/help/helps.png")
							.build())
					.build());
			message.setFrom(Willy.getWilly().getName());
			message.setTo(user.getUsername());
			MessageStorage.add(message);
			sender.sendEmbed((MessageCreateSpec)message.getContent());

			message = new me.laravieira.willy.context.Message(id);
			message.setExpire(Config.getLong("discord.public_chat.auto_delete.delete_after_wait"));
			message.setContent(MessageCreateSpec.builder()
					.addEmbed(EmbedCreateSpec.builder()
							.image("https://raw.laravieira.me/willy/help/help.png")
							.build())
					.build());
			message.setFrom(Willy.getWilly().getName());
			message.setTo(user.getUsername());
			MessageStorage.add(message);
			sender.sendEmbed((MessageCreateSpec)message.getContent());

			message = new me.laravieira.willy.context.Message(id);
			message.setExpire(Config.getLong("discord.public_chat.auto_delete.delete_after_wait"));
			message.setContent(MessageCreateSpec.builder()
					.addEmbed(EmbedCreateSpec.builder()
							.image("https://raw.laravieira.me/willy/help/be-help.png")
							.build())
					.build());
			message.setFrom(Willy.getWilly().getName());
			message.setTo(user.getUsername());
			MessageStorage.add(message);
			sender.sendEmbed((MessageCreateSpec)message.getContent());

			message = new me.laravieira.willy.context.Message(id);
			message.setExpire(Config.getLong("discord.public_chat.auto_delete.delete_after_wait"));
			message.setContent(MessageCreateSpec.builder()
					.addEmbed(EmbedCreateSpec.builder()
							.thumbnail("https://raw.laravieira.me/willy/help/piscadela.jpg")
							.build())
					.build());
			message.setFrom(Willy.getWilly().getName());
			message.setTo(user.getUsername());
			MessageStorage.add(message);
			sender.sendEmbed((MessageCreateSpec)message.getContent());

			Willy.getLogger().info(STR."Chat flood sent to \{channel.getId().asString()}.");
		}catch(RuntimeException e) {
			e.printStackTrace();
		}
	}
}
