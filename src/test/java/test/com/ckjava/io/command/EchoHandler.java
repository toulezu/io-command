package test.com.ckjava.io.command;

import com.ckjava.io.command.Connection;
import com.ckjava.io.command.MessageHandler;

public class EchoHandler implements MessageHandler {
	
	/**
	 * 接收消息后向socket中写入消息 message
	 */
    @Override
    public void onReceive(Connection connection, String message) {
        connection.println("server echo:" + message);
    }
}