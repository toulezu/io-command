package test.com.ckjava.io.command;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.ckjava.io.command.SocketClient;

/**
 * client 必须从 socket中读取信息,否则 getInputStream 会将本地缓冲区写满导致程序异常
 * 读取完毕后必须关闭,同时通知server 端也关闭socket
 * 
 * client 
 * 
 * @author chen_k
 *
 * 2017年4月7日-下午7:15:45
 */
public class TestClient {
	public static void main(String[] args) {
		
		SocketClient client;
		try {
			client = new SocketClient(InetAddress.getByName("10.2.75.149"), 8083);
			client.send("${ipconfig}");
			String result = client.getResult("GBK");
			System.out.println(result);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}

}
