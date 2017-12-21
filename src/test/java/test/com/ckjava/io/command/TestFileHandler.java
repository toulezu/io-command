package test.com.ckjava.io.command;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.io.command.client.SocketClient;
import com.ckjava.io.command.client.listener.impl.DefaultOnGetFileFromServerListenerImpl;
import com.ckjava.io.command.client.listener.impl.DefaultOnSendFileToServerListenerImpl;
import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.io.command.server.SocketServer;

public class TestFileHandler {
	
	private static Logger logger = LoggerFactory.getLogger(TestFileHandler.class);

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
	public void testGetFileFromServer() {
		List<GetFileFromServerThread> threadList = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			threadList.add(new GetFileFromServerThread());
		}
		try {
			executorservice.invokeAll(threadList);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSendFileToServer() {
		List<SendFileToServerThread> threadList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			threadList.add(new SendFileToServerThread());
		}
		try {
			executorservice.invokeAll(threadList);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public class GetFileFromServerThread implements Callable<String> {

		@Override
		public String call() throws Exception {
			try {
				// get file from server
				String remoteFile = TestFileHandler.class.getResource("/remote-file/testReadFile.txt").getPath();
				
				String localPath = TestFileHandler.class.getResource("/local-file/").getPath();
				
				SocketClient client = new SocketClient(InetAddress.getLocalHost(), port);
				//String result = client.send(IOSigns.READ_FILE_SIGN).send("${"+remoteFile+"}").getGetFileFromServerResult(client, localPath);
				
				client.send(IOSigns.READ_FILE_SIGN).send("${"+remoteFile+"}").onGetFileFromServerListener(new DefaultOnGetFileFromServerListenerImpl(client, localPath));
				
			} catch (Exception e) {
				logger.error("GetFileFromServer has error", e);
			}
			return null;
		}

	}
	
	public class SendFileToServerThread implements Callable<String> {

		@Override
		public String call() throws Exception {
			try {
				// send file to server
				String localFilePath = TestFileHandler.class.getResource("/local-file/verifycode.jpg").getPath();
				String remoteFilePath = TestFileHandler.class.getResource("/remote-file/").getPath();
				File localFile = new File(localFilePath);
				
				SocketClient client = new SocketClient(InetAddress.getLocalHost(), port);
				//String result = client.send(IOSigns.WRITE_FILE_SIGN).send("${"+remoteFilePath+","+ localFile.getName() +","+localFile.length()+"}").getSendFileToServerResult(client, localFile);
				
				client.send(IOSigns.WRITE_FILE_SIGN).send("${"+remoteFilePath+","+ localFile.getName() +","+localFile.length()+"}")
						.onSendFileToServerListener(new DefaultOnSendFileToServerListenerImpl(client, localFile));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}
