package me.laravieira.willy.web;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.laravieira.willy.Willy;
import me.laravieira.willy.config.Config;

public class WebResquests {
	
	
	public WebResquests(Socket socket) throws IOException {
		
		InputStream          requestBody    = socket.getInputStream();
		PrintWriter          responseHeader = new PrintWriter(socket.getOutputStream());
		BufferedOutputStream responseBody   = new BufferedOutputStream(socket.getOutputStream());
		
		Map<String, String> header = new HashMap<String, String>();
		String c_header = "";
		int    rchar  = 0;
		while((rchar = requestBody.read()) != -1) {
			c_header += (char)rchar;
			if(c_header.endsWith("\r\n\r\n")) break;
		}
		String[] s_header = c_header.split("\r\n");
		for(int i = 0; i < s_header.length-1; i++) {
			if(i == 0) {
				String[] p_header = s_header[i].split(" ");
				header.put("Method", p_header[0]);
				header.put("Path", p_header[1]);
				header.put("Protocol", p_header[2]);
			}else {
				String[] p_header = s_header[i].split(": ");
				if(p_header.length > 2)
					for(int j = 2; j < p_header.length; j++)
						p_header[1] += p_header[j];
				header.put(p_header[0], p_header[1]);
			}
		}
		
		if(c_header.startsWith("TRACE")) {
			String h_header = "";
			for(int i = 0; i < s_header.length; i++)
				h_header += s_header[i]+"\r\n";
			header.put("Header", h_header);
		}
		
		processRequest(header, new InputStreamReader(requestBody), responseHeader, responseBody);
		
		requestBody.close();
		responseHeader.close();
		responseBody.close();
		socket.close();
	}
	
	private void processRequest(Map<String, String> inHeader, InputStreamReader inBody, PrintWriter outHeader, BufferedOutputStream outBody) throws IOException {

		if(inHeader.get("Method").equals("GET")) {
			processRequestGET(inHeader, inBody, outHeader, outBody, true);
		
		}else if(inHeader.get("Method").equals("HEAD")) {
			processRequestGET(inHeader, inBody, outHeader, outBody, false);
			
		//}else if(inHeader.get("Method").equals("POST")) {
		//	processRequestPOST(inHeader, inBody, outHeader, outBody);
		
		}else if(inHeader.get("Method").equals("OPTIONS")) {
			boolean cors = false;
			outHeader.println("HTTP/1.1 200 OK");
			//outHeader.println("Strict-Transport-Security: max-age=63072000; preload");
			outHeader.println("Date: "+new Date().toString());
			outHeader.println("Allow: GET, HEAD, OPTIONS, TRACE");
			if(inHeader.containsKey("Origin")) {
				outHeader.println("Access-Control-Allow-Origin: "+inHeader.get("Origin"));
				cors = true;
			}if(inHeader.containsKey("Access-Control-Request-Method")) {
				List<String> supported = new ArrayList<String>();
				supported.add("GET");
				supported.add("HEAD");
				//supported.add("POST");
				supported.add("OPTIONS");
				supported.add("TRACE");
				String[] requested = inHeader.get("Access-Control-Request-Method").split(", ");
				for(int i = 0; i < requested.length; i++) {
					if(supported.contains(requested[i])) {
						outHeader.println("Access-Control-Allow-Methods: GET, HEAD, OPTIONS, TRACE");
						cors = true;
					}
				}
			}
			if(inHeader.containsKey("Access-Control-Request-Headers")) {
				outHeader.println("Access-Control-Allow-Headers: "+inHeader.get("Access-Control-Request-Headers"));
				cors = true;
			}
			if(cors) {outHeader.println("Access-Control-Max-Age: 86400");}
			outHeader.println("Connection: Keep-alive");
			outHeader.println("Content-Length: 0");
			outHeader.println();
			outHeader.flush();
			outBody.flush();
		
		}else if(inHeader.get("Method").equals("TRACE")) {
			outHeader.println("HTTP/1.1 200 OK");
			outHeader.println("Server: "+Config.getName()+"/"+Willy.getFullVersion().substring(1));
			//outHeader.println("Strict-Transport-Security: max-age=63072000; preload");
			outHeader.println("Date: "+new Date().toString());
			outHeader.println("Connection: Closed");
			outHeader.println("Content-Type: message/html");
			outHeader.println("Content-Length: "+inHeader.get("Header").length());
			outHeader.println();
			outHeader.flush();
			outBody.write(inHeader.get("Header").getBytes());
			outBody.flush();
			
		}else {
			outHeader.println("HTTP/1.1 200 OK");
			outHeader.println("Server: "+Config.getName()+"/"+Willy.getFullVersion().substring(1));
			//outHeader.println("Strict-Transport-Security: max-age=63072000; preload");
			outHeader.println("Date: "+new Date().toString());
			outHeader.println("Allow: GET, HEAD, OPTIONS, TRACE");
			outHeader.println("Connection: Closed");
			outHeader.println("Content-Length: 0");
			outHeader.println();
			outHeader.flush();
			outBody.flush();
		}
	}

