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