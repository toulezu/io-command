package com.ckjava.io.command;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.utils.CommandUtils;
import com.ckjava.utils.StringUtils;

public class CommandHandler implements MessageHandler {
	
	private static Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	private static final String COMMAND_REG = "(\\$\\{.*\\})";
	private ExecutorService executeService = Executors.newCachedThreadPool();

	@Override
	public void onReceive(Connection connection, String message) {
		if (StringUtils.isNotBlank(message) && message.contains("${") && message.contains("}")) {
			Pattern pattern = Pattern.compile(COMMAND_REG);
			Matcher matcher = pattern.matcher(message);
			if (matcher.find() && matcher.groupCount() == 1) {
				String matcherStr = matcher.group(1); // 获取匹配的数字，从1开始
				final String command = matcherStr.replaceAll("\\$\\{", "").replaceAll("\\}", "");
				Future<String> futureResult = executeService.submit(new RunCommandThread(command));

				try {
					connection.println(futureResult.get());
				} catch (Exception e) {
					logger.error("get RunCommandThread result has error", e);
				}
			} else {
				connection.println("not found command");
			}
		} else {
			connection.println("not found command");
		}
		connection.println(IOSigns.FINISH_SIGN);
	}

	private class RunCommandThread implements Callable<String> {
		String command = null;

		public RunCommandThread(final String command) {
			super();
			this.command = command;
		}

		@Override
		public String call() throws Exception {
			final StringBuffer output = new StringBuffer();
			CommandUtils.execTask(command, output);
			return output.toString();
		}

	}
}