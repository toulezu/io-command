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

import com.ckjava.io.command.client.SocketClient;
import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.io.command.server.SocketServer;
import com.ckjava.utils.OSUtils;

public class TestCommandHandler {

	private static final int port = 19800;
	private static ExecutorService executorservice = Executors.newFixedThreadPool(2);

	@BeforeClass
	public static void startServer() {
		new SocketServer(port);
		System.out.println("Server starts, port is " + port);
	}
	
	@AfterClass
	public static void close() {
		executorservice.shutdown();
	}
	
	@Test
	public void testMultiInvoke() {
		List<RunCommandThread> threadList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
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
				String osType = OSUtils.getCurrentOSType();
				String command = "ifconfig";
				if (osType.equals(OSUtils.WINDOWS)) {
					command = "ipconfig;c:/";
				}
				command = command.concat(";charset=GBK");
				
				String result = client.send(IOSigns.RUN_COMMAND_SIGN).send("${"+command+"}").getRunCommandResult(client);
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

	}

}