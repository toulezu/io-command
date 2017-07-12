package test.com.ckjava.io.command;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ckjava.io.command.SocketClient;
import com.ckjava.io.command.SocketServer;
import com.ckjava.io.command.constants.IOSigns;

public class TestCommandHandler {

	private static final int port = 19800;
	private static ExecutorService executorservice = Executors.newFixedThreadPool(2);

	@BeforeClass
	public static void startServer() {
		new SocketServer(port);
		System.out.println("Server starts, port is " + port);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void close() {
		executorservice.shutdown();
	}
	
	@Test
	public void testMultiInvoke() {
		List<RunCommandThread> threadList = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			threadList.add(new RunCommandThread());
		}
		try {
			executorservice.invokeAll(threadList);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public class RunCommandThread implements Callable<String> {

		@Override
		public String call() throws Exception {
			try {
				SocketClient client = new SocketClient(InetAddress.getLocalHost(), port);
				client.send(IOSigns.RUN_SYNC_COMMAND_SIGN).send("${cmd /C ipconfig}").setCharset("GBK");
				String sign = client.readUTFString();
				if (sign.equals(IOSigns.FOUND_COMMAND)) {
					while (true) {
						sign = client.readUTFString();
						System.out.println(sign);
						
						if (sign.equals(IOSigns.FINISH_RUN_COMMAND_SIGN)) {
							System.out.println(Thread.currentThread().getName() + " finish");
							client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe(); // 通知服务器端关闭
							break;
						}
					}
				} else {
					System.out.println(IOSigns.NOT_FOUND_COMMAND);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

	}

}