package com.ckjava.io.command.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.thread.ThreadService;

/**
 * client 每创建一个连接就新建一个 ConnectionThread 对象并放入 connThreads 队列中
 * 当 client close 的时候最好也通知 Server 端关闭 socket 移除相关线程
 * 
 * @author chen_k
 *
 * 2017年4月7日-下午7:58:29
 */
public class ListeningThread extends Thread {
	
	private final Logger logger = LoggerFactory.getLogger(ListeningThread.class);
	
    private ServerSocket serverSocket;
    private boolean isRunning;

    public ListeningThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        isRunning = true;
    }

    @Override
    public void run() {
        while(isRunning) {
            if (serverSocket.isClosed()) {
                isRunning = false;
                break;
            }
            
            try {
            	// server 端卡在这里当有  client 发起连接的时候才会继续运行, 每个连接创建一个  ConnectionThread 对象
            	// 每个 ConnectionThread 对象处理一个 Client
                Socket socket = serverSocket.accept();
                logger.info("client accept");
                ThreadService.getExecutorService().submit(new ConnectionThread(socket));
            } catch (IOException e) {
            	logger.error("ListeningThread.run has error", e);
            }
        }
    }
    
    public void stopRunning() {
        isRunning = false;
    }
} 