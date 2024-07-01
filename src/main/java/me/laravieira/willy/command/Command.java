package me.laravieira.willy.command;

import java.util.ArrayList;
import java.util.List;

import me.laravieira.willy.command.admin.*;
import me.laravieira.willy.command.commands.CommandBitly;

public class Command {
	public static List<CommandListener> adminCommandsList() {
		List<CommandListener> commands = new ArrayList<>();

		commands.add(new CommandContext());
		commands.add(new CommandDiscord());
		commands.add(new CommandStatus());
		commands.add(new CommandWhatsapp());

		return commands;
	}

	public static List<CommandListener> globalCommandsList() {
		List<CommandListener> commands = new ArrayList<>();

		commands.add(new CommandBitly());

		return commands;
	}
}
