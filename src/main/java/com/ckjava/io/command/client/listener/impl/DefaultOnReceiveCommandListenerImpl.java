package com.ckjava.io.command.client.listener.impl;

import com.ckjava.io.command.client.SocketClient;
import com.ckjava.io.command.client.listener.OnReceiveCommandListener;
import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.utils.StringUtils;

public class DefaultOnReceiveCommandListenerImpl implements OnReceiveCommandListener {

	private SocketClient client;
	
	private StringBuilder result = new StringBuilder();
	
	public DefaultOnReceiveCommandListenerImpl(SocketClient client) {
		super();
		this.client = client;
	}

	
	@Override
	public void onStartExecuteCommand(String sign) {
		System.out.println("onStartExecuteCommand = " + sign);
	}

	@Override
	public void onExecuteCommand(String sign) {
		result.append(sign).append(StringUtils.LF);
	}

	@Override
	public void onFinishExecuteCommand() {
		client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe();
	}

}
