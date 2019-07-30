package net.jwdouglas.willy;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MyLogger {
	private static final Logger logger = Logger.getLogger("Willy");
	private static boolean      logset = false;
	
	public static Logger logger(String level) {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$s] %5$s %n");
		
		switch(level.toLowerCase()) {
			case "debug": logger.setLevel(Level.ALL); break;
			case "normal": logger.setLevel(Level.INFO); break;
			default: logger.setLevel(Level.INFO); break;
		}
		
		try {
			logger.addHandler(new LogFileHandler());
			logger.addHandler(new LogHandler());
			LogManager.getLogManager().addLogger(logger);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		logset = true;
		return logger;
	}
	
	public static Logger getLogger() {
		if(!logset)
			return logger("normal");
		else
			return logger;
	}
	
	public void setLevel(String level) {
		switch(level.toLowerCase()) {
			case "debug": logger.setLevel(Level.ALL); break;
			case "normal": logger.setLevel(Level.INFO); break;
			default: logger.setLevel(Level.INFO); break;
		}
	}
	
	public void close() {
		for(Handler handler:logger.getHandlers()) {
			logger.removeHandler(handler);
			handler.close();
		}
	}
	
}

class LogFileHandler extends Handler {

	private String      filepath    = (new File(".").getCanonicalPath())+File.separator+"logs"+File.separator;
	private String      fileName    = null;
	private FileHandler fileHandler = null;
	private Formatter   fileForm    = new Formatter() {
		@Override
		public String format(LogRecord record) {
			return String.format("[%1$tT][%2$s] %3$s %n", record.getMillis(), record.getLevel(), record.getMessage());
		}
	};
	
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
			fileHandler.setFormatter(fileForm);
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
	
}

class LogHandler extends Handler {

	@Override
	public void close() throws SecurityException {}

	@Override
	public void flush() {}

	@Override
	public void publish(LogRecord record) {
		//if(record.getLoggerName() != "Willy") {
		//	record.setLevel(Level.OFF);
		//}else {
		//	System.out.println("["+record.getLevel()+"]"+record.getMessage());
		//}
	}
	
}