	private void processRequestGET(Map<String, String> inHeader, InputStreamReader inBody, PrintWriter outHeader, BufferedOutputStream outBody, boolean get) throws IOException {

		WebFiles file = new WebFiles(inHeader.get("Path"));
		if(file.exist()) {
			outHeader.println("HTTP/1.1 200 OK");
			outHeader.println("Server: "+Config.getName()+"/"+Willy.getFullVersion().substring(1));
			//outHeader.println("Strict-Transport-Security: max-age=63072000; preload");
			outHeader.println("Date: "+new Date().toString());
			outHeader.println("Connection: Keep-alive");
			outHeader.println("Content-Type: "+file.contentType());
			outHeader.println("Content-Length: "+file.length());
			outHeader.println();
			outHeader.flush();
			
			WritableByteChannel wbc = Channels.newChannel(outBody);
			file.getFile().getChannel().transferTo(0, file.length(), wbc);
			outBody.flush();
			wbc.close();
		}else {
			WebFiles error = new WebFiles(404);
			outHeader.println("HTTP/1.1 404 Not Found");
			outHeader.println("Server: "+Config.getName()+"/"+Willy.getFullVersion().substring(1));
			outHeader.println("Date: "+new Date().toString());
			//outHeader.println("Strict-Transport-Security: max-age=63072000; preload");
			if(error.exist()) {
				outHeader.println("Connection: Keep-alive");
				outHeader.println("Content-Type: "+error.contentType());
				outHeader.println("Content-Length: "+error.length());
				outHeader.println();
				outHeader.flush();

				WritableByteChannel wbce = Channels.newChannel(outBody);
				error.getFile().getChannel().transferTo(0, error.length(), wbce);
				outBody.flush();
				wbce.close();
			}else {
				outHeader.println("Connection: Closed");
				outHeader.println("Content-Length: 0");
				outHeader.println();
				outHeader.flush();
				outBody.flush();
				
			}
		}
	}
	
//	private void processRequestPOST(Map<String, String> inHeader, InputStreamReader inBody, PrintWriter outHeader, BufferedOutputStream outBody) throws IOException {
//
//		JSONObject body = new JSONObject();
//		body.put("title", "POST Response");
//		body.put("Server", Config.getName());
//		outHeader.println("HTTP/1.1 200 OK");
//		outHeader.println("Server: "+Config.getName()+"/"+Willy.getFullVersion().substring(1));
//		outHeader.println("Date: "+new Date().toString());
//		//outHeader.println("Strict-Transport-Security: max-age=63072000; preload");
//		outHeader.println("Connection: Closed");
//		outHeader.println("Content-Type: application/json; charset=utf-8");
//		outHeader.println("Content-Length: "+body.toString().length());
//		outHeader.println();
//		outHeader.flush();
//		
//		outBody.write(body.toString().getBytes(), 0, body.toString().length());
//		outBody.flush();
//	}
	
}
