package me.laravieira.willy.internal.logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

class LogFileHandler extends Handler {
    private static String LOGS_FOLDER = null;

    static {
        try {
            LOGS_FOLDER = (new File(".").getCanonicalPath())+File.separator+"logs"+File.separator;
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String fileName = null;
    private FileHandler fileHandler = null;

    private void createHandler() {
        try {
            if(!(new File(LOGS_FOLDER).isDirectory() || new File(LOGS_FOLDER).mkdirs()))
                return;

            fileName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            File logFile = new File(LOGS_FOLDER+fileName+".log");
            if((logFile.exists() && logFile.isFile()) || logFile.createNewFile())
                return;
            fileHandler = new FileHandler(LOGS_FOLDER+fileName+".log", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LogFileHandler() throws IOException, SecurityException {
        createHandler();
    }

    @Override
    public void publish(LogRecord record) {
        if(!fileName.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))) {
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