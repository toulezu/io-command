package com.ckjava.io.command.server.handler.impl;

import java.io.File;

import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.io.command.server.ServerConnectionAction;
import com.ckjava.io.command.server.handler.Handler;

/**
 * 读取文件
 * 
 * @author chen_k
 *
 * 2017年4月11日-下午4:10:44
 */
public class GetFileFromServerHandler implements Runnable, Handler {
	
	private ServerConnectionAction connection;
	private String detail;

	public GetFileFromServerHandler(ServerConnectionAction connection, String detail) {
		super();
		this.detail = detail;
		this.connection = connection;
	}

	@Override
	public void run() {
		File file = new File(detail);
		if (file.exists()) {
			connection.writeUTFString(IOSigns.FOUND_FILE_SIGN);
			connection.writeUTFString(file.getName());
			connection.writeLong(file.length());
			connection.sendFileToClient(file);
		} else {
			connection.writeUTFString(IOSigns.NOT_FOUND_FILE_SIGN);
		}
	}

}