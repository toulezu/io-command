package com.ckjava.io.command.client;

import java.util.concurrent.Future;

public class ClientFutureInfo {

	private ClientInfo clientInfo;
	private Future<String> clientFuture;
	public ClientInfo getClientInfo() {
		return clientInfo;
	}
	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}
	public Future<String> getClientFuture() {
		return clientFuture;
	}
	public void setClientFuture(Future<String> clientFuture) {
		this.clientFuture = clientFuture;
	}
	public ClientFutureInfo(ClientInfo clientInfo, Future<String> clientFuture) {
		super();
		this.clientInfo = clientInfo;
		this.clientFuture = clientFuture;
	}
	
}
