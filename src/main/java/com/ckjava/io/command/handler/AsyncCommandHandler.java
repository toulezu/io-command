package com.ckjava.io.command.handler;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.utils.CommandUtils;
import com.ckjava.utils.StringUtils;
import com.ckjava.io.command.Connection;
import com.ckjava.io.command.IOSigns;

/**
 * 处理命令
 * 
 * @author chen_k
 *
 * 2017年4月11日-下午4:10:44
 */
public class AsyncCommandHandler implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(AsyncCommandHandler.class);

	private static final String COMMAND_REG = "(\\$\\{.*\\})";
	
	private Connection connection;
	private String message;
	

	public AsyncCommandHandler(Connection connection, String message) {
		super();
		this.connection = connection;
		this.message = message;
	}

	@Override
	public void run() {
		if (StringUtils.isNotBlank(message) && message.contains("${") && message.contains("}")) {
			Pattern pattern = Pattern.compile(COMMAND_REG);
			Matcher matcher = pattern.matcher(message);
			if (matcher.find() && matcher.groupCount() == 1) {
				connection.writeUTFString(IOSigns.FOUND_COMMAND);
				
				String matcherStr = matcher.group(1); // 获取匹配的数字，从1开始
				String command = matcherStr.replaceAll("\\$\\{", "").replaceAll("\\}", "");
				try {
					CommandUtils.execTask(command, "start_robot", connection.getSocketOutputStream());
					connection.writeUTFString(IOSigns.FINISH_RUN_ASYNC_COMMAND_SIGN);
				} catch (IOException e) {
					logger.error("AsyncCommandHandler execTask has error", e);
				}
				
			} else {
				connection.writeUTFString(IOSigns.NOT_FOUND_COMMAND);
			}
		} else {
			connection.writeUTFString(IOSigns.NOT_FOUND_COMMAND);
		}
	
	}

}