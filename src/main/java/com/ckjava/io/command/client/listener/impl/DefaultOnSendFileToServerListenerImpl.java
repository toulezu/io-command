package com.ckjava.io.command.client.listener.impl;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.client.SocketClient;
import com.ckjava.io.command.client.listener.OnSendFileToServerListener;
import com.ckjava.io.command.constants.IOSigns;

public class DefaultOnSendFileToServerListenerImpl implements OnSendFileToServerListener {
	
	private Logger logger = LoggerFactory.getLogger(DefaultOnSendFileToServerListenerImpl.class);

	private SocketClient client;
	private File localFile;

	public DefaultOnSendFileToServerListenerImpl(SocketClient client, File localFile) {
		super();
		this.client = client;
		this.localFile = localFile;
	}

	@Override
	public void onStartSendFile(String sign) {
		logger.info("onStartSendFile, sign: {}", sign);
	}

	@Override
	public File onSendFile() {
		return localFile;
	}

	@Override
	public void onFailSendFile() {
		logger.error("fail send file to server");
	}

	@Override
	public void onFinishSendFile(String clientResult, String serverResult) {
		logger.info("finish send file to server, clientResult: {}, serverResult = {}", clientResult, serverResult);
		
		client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe();
	}

}
