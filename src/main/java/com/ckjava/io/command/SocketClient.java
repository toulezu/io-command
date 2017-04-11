package com.ckjava.io.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketClient {
	
	private static Logger logger = LoggerFactory.getLogger(SocketClient.class);
	
    private Socket socket;

    public SocketClient(InetAddress ip, int port) {
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
        	logger.error("init SocketClient has error", e);
        }
    }

    public void send(String message) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            writer.println(message);
        } catch (IOException e) {
        	logger.error("SocketClient send method has error", e);
        }
    }

    /**
     * 获取命令的输出结果
     * 
     * @param charsetName 服务器端编码
     * @return String 命令的输出结果
     */
    public String getResult(String charsetName) {
    	StringBuilder runResult = new StringBuilder();
        BufferedReader reader = null;
        String tempStr = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), charsetName));
            while ((tempStr = reader.readLine()) != null) {
        		if (tempStr.equals(IOSigns.FINISH_SIGN)) {
        			send(IOSigns.CLOSE_SIGN); // 关闭服务器端 socket
					close(); // 关闭客户端 socket
					break;
        		} else {
        			runResult.append(tempStr).append("\n");	
        		}
		    }
            return runResult.toString();
        } catch (IOException e) {
        	logger.error("SocketClient getResult method has error", e);
            return runResult.toString();
        } finally {
        	try {
        		reader.close();	
			} catch (Exception e2) {
			}
        }
    }
    
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
            	socket.close();
            }
        } catch (IOException e) {
        	logger.error("SocketClient close method has error", e);
        }
    }
}