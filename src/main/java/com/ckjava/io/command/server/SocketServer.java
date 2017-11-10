package com.ckjava.io.command.server;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketServer {
	private static Logger logger = LoggerFactory.getLogger(SocketServer.class);
	
    private ServerSocket serverSocket;

    /**
     * 启动一个监听线程 ListeningThread
     * ListeningThread 线程中再启动一个  ConnectionThread 
     * ConnectionThread 从 Socket 中读取 client 发送的数据
     * 
     * 一个 ListeningThread 启动多个 ConnectionThread
     * 
     * ListeningThread 中启动一个Socket,不停的监听来自 clint 发过来的信息
     * 
     * @param port
     * @param handler
     */
    public SocketServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
        	logger.info("init ServerSocket has error", e);
        }
        new Thread(new ServerListenClientRunner(serverSocket)).start();
    }
    
    /*
     * Ready for use.
     */
    public void close() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
        	logger.info("close ServerSocket has error", e);
        }
    }
}