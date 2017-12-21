package com.ckjava.io.command.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.ClassInstanceHelper;
import com.ckjava.io.command.client.ClientInfo;
import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.io.command.server.handler.BlockAble;
import com.ckjava.io.command.server.handler.Handler;
import com.ckjava.io.command.thread.ThreadService;
import com.ckjava.utils.StringUtils;

/**
 * 处理每个用户的一次连接请求
 * @author chen_k
 *
 */
public class ServerHandleClientConnectionRunner implements Callable<String> {
	
	private static Logger logger = LoggerFactory.getLogger(ServerHandleClientConnectionRunner.class);
	private static final String COMMAND_REG = "(\\$\\{.*\\})";
	
	private ClientInfo clientInfo;
    private ServerConnectionAction connection;
    private Map<String, String> handlerMap;
    
    private boolean isRunning;

    public ServerHandleClientConnectionRunner(ClientInfo clientInfo, Map<String, String> handlerMap) {
        this.clientInfo = clientInfo;
        this.handlerMap = handlerMap;
        
        connection = new ServerConnectionAction(clientInfo.getSocket());
        isRunning = true;
    }

    @Override
    public String call() throws Exception {
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
            			return "client send unsupported command";
            		}
            		// TODO 这里以后增加客户端权限校验功能
                	logger.info("{} send command = {}", clientInfo.getClientInfo(), command);
                	String detail = getCommandDetail(reader.readLine());
                	if (StringUtils.isNotNullAndNotBlank(detail)) {
                		logger.info("{} send command detail = {}", clientInfo.getClientInfo(), detail);	
                	}
                	
                	if (command.equals(IOSigns.CLOSE_SERVER_SIGN)) { // 关闭 当客户端发送数据,读取响应后,不仅关闭自己的socket,还要通知服务器端关闭自己的socket
                		stopRunning();
                		logger.info("{} want server close, server close socket", clientInfo.getClientInfo());
    					break;
                	} else { 
                		// 从 handlerMap 中取出对应的 handler, handler 的约束名称为 command + Handler
                		// 比如 	command 为 GetFileFromServer, 那么对应的 handler 为  GetFileFromServerHandler
                		String handlerName = handlerMap.get(command);
                		if (StringUtils.isNotBlank(handlerName)) {
                			Object handlerObj = ClassInstanceHelper.getInstance(handlerName, new Object[] { connection, detail });
                    		if (handlerObj != null && handlerObj instanceof Handler) {
                    			Future<?> future = ThreadService.getExecutorService().submit((Runnable) handlerObj);
                    			if (handlerObj instanceof BlockAble) { // 实现了 BlockAble 的 Handler 将会导致服务器端阻塞并且不再接受新的命令
                    				future.get();
                    			}
                    		}
                		}
                	}
                }
			} catch (Exception e) {
				stopRunning();
				logger.error("client is "+clientInfo.getClientInfo()+", ServerHandleClientConnectionRunner read InputStream from Socket ready() or readLine() has error", e);
				break;
			}
        }
        
        long totalConsume = System.currentTimeMillis() - start;
        return "total consume time is:" + totalConsume + "ms";
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