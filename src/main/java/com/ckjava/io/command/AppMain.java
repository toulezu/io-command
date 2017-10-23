package com.ckjava.io.command;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.server.SocketServer;

/**
 * the main class of this app
 * @author chen_k
 *
 */
public class AppMain {

	private static Logger logger = LoggerFactory.getLogger(AppMain.class);
	
	public static void main(String[] args) {
		
		String port = null;
		try {
			InputStream in = AppMain.class.getResourceAsStream("/application.properties");
			Properties p = new Properties();
			p.load(in);
			
			port = p.getProperty("server.port");
		} catch (Exception e) {
			logger.error("error read port", e);
		}
		
		if (port != null) {
			new SocketServer(Integer.parseInt(port));
			
			logger.info("server start, port is " + port);
		} else {
			logger.error("error read port, port is null");
			System.exit(0);
		}
		
	}
}
