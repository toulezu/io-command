package test.com.ckjava.io.command;

import com.ckjava.io.command.SocketServer;

/**
 * 
 * @author chen_k
 *
 * 2017年4月7日-下午7:22:54
 */
public class TestServer {
	public static void main(String[] args) {
		
		int port = 9800;
		new SocketServer(port);
        System.out.println("Server starts, port is " + port);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
	}

}
