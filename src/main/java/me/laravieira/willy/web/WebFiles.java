package me.laravieira.willy.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import me.laravieira.willy.config.MyLogger;

public class WebFiles {

	private Logger log  = MyLogger.getLogger();
	private File   file = null;
	private String root = null;
	
	public WebFiles() {
		try {
			root = (new File(".").getCanonicalPath())+File.separator+"web"+File.separator;
		} catch (IOException e) {
			log.warning(e.getMessage());
		}
	}
	
	public WebFiles(String path) {
		try {
			root = (new File(".").getCanonicalPath())+File.separator+"web"+File.separator;
			if(path.startsWith("/")) path = path.substring(1);
			char[] c_path = path.toCharArray();
			path = "";
			for(int i = 0; i < c_path.length; i++)
				if(c_path[i] != '?')
					path += c_path[i];
				else break;
			file = new File(root+path);
			if(file.isDirectory())
				file = new File(root+path+"index.html");
			if(!file.isFile())
				file = new File(root+path+"index.htm");
		} catch (IOException e) {
			log.warning(e.getMessage());
		}
	}

	public WebFiles(int code) {
		try {
			String path = ""; switch(code) {
			case 401: path = "error/401.htm"; break; // Access denied (unlogged)
			case 403: path = "error/403.htm"; break; // Access forbidden
			case 404: path = "error/404.htm"; break; // File not found
			case 500: path = "error/500.htm"; break; // Internal error
			case 505: path = "error/505.htm"; break; // HTTP Version not supported
			}
			
			root = (new File(".").getCanonicalPath())+File.separator+"web"+File.separator;
			if(path.startsWith("/")) path = path.substring(1);
			char[] c_path = path.toCharArray();
			path = "";
			for(int i = 0; i < c_path.length; i++)
				if(c_path[i] != '?')
					path += c_path[i];
				else break;
			file = new File(root+path);
		} catch (IOException e) {
			log.warning(e.getMessage());
		}
	}
	
	public FileInputStream getFile() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.warning(e.getMessage());
			return null;
		}
	}
	
	public String contentType() {
		// HTML files type definition
		if(file.getName().endsWith(".html"))
			return "text/html; charset=utf-8";
		else if(file.getName().endsWith(".htm"))
			return "text/html; charset=utf-8";
		else if(file.getName().endsWith(".xhtml"))
			return "application/xhtml+xml; charset=utf-8";
		else if(file.getName().endsWith(".php"))
			return "text/html; charset=utf-8";
		else if(file.getName().endsWith(".js"))
			return "application/javascript";
		else if(file.getName().endsWith(".css"))
			return "text/css";
		
		// Images files type definition
		else if(file.getName().endsWith(".gif"))
			return "image/gif";
		else if(file.getName().endsWith(".jpg"))
			return "image/jpeg";
		else if(file.getName().endsWith(".jpeg"))
			return "image/jpeg";
		else if(file.getName().endsWith(".png"))
			return "image/png";
		else if(file.getName().endsWith(".bmp"))
			return "image/bmp";
		else if(file.getName().endsWith(".webp"))
			return "image/webp";
		else if(file.getName().endsWith(".svg"))
			return "image/svg+xml";
		else if(file.getName().endsWith(".ico"))
			return "image/x-icon";
		
		// Audio files type definition
		else if(file.getName().endsWith(".mp3"))
			return "audio/mpeg";
		else if(file.getName().endsWith(".wave"))
			return "audio/wave";
		else if(file.getName().endsWith(".wav"))
			return "audio/wave";
		else if(file.getName().endsWith(".mid"))
			return "audio/midi";
		
		// Video files type definition
		else if(file.getName().endsWith(".mp4"))
			return "video/mpeg";
		else if(file.getName().endsWith(".webm"))
			return "video/webm";
		else if(file.getName().endsWith(".flv"))
			return "video/flv";
		
		// Documents files type definition
		else if(file.getName().endsWith(".pdf"))
			return "application/pdf";
		else if(file.getName().endsWith(".xml"))
			return "application/xml";
		else if(file.getName().endsWith(".json"))
			return "application/json";
		else if(file.getName().endsWith(".pps"))
			return "application/vnd.mspowerpoint";
		else if(file.getName().endsWith(".xpps"))
			return "application/vnd.mspowerpoint";
		else if(file.getName().contains("."))
			return "text/plain";
		else if(file.getName().endsWith(".rar"))
			return "application/x-rar-compressed";
		else 
			return "application/octet-stream";
	}

	public boolean exist() {
		if(file.exists())
			return true;
		return false;
	}

	public long length() {
		return file.length();
	}
	
	public boolean load() {
		if(!(new File(root).isDirectory())) {
			List<String> directories = new ArrayList<String>();
			List<String> webFiles    = new ArrayList<String>();
	
			// Directories
			directories.add("error/");
			
			// Create root directory
			new File(root).mkdirs();
			log.info("root folder for web files created.");
			
			// Create sub directories
			directories.forEach((dir) -> {
				if(!(new File(root+dir).isDirectory())) new File(root+dir).mkdirs();
			});
			log.info("All directories for web files created.");
			
			// Move web files to unpack directories
			int size = webFiles.size();
			boolean n0 = true, n1 = true, n2 = true;
			boolean n3 = true, n4 = true;
			for(int i = 0; i < size; i++) {
				
				
				// Notification about web files progress
				if(i > (size/5*4) && n4) { n4 = false;
					log.info("Build web files: 80% ");
				}else if(i > (size/5*3) && n3) { n3 = false;
					log.info("Build web files: 60% ");
				}else if(i > (size/5*2) && n2) { n2 = false;
					log.info("Build web files: 40% ");
				}else if(i > (size/5*1) && n1) { n1 = false;
					log.info("Build web files: 20% ");
				}else if(n0) { n0 = false;
					log.info("Build web files: 0%");}
			}log.info("Build web completed.");
			return true;
		}else {
			log.info("Web files already placed. To replace them delete/rename the folder \"web\" on server folder.");
			return true;
		}
	}
}
