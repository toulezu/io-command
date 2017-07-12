package com.ckjava.io.command.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.ServerConnection;
import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.utils.CommandUtils;

/**
 * handle command
 * 
 * @author chen_k
 *
 * 2017年4月11日-下午4:10:44
 */
public class AsyncCommandHandler implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(AsyncCommandHandler.class);
	
	private ServerConnection connection;
	private String detail;
	private String charset;
	
	public AsyncCommandHandler(ServerConnection connection, String charset, String message) {
		super();
		this.connection = connection;
		this.charset = charset;
		this.detail = message;
	}

	@Override
	public void run() {
		try {
			//CommandUtils.execTask(detail, charset, "start_robot", connection.getSocketOutputStream());
			connection.writeUTFString(IOSigns.FINISH_RUN_ASYNC_COMMAND_SIGN);
		} catch (IOException e) {
			logger.error("AsyncCommandHandler execTask has error", e);
		}
	}

}