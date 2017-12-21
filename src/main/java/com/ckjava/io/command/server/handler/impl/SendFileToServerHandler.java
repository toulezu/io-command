package com.ckjava.io.command.server.handler.impl;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.io.command.server.ServerConnectionAction;
import com.ckjava.io.command.server.handler.BlockAble;
import com.ckjava.io.command.server.handler.Handler;
import com.ckjava.utils.ArrayUtils;

/**
 * 客户端向服务器端发送文件
 * 
 * @author chen_k
 *
 * 2017年4月11日-下午4:10:44
 */
public class SendFileToServerHandler implements Runnable, Handler, BlockAble {

	private static Logger logger = LoggerFactory.getLogger(CommandHandler.class);
	
	private ServerConnectionAction connection;
	private String detail;
	
	public SendFileToServerHandler(ServerConnectionAction connection, String detail) {
		super();
		this.connection = connection;
		this.detail = detail;
	}

	@Override
	public void run() {
		String[] details = detail.split(",");
		String filePath = ArrayUtils.getValue(details, 0);
		String fileName = ArrayUtils.getValue(details, 1);
		String fileLength = ArrayUtils.getValue(details, 2);
		
		File file = new File(filePath+"/"+fileName);
		try {
			if (file.exists()) {
				file.delete();
			}
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (file.createNewFile()) {
				connection.writeUTFString(IOSigns.WRITE_FILE_SUCCESS);
				String result = connection.getFileFromClient(file, Long.valueOf(fileLength));
				
				connection.writeUTFString(result);
				connection.writeUTFString(IOSigns.FINISH_SIGN);
			} else {
				connection.writeUTFString(IOSigns.WRITE_FILE_FAIL);
			}
		} catch (IOException e) {
			logger.error("WriteFileHandler run has error", e);
			connection.writeUTFString(IOSigns.ERROR_SIGN);
		}
	}

}