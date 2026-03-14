package me.laravieira.willy.chat.discord;

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
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.core.spec.MessageCreateSpec;
import me.laravieira.willy.Context;
import me.laravieira.willy.Willy;
import me.laravieira.willy.WillyMessage;
import me.laravieira.willy.command.Command;
import me.laravieira.willy.command.CommandListener;
import me.laravieira.willy.internal.Config;
import discord4j.core.object.entity.channel.MessageChannel;
import me.laravieira.willy.utils.PassedInterval;
import me.laravieira.willy.utils.WillyUtils;
import org.jetbrains.annotations.NotNull;

public class DiscordListener {

	public static InteractionApplicationCommandCallbackReplyMono onCommand(ChatInputInteractionEvent event) {
		try {
			for (CommandListener command : Command.adminCommandsList()) {
				if (command.getName().equals(event.getCommandName()))
					return command.execute(event);
			}
			for (CommandListener command : Command.globalCommandsList()) {
				if (command.getName().equals(event.getCommandName()))
					return command.execute(event);
			}
			return null;
		}catch(Exception e) {
			return event.reply("Interaction went wrong: "+e.getMessage());
		}
	}

	public static void onMessage(Message message) {
		Thread thread = new Thread(() -> {
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
					Willy.getLogger().severe(elem.getMethodName()+" "+elem.getLineNumber());
			}
		});
		thread.setName("Discord Message Listener");
		thread.setDaemon(true);
		thread.start();
	}

	public static void onPrivateTextChannelMessage(MessageChannel channel, User user, Message message) {
		String content = parseWillyDiscordId(message.getContent());
		UUID id = UUID.nameUUIDFromBytes(("discord-"+user.getId().asString()).getBytes());

		if(content.isEmpty())
			return;
		if(WillyUtils.startsWith(content, Config.getStringList("discord.public_chat.ignore_start_with")))
			return;

		Willy.getLogger().fine("Msg on Discord dm "+channel.getId().asLong()+" by "+user.getId().asLong());
        Context context = Context.of(id, new DiscordChannel(channel, PassedInterval.DISABLE), "Discord", user.getUsername());
		DiscordMessage discordMessage = buildMessage(user, message, content, PassedInterval.DISABLE);
        context.process(discordMessage);
	}

	@NotNull
	private static DiscordMessage buildMessage(User user, Message message, String content, long expire) {
		DiscordMessage discordMessage = new DiscordMessage(user, message, content, expire);
		for(Attachment attachment : message.getAttachments()) {
			try {
				if(attachment.getContentType().isPresent() && attachment.getContentType().get().toLowerCase().startsWith("image"))
					discordMessage.addUrl(attachment.getUrl());
			}catch(Exception e) {
				Willy.getLogger().warning("Error on attachment proccessing: "+e.getMessage());
			}
		}
		return discordMessage;
	}

	public static void onPublicTextChannelMessage(MessageChannel channel, User user, Message message) {
		if(!Config.getBoolean("discord.public_chat.enable"))
			return;
		String content = parseWillyDiscordId(message.getContent());
		UUID id = UUID.nameUUIDFromBytes(("discord-"+channel.getId()).getBytes());

		if(content.isEmpty())
			return;
		if(WillyUtils.startsWith(content, Config.getStringList("discord.public_chat.ignore_start_with")))
			return;
		if(!WillyUtils.hasWillyName(content, Config.getStringList("discord.public_chat.willy_names")) && !Context.has(id))
			return;

		long expire = Config.getBoolean("discord.public_chat.auto_delete.willy_messages")
				? Config.getLong("discord.public_chat.auto_delete.delete_after_wait")
				: PassedInterval.DISABLE;

		Willy.getLogger().fine("Msg on Discord public "+channel.getId().asLong()+" by "+user.getId().asLong());
        Context context = Context.of(id, new DiscordChannel(channel, expire), "discord", user.getUsername());
        context.setUser(user.getUsername());
        DiscordMessage discordMessage = buildMessage(user, message, content, expire);
        context.process(discordMessage);
	}
	
	@NotNull
	private static String parseWillyDiscordId(@NotNull String message) {
		long id = Config.getLong("discord.client_id");
		String name = Config.getStringList("discord.public_chat.willy_names").getFirst();
		return message.replaceAll("<@"+id+">", name);
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
		//TODO Make this a command
		try {
			UUID id = UUID.nameUUIDFromBytes(("discord-"+channel.getId()).getBytes());
			DiscordChannel sender = new DiscordChannel(channel, Config.getLong("discord.public_chat.auto_delete.delete_after_wait"));
			Context context = Context.of(id, sender, "discord", user.getUsername());

			WillyMessage message = new WillyMessage(
                MessageCreateSpec.builder()
                    .addEmbed(EmbedCreateSpec.builder()
                        .image("https://raw.laravieira.me/willy/help/helps.png")
                        .build())
                    .build()
            );
			message.setExpire(Config.getLong("discord.public_chat.auto_delete.delete_after_wait"));
            context.respond(message);

			message = new WillyMessage(
                MessageCreateSpec.builder()
                    .addEmbed(EmbedCreateSpec.builder()
                        .image("https://raw.laravieira.me/willy/help/help.png")
                        .build())
                    .build()
            );
			message.setExpire(Config.getLong("discord.public_chat.auto_delete.delete_after_wait"));
            context.respond(message);

			message = new WillyMessage(
                MessageCreateSpec.builder()
                    .addEmbed(EmbedCreateSpec.builder()
                        .image("https://raw.laravieira.me/willy/help/be-help.png")
                        .build())
                    .build()
            );
			message.setExpire(Config.getLong("discord.public_chat.auto_delete.delete_after_wait"));
            context.respond(message);

			message = new WillyMessage(
                MessageCreateSpec.builder()
                    .addEmbed(EmbedCreateSpec.builder()
                        .thumbnail("https://raw.laravieira.me/willy/help/piscadela.jpg")
                        .build())
                    .build()
            );
			message.setExpire(Config.getLong("discord.public_chat.auto_delete.delete_after_wait"));
            context.respond(message);

			Willy.getLogger().info("Chat flood sent to "+channel.getId().asString()+".");
		}catch(RuntimeException e) {
			e.printStackTrace();
		}
	}
}
