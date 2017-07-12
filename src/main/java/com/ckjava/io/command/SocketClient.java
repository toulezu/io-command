package com.ckjava.io.command;

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

import com.ckjava.io.command.constants.IOSigns;

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
	 * 设置服务器端执行命令后接收命令输出使用的编码
	 * @param charset String
	 * @return SocketClient
	 */
	public SocketClient setCharset(String charset) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			writer.println(charset);
		} catch (IOException e) {
			logger.error("SocketClient setCharset method has error", e);
		}
		return this;
	}
	
	public void waitRead(final InputStream dis) throws IOException {
		try {
			new Wait() {
				@Override
				public boolean until() throws IOException  {
					return dis.available() > 0;
				}
			}.wait("SocketClient waitRead(InputStream) has error", 500000);
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
	public String getFileFromServer(String savePath, Long fileSize) {
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
	public void sendFileToServer(File file) {
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
        	logger.info("client send file to server size is " + tempLen + " byte");
        } catch (IOException e) {
        	logger.info("SocketClient sendFileToServer has error", e);
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

	/**
	 * 获取命令的输出结果
	 * 
	 * @param charsetName
	 *            服务器端编码
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
					send(IOSigns.CLOSE_SERVER_SIGN); // 通知服务器端关闭 socket
					closeMe(); // 关闭客户端 socket
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