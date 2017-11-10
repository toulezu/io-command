package com.ckjava.io.command.client;

import java.net.Socket;

public class ClientInfo {
	private String hostName;
	private String hostAddress;
	private int hostPort;
	private String runResult;
	private Socket socket;
	
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getHostAddress() {
		return hostAddress;
	}
	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}
	public int getHostPort() {
		return hostPort;
	}
	public void setHostPort(int hostPort) {
		this.hostPort = hostPort;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public String getRunResult() {
		return runResult;
	}
	public void setRunResult(String runResult) {
		this.runResult = runResult;
	}
	public ClientInfo(Socket socket, String hostName, String hostAddress, int hostPort) {
		super();
		this.socket = socket;
		this.hostName = hostName;
		this.hostAddress = hostAddress;
		this.hostPort = hostPort;
	}
	public String getClientInfo() {
		return "hostName:"+hostName+"["+hostAddress+":"+hostPort+"]";
	}
}
