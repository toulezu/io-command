package com.ckjava.io.command.client.listener.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.client.SocketClient;
import com.ckjava.io.command.client.listener.OnGetFileFromServerListener;
import com.ckjava.io.command.constants.IOSigns;

public class DefaultOnGetFileFromServerListenerImpl implements OnGetFileFromServerListener {
	
	private Logger logger = LoggerFactory.getLogger(DefaultOnGetFileFromServerListenerImpl.class);

	private SocketClient client;
	private String localPath;
	
	public DefaultOnGetFileFromServerListenerImpl(SocketClient client, String localPath) {
		super();
		this.client = client;
		this.localPath = localPath;
	}

	@Override
	public void onStartGetFile(String sign) {
		logger.info("onStartGetFile, sign = {} ", sign);
	}

	@Override
	public String onFindFile(String fileName, Long fileSize) {
		logger.info("find server file, fileName is {}, size is {} ", fileName, fileSize);
		return localPath + fileName;
	}

	@Override
	public void onNotFindFile() {
		logger.info("not find server file ");
	}

	@Override
	public void onFinishSaveFile(String saveResult) {
		logger.info("finish save file from server, result: {} ", saveResult);
		client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe();
	}

}
