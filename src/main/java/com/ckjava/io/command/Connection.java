package com.ckjava.io.command;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * server socket 的包装类
 * 
 * @author chen_k
 *
 * 2017年4月11日-下午4:34:04
 */
public class Connection {
	private static Logger logger = LoggerFactory.getLogger(Connection.class);
	
    private Socket socket;

    public Connection(Socket socket) {
        this.socket = socket;
    }
    
    /**
     * 向socket中写入数据
     * 
     * @param message
     */
    public void writeUTFString(String message) {
        try {
        	DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        	dos.writeUTF(message);
        	dos.flush();
        } catch (IOException e) {
        	logger.info("Connection object writeString method has error", e);
        }
    }
    
    /**
     * 向socket中写入数据
     * 
     * @param message
     */
    public void writeBytes(String message) {
        try {
        	DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        	dos.writeUTF(message);
        	dos.flush();
        } catch (IOException e) {
        	logger.info("Connection object writeString method has error", e);
        }
    }
    
    public void printString(String message) {
        try {
        	PrintWriter pw = new PrintWriter(socket.getOutputStream());
        	pw.write(message);
        	pw.flush();
        } catch (IOException e) {
        	logger.info("Connection object printString method has error", e);
        }
    }
    
    /**
     * 向socket中写入数据
     * 
     * @param message
     */
    public void writeLong(Long data) {
        try {
        	DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        	dos.writeLong(data);
        	dos.flush();
        } catch (IOException e) {
        	logger.info("Connection object writeLong method has error", e);
        }
    }
    
    /**
     * 读取文件并将文件流输出到 socket 的输出流中
     * 
     * @param filePath
     */
    public void writeFile(File file) {
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
        	logger.info("write file length = " + tempLen);
        } catch (IOException e) {
        	logger.info("Connection object writeFile method has error", e);
        } finally {
        	try {
        		dis.close();
			} catch (Exception e2) {
			}
        }
    }

    /**
     * 向socket中写入数据
     * 
     * @param message
     */
    public void println(String message) {
        try {
        	PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            writer.println(message);
            writer.flush();
        } catch (IOException e) {
        	logger.info("Connection object println method has error", e);
        }
    }
    
	public Socket getSocket() {
		return socket;
	}
	
	public OutputStream getSocketOutputStream() throws IOException {
		return socket.getOutputStream();
	}
	
	public InputStream getSocketInputStream() throws IOException {
		return socket.getInputStream();
	}
	
	public static void main(String[] args) {
		System.out.println(System.getProperty("file.encoding"));
		System.out.println(Charset.defaultCharset());
	}
    
}