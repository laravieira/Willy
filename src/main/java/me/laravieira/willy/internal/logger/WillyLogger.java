package me.laravieira.willy.internal.logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import me.laravieira.willy.Willy;


public class WillyLogger extends Logger {

	private Logger  consoleLogger  = null;
	private Handler fileHandler    = null;
	private Handler consoleHandler = null;
	
	public WillyLogger() {
		super(Willy.class.getCanonicalName(), Logger.getGlobal().getResourceBundleName());

		try {
			LogManager.getLogManager().reset();
			System.setProperty("java.util.logging.SimpleFormatter.format", "[%2$tT][%4$s] %5$s %n");
			Handler[] handlers = Logger.getLogger(Willy.class.getCanonicalName()).getParent().getHandlers();
			for(Handler handler : handlers)
				Logger.getLogger(Willy.class.getCanonicalName()).getParent().removeHandler(handler);

			// Create a format for loggers
			Formatter fileFormatter = new Formatter() {
				@Override
				public String format(LogRecord record) {
					return String.format("[%1$tT][%2$s] %3$s %n", record.getMillis(), record.getLevel(), record.getMessage());
				}
			};
			Formatter consoleFormatter = new Formatter() {
				@Override
				public String format(LogRecord record) {
					return String.format("[%1$tT][%2$s] %3$s %n", record.getMillis(), record.getLevel(), record.getMessage());
				}
			};
			fileHandler    = new LogFileHandler();
			consoleHandler = new ConsoleHandler();
			
			fileHandler.setLevel(Level.ALL);
			consoleHandler.setLevel(Level.INFO);
			fileHandler.setFormatter(fileFormatter);
			consoleHandler.setFormatter(consoleFormatter);

			consoleLogger = Logger.getLogger(Willy.class.getCanonicalName()+"-console");
			
			this.addHandler(fileHandler);
			this.addHandler(consoleHandler);
			consoleLogger.addHandler(consoleHandler);
			
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
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