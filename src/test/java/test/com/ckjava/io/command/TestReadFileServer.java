package test.com.ckjava.io.command;

import com.ckjava.io.command.SocketServer;

/**
 * 
 * @author chen_k
 *
 * 2017年4月7日-下午7:22:54
 */
public class TestReadFileServer {
	public static void main(String[] args) {
		
		new SocketServer(9001);
        System.out.println("Server starts.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
	}

}
