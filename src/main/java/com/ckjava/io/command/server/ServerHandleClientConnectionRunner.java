package com.ckjava.io.command.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.client.ClientInfo;
import com.ckjava.io.command.constants.IOSigns;
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
public class ServerHandleClientConnectionRunner implements Callable<ClientInfo> {
	
	private static Logger logger = LoggerFactory.getLogger(ServerHandleClientConnectionRunner.class);
	private static final String COMMAND_REG = "(\\$\\{.*\\})";
	
	private ClientInfo clientInfo;
    private ServerConnectionAction connection;
    private boolean isRunning;

    public ServerHandleClientConnectionRunner(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
        connection = new ServerConnectionAction(clientInfo.getSocket());
        isRunning = true;
    }

    @Override
    public ClientInfo call() throws Exception {
    	long start = System.currentTimeMillis();
    	// read message from socket
    	BufferedReader reader = null;
    	try {
    		reader = new BufferedReader(new InputStreamReader(clientInfo.getSocket().getInputStream()));
		} catch (Exception e) {
			logger.error("client is "+clientInfo.getClientInfo()+", ServerHandleClientConnectionRunner read InputStream from Socket has error", e);
		}
    	
    	String command = null;
        while(isRunning) {
            // Check whether the socket is closed.
            if (clientInfo.getSocket().isClosed()) {
                isRunning = false;
                break;
            }
            try {
            	if (reader.ready() && StringUtils.isNotBlank((command = reader.readLine()))) {
            		if (StringUtils.isBlank(command)) {
            			stopRunning();
            			clientInfo.setRunResult("client send unsupported command");
            			return clientInfo;
            		}
            		// TODO 这里以后增加客户端权限校验功能
                	logger.info("{} send command = {}", clientInfo.getClientInfo(), command);
                	String detail = getCommandDetail(reader.readLine());
                	if (StringUtils.isNotNullAndNotBlank(detail)) {
                		logger.info("{} send command detail = {}", clientInfo.getClientInfo(), detail);	
                	}
                	
                	switch (command) {
    				case IOSigns.CLOSE_SERVER_SIGN:// 关闭 当客户端发送数据,读取响应后,不仅关闭自己的socket,还要通知服务器端关闭自己的socket
    					stopRunning();
                		logger.info("{} want server close, server close socket", clientInfo.getClientInfo());
    					break;
    				case IOSigns.RUN_COMMAND_SIGN:
    					ThreadService.getExecutorService().submit(new CommandHandler(connection, detail));
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
                }
			} catch (Exception e) {
				stopRunning();
				logger.error("client is "+clientInfo.getClientInfo()+", ServerHandleClientConnectionRunner read InputStream from Socket ready() or readLine() has error", e);
				break;
			}
        }
        
        long totalConsume = System.currentTimeMillis() - start;
        clientInfo.setRunResult("total consume time is:" + totalConsume + "ms");
        return clientInfo;
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
        	if (!clientInfo.getSocket().isClosed()) {
        		clientInfo.getSocket().close();	
        	}
        } catch (IOException e) {
        	logger.error("client is "+clientInfo.getClientInfo()+", close socket has error", e);
        }
    }

	public boolean isRunning() {
		return isRunning;
	}
    
}