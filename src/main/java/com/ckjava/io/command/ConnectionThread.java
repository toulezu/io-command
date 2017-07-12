package com.ckjava.io.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.io.command.handler.AsyncCommandHandler;
import com.ckjava.io.command.handler.CommandHandler;
import com.ckjava.io.command.handler.GetFileFromServerHandler;
import com.ckjava.io.command.handler.SendFileToServerHandler;
import com.ckjava.io.command.thread.ThreadService;
import com.ckjava.utils.StringUtils;

/**
 * 处理每个用户的一次连接请求
 * @author chen_k
 *
 */
public class ConnectionThread implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(ConnectionThread.class);
	private static final String COMMAND_REG = "(\\$\\{.*\\})";
	
    private Socket socket;
    private ServerConnection connection;
    private boolean isRunning;

    public ConnectionThread(Socket socket) {
        this.socket = socket;
        connection = new ServerConnection(socket);
        isRunning = true;
    }

    @Override
    public void run() {
    	// read message from socket
    	BufferedReader reader = null;
    	try {
    		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			logger.error("ConnectionThread read InputStream from Socket has error", e);
		}
    	
    	String command = null;
        while(isRunning) {
            // Check whether the socket is closed.
            if (socket.isClosed()) {
                isRunning = false;
                break;
            }
            try {
            	if (reader.ready() && StringUtils.isNotBlank((command = reader.readLine()))) {
                	logger.info("client send command:" + command);
                	String detail = getCommandDetail(reader.readLine());
                	if (StringUtils.isNotNullAndNotBlank(detail)) {
                		logger.info("client send command detail:" + detail);	
                	}
                	
                	switch (command) {
    				case IOSigns.CLOSE_SERVER_SIGN:// 关闭 当客户端发送数据,读取响应后,不仅关闭自己的socket,还要通知服务器端关闭自己的socket
    					isRunning = false;
                		logger.info("client want server close, server close socket");
    					break;
    				case IOSigns.RUN_ASYNC_COMMAND_SIGN: // 执行异步命令
    					ThreadService.getExecutorService().submit(new AsyncCommandHandler(connection, reader.readLine(), detail));
    					continue;
    				case IOSigns.RUN_SYNC_COMMAND_SIGN:
    					ThreadService.getExecutorService().submit(new CommandHandler(connection, reader.readLine(), detail));
    					continue;
    				case IOSigns.READ_FILE_SIGN: // client read file from server
    					ThreadService.getExecutorService().submit(new GetFileFromServerHandler(connection, detail));
    					continue;
    				case IOSigns.WRITE_FILE_SIGN: // client write file to server
    					Future<?> result = ThreadService.getExecutorService().submit(new SendFileToServerHandler(connection, detail));
    					result.get(); // call the thread stop until finish transfer the file
    					continue;
    				default:
    					continue;
    				}
                	logger.info("server finish process");
                }
			} catch (Exception e) {
				isRunning = false;
				logger.error("ConnectionThread read InputStream from Socket ready() or readLine() has error", e);
				break;
			}
        }
    }
    
    public String getCommandDetail(String message) {
    	if (StringUtils.isNotBlank(message) && message.contains("${") && message.contains("}")) {
			Pattern pattern = Pattern.compile(COMMAND_REG);
			Matcher matcher = pattern.matcher(message);
			if (matcher.find() && matcher.groupCount() == 1) {
				connection.writeUTFString(IOSigns.FOUND_COMMAND);
				
				String matcherStr = matcher.group(1); // 获取匹配的数字，从1开始
				return matcherStr.replaceAll("\\$\\{", "").replaceAll("\\}", "");
			} else {
				connection.writeUTFString(IOSigns.NOT_FOUND_COMMAND);
			}
    	} else {
    		connection.writeUTFString(IOSigns.NOT_FOUND_COMMAND);
    	}
    	return null;
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