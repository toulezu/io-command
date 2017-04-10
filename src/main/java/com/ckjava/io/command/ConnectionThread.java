package com.ckjava.io.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionThread implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(ConnectionThread.class);
	
    private Socket socket;
    private SocketServer socketServer;
    private Connection connection;
    private boolean isRunning;

    public ConnectionThread(Socket socket, SocketServer socketServer) {
        this.socket = socket;
        this.socketServer = socketServer;
        connection = new Connection(socket);
        isRunning = true;
    }

    @Override
    public void run() {
        while(isRunning) {
            // Check whether the socket is closed.
            if (socket.isClosed()) {
                isRunning = false;
                break;
            }
            
            BufferedReader reader;
            try {
            	// 从socket中读取信息
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = reader.readLine();
                if (message != null) {
                	if (message.equals(IOSigns.CLOSE_SIGN)) { // 当客户端发送数据,读取响应后,不仅关闭自己的socket,还要通知服务器端关闭自己的socket
                		isRunning = false;
                		logger.info("client want server close, server close socket");
                        break;
                	}
                	// 处理读取的信息
                	socketServer.getMessageHandler().onReceive(connection, message);
                	logger.info("server finish process");
                }
            } catch (IOException e) {
            	logger.error("read infomation from socket has error", e);
            }
        }
    }
    
    public void stopRunning() {
        isRunning = false;
        try {
        	if (!socket.isClosed()) {
        		socket.close();	
        	}
        } catch (IOException e) {
        	logger.error("close socket has error", e);
        }
    }

	public boolean isRunning() {
		return isRunning;
	}
    
}