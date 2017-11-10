package com.ckjava.io.command.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.client.ClientFutureInfo;
import com.ckjava.io.command.client.ClientInfo;

public class CheckClientResultRunner implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(CheckClientResultRunner.class);
	
	// 客户端最大允许连接时间,超过后断开连接, TODO 是否可以允许客户端自定义服务时间
	private final long timeOut = 5*60*1000; // 5分钟
	
	private ServerSocket serverSocket;
	private List<ClientFutureInfo> clientResultList;
	
	public CheckClientResultRunner(ServerSocket serverSocket, List<ClientFutureInfo> clientResultList) {
		this.serverSocket = serverSocket;
		this.clientResultList = clientResultList;
	}

	@Override
	public void run() {
		while(true) {
            if (serverSocket.isClosed()) {
                break;
            }
            
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("check client result sleep 1000 has error", e);
			}
            
            synchronized (clientResultList) {
                for (Iterator<ClientFutureInfo> it = clientResultList.listIterator();it.hasNext();) {
                	ClientFutureInfo futureInfo = it.next();
                	ClientInfo clientInfo = futureInfo.getClientInfo();
                	Future<String> clientFuture = futureInfo.getClientFuture();
                	if (clientFuture.isDone()) {
                		try {
                			clientInfo.setRunResult(clientFuture.get());
							logger.info(clientInfo.getClientInfo() + ", runResult=" + clientInfo.getRunResult());
						} catch (Exception e) {
							logger.error("check client result has error", e);
						}
                		
                		it.remove();
                	} else if ((System.currentTimeMillis() - clientInfo.getServerAcceptTime()) > timeOut) {
                		clientInfo.setRunResult("time out, try close");
                		if (clientFuture.cancel(true)) {
                			logger.info(clientInfo.getClientInfo() + ", runResult=" + clientInfo.getRunResult());
                			// 关闭客户端占用的socket
                			if (!clientInfo.getSocket().isClosed()) {
                    			try {
    								clientInfo.getSocket().close();
    							} catch (IOException e) {
    								logger.error("CheckClientResultRunner close client = "+clientInfo.getClientInfo()+" has error", e);
    							}
                    		}
                			
                			it.remove();
                		}
                	}
                	
                }
            }
            
        }
	}

}
