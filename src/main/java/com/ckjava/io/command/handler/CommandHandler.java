package com.ckjava.io.command.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.Connection;
import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.utils.CommandUtils;

/**
 * 处理命令
 * 
 * @author chen_k
 *
 * 2017年4月11日-下午4:10:44
 */
public class CommandHandler implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(CommandHandler.class);
	
	private Connection connection;
	private String detail;
	
	public CommandHandler(Connection connection, String message) {
		super();
		this.connection = connection;
		this.detail = message;
	}

	@Override
	public void run() {
		try {
			CommandUtils.execTask(detail, connection.getSocketOutputStream());
			connection.writeUTFString(IOSigns.FINISH_RUN_SYNC_COMMAND_SIGN);
		} catch (IOException e) {
			logger.error("SyncCommandHandler execTask has error", e);
		}
	}

}