package com.ckjava.io.command.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.Wait;
import com.ckjava.io.command.client.listener.OnGetFileFromServerListener;
import com.ckjava.io.command.client.listener.OnReceiveCommandListener;
import com.ckjava.io.command.client.listener.OnSendFileToServerListener;
import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.utils.StringUtils;

public class SocketClient {

	private static Logger logger = LoggerFactory.getLogger(SocketClient.class);
	
	private static final long TIMEOUT = 50000l;

	private Socket socket;

	public SocketClient(InetAddress ip, int port) {
		try {
			socket = new Socket(ip, port);
		} catch (IOException e) {
			logger.error("init SocketClient has error", e);
		}
	}
	
	/**
	 * 获取命令的执行结果
	 * 
	 * @param client SocketClient
	 * @return String
	 */
	@Deprecated
	public String getRunCommandResult(SocketClient client) {
		StringBuilder result = new StringBuilder();
		
		String sign = client.readUTFString();
		if (sign.equals(IOSigns.FOUND_COMMAND)) {
			while (true) {
				sign = client.readUTFString();
				logger.info(sign);
				
				if (sign.equals(IOSigns.FINISH_RUN_COMMAND_SIGN)) {
					client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe(); // 通知服务器端关闭
					break;
				} else {
					result.append(sign).append(StringUtils.LF);	
				}
			}
		} else {
			logger.info(IOSigns.NOT_FOUND_COMMAND);
		}
		return result.toString();
	}
	
