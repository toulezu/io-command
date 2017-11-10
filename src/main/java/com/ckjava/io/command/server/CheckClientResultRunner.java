package com.ckjava.io.command.server;

import java.net.ServerSocket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.client.ClientInfo;

public class CheckClientResultRunner implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(CheckClientResultRunner.class);
	
	private ServerSocket serverSocket;
	private List<Future<ClientInfo>> clientResultList;
	
	public CheckClientResultRunner(ServerSocket serverSocket, List<Future<ClientInfo>> clientResultList) {
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
                for (Iterator<Future<ClientInfo>> it = clientResultList.listIterator();it.hasNext();) {
                	Future<ClientInfo> result = it.next();
                	if (result.isDone()) {
                		try {
                			ClientInfo clientInfo = result.get();
							logger.info(clientInfo.getClientInfo() + ", runResult=" + clientInfo.getRunResult());
						} catch (Exception e) {
							logger.error("check client result has error", e);
						}
                		
                		it.remove();
                	}
                }
            }
            
        }
	}

}
