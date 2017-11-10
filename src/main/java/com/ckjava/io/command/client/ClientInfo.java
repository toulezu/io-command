package com.ckjava.io.command.client;

import java.net.Socket;

public class ClientInfo {
	private String hostName;
	private String hostAddress;
	private int hostPort;
	private String runResult;
	private Socket socket;
	private long serverAcceptTime;
	
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
	public long getServerAcceptTime() {
		return serverAcceptTime;
	}
	public void setServerAcceptTime(long serverAcceptTime) {
		this.serverAcceptTime = serverAcceptTime;
	}
	public ClientInfo(Socket socket, String hostName, String hostAddress, int hostPort, long serverAcceptTime) {
		super();
		this.socket = socket;
		this.hostName = hostName;
		this.hostAddress = hostAddress;
		this.hostPort = hostPort;
		this.serverAcceptTime = serverAcceptTime;
	}
	public String getClientInfo() {
		return "hostName:"+hostName+"["+hostAddress+":"+hostPort+"]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostAddress == null) ? 0 : hostAddress.hashCode());
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + hostPort;
		result = prime * result + ((runResult == null) ? 0 : runResult.hashCode());
		result = prime * result + (int) (serverAcceptTime ^ (serverAcceptTime >>> 32));
		result = prime * result + ((socket == null) ? 0 : socket.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientInfo other = (ClientInfo) obj;
		if (hostAddress == null) {
			if (other.hostAddress != null)
				return false;
		} else if (!hostAddress.equals(other.hostAddress))
			return false;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (hostPort != other.hostPort)
			return false;
		if (runResult == null) {
			if (other.runResult != null)
				return false;
		} else if (!runResult.equals(other.runResult))
			return false;
		if (serverAcceptTime != other.serverAcceptTime)
			return false;
		if (socket == null) {
			if (other.socket != null)
				return false;
		} else if (!socket.equals(other.socket))
			return false;
		return true;
	}
	
}