	/**
	 * 发送命令后基于事件对结果进行处理
	 * @param client
	 * @param onReceiveCommandListener
	 * @return
	 */
	public void setOnReceiveCommandListener(OnReceiveCommandListener onReceiveCommandListener) {
		String sign = this.readUTFString();
		onReceiveCommandListener.onStartExecuteCommand(sign);
		if (sign.equals(IOSigns.FOUND_COMMAND)) {
			while (true) {
				sign = this.readUTFString();
				
				if (sign.equals(IOSigns.FINISH_RUN_COMMAND_SIGN)) {
					onReceiveCommandListener.onFinishExecuteCommand();
					break;
				} else {
					onReceiveCommandListener.onExecuteCommand(sign);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param client
	 * @param localPath
	 * @return
	 */
	@Deprecated
	public String getGetFileFromServerResult(SocketClient client, String localPath) {
		StringBuilder result = new StringBuilder();
		
		String sign = client.readUTFString();
		if (sign.equals(IOSigns.FOUND_COMMAND)) {
			sign = client.readUTFString();
			if (sign.equals(IOSigns.FOUND_FILE_SIGN)) {
				String fileName = client.readUTFString();
				Long fileSize = client.readLong();
				String localFile = localPath + fileName;
				
				String saveResult = client.saveFileFromServer(localFile, fileSize);
				result.append(saveResult);
				
				logger.debug(saveResult);
				client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe(); // 通知服务器端关闭
			} else {
				logger.error(IOSigns.NOT_FOUND_FILE_SIGN);
			}
		} else {
			logger.error(IOSigns.NOT_FOUND_COMMAND);
		}
		return result.toString();
	}
	
	/**
	 * 发送命令后基于事件对结果进行处理
	 * @param client
	 * @param onReceiveCommandListener
	 * @return
	 */
	public void onGetFileFromServerListener(OnGetFileFromServerListener listener) {
		String sign = this.readUTFString();
		listener.onStartGetFile(sign);
		if (sign.equals(IOSigns.FOUND_COMMAND)) {
			sign = this.readUTFString();
			if (sign.equals(IOSigns.FOUND_FILE_SIGN)) {
				String fileName = this.readUTFString();
				Long fileSize = this.readLong();
				
				String localFile = listener.onFindFile(fileName, fileSize);
				
				String saveResult = this.saveFileFromServer(localFile, fileSize);
				
				listener.onFinishSaveFile(saveResult);
			} else {
				listener.onNotFindFile();
			}
		}
	}
	
	@Deprecated
	public String getSendFileToServerResult(SocketClient client, File localFile) {
		StringBuilder result = new StringBuilder();
		
		String sign = client.readUTFString();
		if (sign.equals(IOSigns.FOUND_COMMAND)) {
			sign = client.readUTFString();
			if (sign.equals(IOSigns.WRITE_FILE_SUCCESS)) {

				client.sendFileToServer(localFile);
				
				sign = client.readUTFString();
				logger.debug(sign);
				
				if (sign.equals(IOSigns.FINISH_SIGN)) {
					client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe(); // 通知服务器端关闭	
				} else {
					result.append(sign).append(StringUtils.LF);
				}
			} else {
				logger.debug(IOSigns.WRITE_FILE_FAIL);
			}
		} else {
			logger.debug(IOSigns.NOT_FOUND_COMMAND);
		}
		
		return result.toString();
	}
	

	/**
	 * 将文件发送到服务器端后的事件处理
	 * 
	 * @param listener
	 */
	public void onSendFileToServerListener(OnSendFileToServerListener listener) {
		String sign = this.readUTFString();
		listener.onStartSendFile(sign);
		if (sign.equals(IOSigns.FOUND_COMMAND)) {
			sign = this.readUTFString();
			if (sign.equals(IOSigns.WRITE_FILE_SUCCESS)) {
				File localFile = listener.onSendFile();
				
				String clientResult = this.sendFileToServer(localFile);
				String serverResult = this.readUTFString();
				
				sign = this.readUTFString();
				if (sign.equals(IOSigns.FINISH_SIGN)) {
					listener.onFinishSendFile(clientResult, serverResult);
				}
			} else {
				listener.onFailSendFile();
			}
		}
	}

	public SocketClient send(String message) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			writer.println(message);
		} catch (IOException e) {
			logger.error("SocketClient send method has error", e);
		}
		return this;
	}
	
	/**
	 * cause current thread to wait until InputStream has data
	 * 
	 * @param dis InputStream from socket
	 * @throws IOException
	 */
	public void waitRead(final InputStream dis) throws IOException {
		try {
			new Wait() {
				@Override
				public boolean until() throws IOException  {
					return dis.available() > 0;
				}
			}.wait("SocketClient waitRead(InputStream) has error", TIMEOUT);
		} catch (Exception e) {
			throw new RuntimeException("SocketClient waitRead(InputStream) has error", e);
		}
	}
	
	public void waitRead(final Reader reader) throws IOException {
		try {
			new Wait() {
				@Override
				public boolean until() throws IOException  {
					return reader.ready();
				}
			}.wait("SocketClient waitRead(Reader) has error", 500000);
		} catch (Exception e) {
			throw new RuntimeException("SocketClient waitRead(Reader) has error", e);
		}
	}

	public String readUTFString() {
		try {
			DataInputStream	dis = new DataInputStream(socket.getInputStream()); // 外部输出流,不要关闭
			waitRead(dis);
			return dis.readUTF();
		} catch (IOException e) {
			logger.error("SocketClient readString method has error", e);
			return null;
		}
	}
	
	public String readString(String charset) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset)); // 外部输出流,不要关闭
			waitRead(reader);
			return reader.readLine();
		} catch (IOException e) {
			logger.error("SocketClient readString method has error", e);
			return null;
		}
	}

	public Long readLong() {
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(socket.getInputStream());
			waitRead(dis);
			return dis.readLong();
		} catch (IOException e) {
			logger.error("SocketClient readLong method has error", e);
			return 0L;
		}
	}

	/**
	 * client get file from server
	 * 
	 * @param savePath
	 * @param fileSize
	 * @return
	 */
	public String saveFileFromServer(String savePath, Long fileSize) {
		DataOutputStream fileOut = null;
		try {
			fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
			DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			byte[] bytes = new byte[2048];
			waitRead(dis);
			int tempSumLen = 0;
			int readLen = 0;
			
			long firstTime = System.currentTimeMillis();
			while ((readLen = dis.read(bytes)) != -1) {
				tempSumLen += readLen;
				
				long nowTime = System.currentTimeMillis();
				if (nowTime - firstTime >= 1000) {
					System.out.printf("\r%8s:\t%4s%%", "receive size", (tempSumLen/fileSize)*100);
					firstTime = nowTime;
				}
				
				fileOut.write(bytes, 0, readLen);
				
				if (tempSumLen == fileSize) { // 必须通过判断读取的字节数和文件字节数是否相等来退出循环,因为当前的 socket 没有关闭,dis.read 会卡死在这里
					break;
				}
			}
			
			if (tempSumLen != fileSize) {
				return "file size is " + fileSize + " byte, get file from server size is " + tempSumLen + " byte";
			} else {
				return "file is ok, get file from server size is " + tempSumLen + " byte";
			}
		} catch (Exception e) {
			logger.error("SocketClient getFileFromServer has error", e);
			return "SocketClient getFileFromServer has error";
		} finally {
			try {
				fileOut.close();
			} catch (Exception e2) {
			}
		}
	}
	
	/**
	 * client send file to server
	 * 
	 * @param savePath
	 * @param fileSize
	 * @return
	 */
	public String sendFileToServer(File file) {
    	DataInputStream dis = null;
        try {
        	dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        	DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        	byte[] bytes = new byte[2048];
        	int tempLen = 0;
        	int readLen = 0;
        	while ((readLen = dis.read(bytes)) != -1) {
        		tempLen += readLen;
				dos.write(bytes, 0, readLen);
			}
        	return "client send file to server size is " + tempLen + " byte";
        } catch (IOException e) {
        	logger.error("SocketClient sendFileToServer has error", e);
        	return "SocketClient sendFileToServer has error";
        } finally {
        	try {
        		dis.close();
			} catch (Exception e2) {
			}
        }
    }

	public InputStream getSocketInputStream() throws IOException {
		return socket.getInputStream();
	}

	public SocketClient closeMe() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			logger.error("SocketClient close method has error", e);
		}
		return this;
	}
}