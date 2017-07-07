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

public class TestFileHandler {

	private static final int port = 19800;
	private static ExecutorService executorservice = Executors.newFixedThreadPool(1);

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
		List<RunFileThread> threadList = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			threadList.add(new RunFileThread());
		}
		try {
			executorservice.invokeAll(threadList);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public class RunFileThread implements Callable<String> {

		@Override
		public String call() throws Exception {
			try {
				String remoteFile = TestFileHandler.class.getResource("/remote-file/testReadFile.txt").getPath();
				
				String localPath = TestFileHandler.class.getResource("/local-file/").getPath();
				SocketClient client = new SocketClient(InetAddress.getLocalHost(), port);
				client.send(IOSigns.READ_FILE_SIGN);
				client.send("${"+remoteFile+"}");
				String sign = client.readUTFString();
				if (sign.equals(IOSigns.FOUND_COMMAND)) {
					sign = client.readUTFString();
					if (sign.equals(IOSigns.FOUND_FILE_SIGN)) {
						String fileName = client.readUTFString();
						Long fileSize = client.readLong();
						String localFile = localPath + fileName;
						String result = client.readFile(localFile, fileSize);
						if (result == null) {
							System.out.println("文件传输完毕, path = " + localFile + ", size = " + fileSize + " byte");
							client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe(); // 通知服务器端关闭
						} else {
							System.err.println(result);
						}
					} else {
						System.out.println(IOSigns.NOT_FOUND_FILE_SIGN);
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
