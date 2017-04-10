package test.com.ckjava.io.command;

import com.ckjava.io.command.CommandHandler;
import com.ckjava.io.command.SocketServer;

/**
 * 
 * @author chen_k
 *
 * 2017年4月7日-下午7:22:54
 */
public class TestServer {
	public static void main(String[] args) {
		
		new SocketServer(5556, new CommandHandler());
        System.out.println("Server starts.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
	}

}
