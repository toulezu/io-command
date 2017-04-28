package com.ckjava.io.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.utils.StringUtils;
import com.ckjava.io.command.handler.AsyncCommandHandler;
import com.ckjava.io.command.handler.ReadFileHandler;
import com.ckjava.io.command.handler.SyncCommandHandler;
import com.ckjava.io.command.handler.WriteFileHandler;

/**
 * 处理每个用户的一次连接请求
 * @author chen_k
 *
 * 2017年4月17日-下午4:43:11
 */
public class ConnectionThread implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(ConnectionThread.class);
	
    private Socket socket;
    private Connection connection;
    private boolean isRunning;
    private ExecutorService executeService = Executors.newCachedThreadPool();

    public ConnectionThread(Socket socket) {
        this.socket = socket;
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
                String message = reader.ready() ? reader.readLine() : "";
                if (StringUtils.isNotBlank(message)) {
                	logger.info("client send message:" + message);
                	switch (message) {
					case IOSigns.CLOSE_SIGN:// 关闭 当客户端发送数据,读取响应后,不仅关闭自己的socket,还要通知服务器端关闭自己的socket
						isRunning = false;
                		logger.info("client want server close, server close socket");
						break;
					case IOSigns.RUN_ASYNC_COMMAND_SIGN: // 执行异步命令
						executeService.submit(new AsyncCommandHandler(connection, reader.readLine()));
						continue;
					case IOSigns.RUN_SYNC_COMMAND_SIGN: // 执行同步命令
						new SyncCommandHandler().onReceive(connection, reader.readLine());
						continue;
					case IOSigns.READ_FILE_SIGN: // 读取文件
						new ReadFileHandler().onReceive(connection, reader.readLine());
						continue;
					case IOSigns.WRITE_FILE_SIGN: // 写文件
						new WriteFileHandler().onReceive(connection, "", reader.readLine());
						continue;
					default:
						continue;
					}
                	logger.info("server finish process");
                }
            } catch (IOException e) {
            	logger.error("ConnectionThread read infomation from socket has error", e);
            	isRunning = false;
                break;
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