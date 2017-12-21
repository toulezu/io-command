package com.ckjava.io.command.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.ClasspathPackageScanner;
import com.ckjava.io.command.client.ClientFutureInfo;
import com.ckjava.io.command.client.ClientInfo;
import com.ckjava.io.command.thread.ThreadService;
import com.ckjava.utils.StringUtils;

/**
 * client 每创建一个连接就新建一个 ConnectionThread 对象并放入 connThreads 队列中
 * 当 client close 的时候最好也通知 Server 端关闭 socket 移除相关线程
 * 
 * @author chen_k
 *
 * 2017年4月7日-下午7:58:29
 */
public class ServerListenClientRunner implements Runnable {
	
	private final Logger logger = LoggerFactory.getLogger(ServerListenClientRunner.class);
	
    private ServerSocket serverSocket;
    private List<ClientFutureInfo> clientResultList = Collections.synchronizedList(new LinkedList<ClientFutureInfo>());
    private Map<String, String> handlerMap = Collections.synchronizedMap(new HashMap<String, String>());
    private boolean isRunning;

    public ServerListenClientRunner(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        isRunning = true;
        // 启动线程检查每个客户端的执行结果
        ThreadService.getExecutorService().submit(new CheckClientResultRunner(serverSocket, clientResultList));
        // 加载服务器端的 Handler
        loadDefaultServerHandler();
    }

	private void loadDefaultServerHandler() {
		ClasspathPackageScanner scan = new ClasspathPackageScanner("com.ckjava.io.command.server.handler.impl");
		try {
			List<String> list = scan.getFullyQualifiedClassNameList();
			for (String name : list) {
				String handlerFullName = name.substring(name.lastIndexOf(".")+1);
		    	String handlerName = handlerFullName.replace("Handler", "");
		    	if (StringUtils.isBlank(handlerName)) {
		    		continue;
		    	}
				handlerMap.put(handlerName, name);
			}
			logger.info("loadDefaultServerHandler success, handlerMap size = {} ", handlerMap.size());	
		} catch (Exception e) {
			logger.error("ServerListenClientRunner loadDefaultServerHandler has error", e);
		}
        
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
                InetAddress remoteClient = socket.getInetAddress();
                
                ClientInfo clientInfo = new ClientInfo(socket, remoteClient.getHostName(), remoteClient.getHostAddress(), socket.getPort(), System.currentTimeMillis());
                
                logger.info("incoming remote client, remoteClientInfo = {}", clientInfo.getClientInfo());
                Future<String> clientFuture = ThreadService.getHandleClientService().submit(new ServerHandleClientConnectionRunner(clientInfo, handlerMap));
                
                clientResultList.add(new ClientFutureInfo(clientInfo, clientFuture));
                
            } catch (IOException e) {
            	logger.error("ListeningThread.run has error", e);
            }
        }
    }
    
    public void stopRunning() {
        isRunning = false;
    }
} 