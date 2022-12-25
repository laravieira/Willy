package me.laravieira.willy.internal.logger;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import me.laravieira.willy.Willy;
import org.jetbrains.annotations.NotNull;


public class WillyLogger extends Logger {
	private static final Formatter LOG_FORMATTER = new Formatter() {
		@Override
		public String format(@NotNull LogRecord record) {
			return String.format("[%1$tT][%2$s] %3$s %n", record.getMillis(), record.getLevel(), record.getMessage());
		}
	};
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%2$tT][%4$s] %5$s %n");
	}

	private Logger consoleLogger = null;

	public WillyLogger() {
		super(Willy.class.getCanonicalName(), Logger.getGlobal().getResourceBundleName());

		try {
			LogManager.getLogManager().reset();

			Handler[] handlers = Logger.getLogger(Willy.class.getCanonicalName()).getParent().getHandlers();
			for(Handler handler : handlers)
				Logger.getLogger(Willy.class.getCanonicalName()).getParent().removeHandler(handler);

			Handler fileHandler = new LogFileHandler();
			Handler consoleHandler = new ConsoleHandler();

			fileHandler.setLevel(Level.ALL);
			consoleHandler.setLevel(Level.INFO);

			fileHandler.setFormatter(LOG_FORMATTER);
			consoleHandler.setFormatter(LOG_FORMATTER);

			consoleLogger = Logger.getLogger(Willy.class.getCanonicalName()+"-console");

			this.addHandler(fileHandler);
			this.addHandler(consoleHandler);
			consoleLogger.addHandler(consoleHandler);

		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	public void registerDiscordHandler() {
		Handler discordHandler = new DiscordHandler();
		discordHandler.setLevel(Level.INFO);
		discordHandler.setFormatter(LOG_FORMATTER);
		this.addHandler(discordHandler);
	}

	public Logger getConsole() {
		return consoleLogger;
	}

	public void close() {
		for(Handler handler : consoleLogger.getHandlers()) {
			consoleLogger.removeHandler(handler);
			handler.close();
		}
		for(Handler handler : this.getHandlers()) {
			this.removeHandler(handler);
			handler.close();
		}
	}
	
}