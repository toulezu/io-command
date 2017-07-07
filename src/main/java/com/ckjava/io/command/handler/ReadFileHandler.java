package com.ckjava.io.command.handler;

import java.io.File;

import com.ckjava.io.command.Connection;
import com.ckjava.io.command.constants.IOSigns;

/**
 * 读取文件
 * 
 * @author chen_k
 *
 * 2017年4月11日-下午4:10:44
 */
public class ReadFileHandler implements Runnable {
	
	private Connection connection;
	private String detail;

	public ReadFileHandler(Connection connection, String message) {
		super();
		this.detail = message;
		this.connection = connection;
	}

	@Override
	public void run() {
		File file = new File(detail);
		if (file.exists()) {
			connection.writeUTFString(IOSigns.FOUND_FILE_SIGN);
			connection.writeUTFString(file.getName());
			connection.writeLong(file.length());
			connection.writeFile(file);
		} else {
			connection.writeUTFString(IOSigns.NOT_FOUND_FILE_SIGN);
		}
	}

}