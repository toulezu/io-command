package com.ckjava.io.command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * client 每创建一个连接就新建一个 ConnectionThread 对象并放入 connThreads 队列中
 * 当 client close 的时候最好也通知 Server 端关闭 socket 移除相关线程
 * 
 * @author chen_k
 *
 * 2017年4月7日-下午7:58:29
 */
public class ListeningThread implements Runnable {
    private SocketServer socketServer;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private ExecutorService executeService = Executors.newCachedThreadPool();

    public ListeningThread(SocketServer socketServer, ServerSocket serverSocket) {
        this.socketServer = socketServer;
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
                Socket socket = serverSocket.accept();
                executeService.submit(new ConnectionThread(socket, socketServer));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void stopRunning() {
        isRunning = false;
        executeService.shutdown();
    }
} 