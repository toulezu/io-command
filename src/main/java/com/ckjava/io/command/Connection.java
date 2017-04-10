package com.ckjava.io.command;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {
    private Socket socket;

    public Connection(Socket socket) {
        this.socket = socket;
    }

    /**
     * 向socket中写入数据
     * 
     * @param message
     */
    public void println(String message) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            writer.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public Socket getSocket() {
		return socket;
	}
    
    
}