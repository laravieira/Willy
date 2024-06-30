package me.laravieira.willy.command;

import java.util.ArrayList;
import java.util.List;

import me.laravieira.willy.command.commands.*;

public class Command {
	public static List<CommandListener> commandsList() {
		List<CommandListener> commands = new ArrayList<>();

		commands.add(new CommandBitly());
		commands.add(new CommandContext());
		commands.add(new CommandDiscord());
		commands.add(new CommandStatus());
		commands.add(new CommandWhatsapp());

		return commands;
	}
}
