package net.jwdouglas.willy;

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


public class MyLogger {
	
	private static Logger  fileLogger     = null;
	private static Logger  consoleLogger  = null;
	private static Handler fileHandler    = null;
	private static Handler consoleHandler = null;
	
	public static void load() {
		try {
			// Reset Loggers
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
			
			fileLogger    = Logger.getGlobal();
			consoleLogger = Logger.getLogger(Willy.class.getCanonicalName());
			
			fileLogger.addHandler(fileHandler);
			fileLogger.addHandler(consoleHandler);
			consoleLogger.addHandler(consoleHandler);
			
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Logger getLogger() {
		return fileLogger;
	}
	
	public static Logger getConsoleLogger() {
		return consoleLogger;
	}
	
	public static void close() {
		fileHandler.close();
	}
	
}

class LogFileHandler extends Handler {

	private String      filepath    = (new File(".").getCanonicalPath())+File.separator+"logs"+File.separator;
	private String      fileName    = null;
	private FileHandler fileHandler = null;
	
	private void createHandler() {
		if(!(new File(filepath).isDirectory()))
			new File(filepath).mkdirs();
		fileName = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
		File logFile = new File(filepath+fileName+".log");
		if(!logFile.exists() || !logFile.isFile()) {
			try {
				logFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
		}
		try {
			fileHandler = new FileHandler(filepath+fileName+".log", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LogFileHandler() throws IOException, SecurityException {
		createHandler();
	}

	@Override
	public void publish(LogRecord record) {
		if(!fileName.equals(new SimpleDateFormat("YYYY-MM-dd").format(new Date()))) {
			fileHandler.flush();
			fileHandler.close();
			fileHandler = null;
			createHandler();
		}
		fileHandler.publish(record);
	}

	@Override
	public void flush() {
		fileHandler.flush();
	}

	@Override
	public void close() throws SecurityException {
		fileHandler.close();
		fileHandler = null;
	}
	
	@Override
	public void setFormatter(Formatter formatter) {
		fileHandler.setFormatter(formatter);
	}
	
}