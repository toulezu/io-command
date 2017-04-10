package test.com.ckjava.io.command;

import java.io.IOException;
import java.net.InetAddress;

import com.ckjava.io.command.SocketClient;
import com.ckjava.io.command.SocketServer;

public class SocketExample {
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        SocketServer server = new SocketServer(5556, new EchoHandler());
        System.out.println("Server starts.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        

        SocketClient client = new SocketClient(InetAddress.getLocalHost(), 5556);
        client.send("Hello!");
        
        System.out.println(client.getResult("GBK"));
        
        client.close();
        server.close();
    }
}