package com.ckjava.io.command.handler;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ckjava.utils.StringUtils;
import com.ckjava.io.command.Connection;
import com.ckjava.io.command.IOSigns;

/**
 * 读取文件
 * 
 * @author chen_k
 *
 * 2017年4月11日-下午4:10:44
 */
public class ReadFileHandler implements MessageHandler {

	private static final String COMMAND_REG = "(\\$\\{.*\\})";

	@Override
	public void onReceive(Connection connection, String message) {
		if (StringUtils.isNotBlank(message) && message.contains("${") && message.contains("}")) {
			Pattern pattern = Pattern.compile(COMMAND_REG);
			Matcher matcher = pattern.matcher(message);
			if (matcher.find() && matcher.groupCount() == 1) {
				connection.writeUTFString(IOSigns.FOUND_COMMAND);
				
				String matcherStr = matcher.group(1); // 获取匹配的数字，从1开始
				final String filePath = matcherStr.replaceAll("\\$\\{", "").replaceAll("\\}", "");
				File file = new File(filePath);
				if (file.exists()) {
					connection.writeUTFString(IOSigns.FOUND_FILE_SIGN);
					connection.writeUTFString(file.getName());
					connection.writeLong(file.length());
					connection.writeFile(file);
				} else {
					connection.writeUTFString(IOSigns.NOT_FOUND_FILE_SIGN);
				}
			} else {
				connection.writeUTFString(IOSigns.NOT_FOUND_COMMAND);
			}
		} else {
			connection.writeUTFString(IOSigns.NOT_FOUND_COMMAND);
		}
	}

}