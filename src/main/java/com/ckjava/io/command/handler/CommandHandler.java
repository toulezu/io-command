package com.ckjava.io.command.handler;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.io.command.server.ServerConnection;
import com.ckjava.utils.ArrayUtils;
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
	
	private static final String SEMICOLON = ";";
	private static final String EQUALS = "=";
	
	private ServerConnection connection;
	private String detail;
	
	public CommandHandler(ServerConnection connection, String detail) {
		super();
		this.connection = connection;
		this.detail = detail;
	}

	@Override
	public void run() {
		try {
			String[] details = detail.split(SEMICOLON);
			String detail = ArrayUtils.getValue(details, 0);
			String dir = ArrayUtils.getValue(details, 1);
			String[] charsets = ArrayUtils.getValue(details, 2).split(EQUALS);
			String charset = ArrayUtils.getValue(charsets, 1);
			
			CommandUtils.execTask(detail, null, new File(dir), charset, connection.getSocketOutputStream());
			connection.writeUTFString(IOSigns.FINISH_RUN_COMMAND_SIGN);
		} catch (IOException e) {
			logger.error("CommandHandler run has error", e);
		}
	}

}