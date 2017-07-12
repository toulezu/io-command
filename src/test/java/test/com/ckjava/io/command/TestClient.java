package test.com.ckjava.io.command;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ckjava.io.command.SocketClient;
import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.utils.CommandUtils;

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
		//String host = "10.2.75.149";
		//String host = "10.32.80.130";
		//String host = "10.32.80.21";
		
		// 1.1停止iis服务
		String command_stop_iis_opencover = "cmd /C call 1_stop_iis_opencover.cmd";
		
		// 1.2使用OpenCover.Console.exe把iis站点启动
		String command_use_opencover_start_iis = "cmd /C call D:/apps/exe/io-command/2_use_opencover_start_iis.cmd";
		
		// 1.3将xml报告转成html
		String command_convert_opencover_xml_2_html = "cmd /C call D:/apps/exe/io-command/3_convert_opencover_xml_2_html.cmd";
		
		// http://10.2.75.149/payment-base-managedservice/managedservice.asmx
		String host = "10.2.75.149";
		String site_name = "Ctrip_test";
		String app_pool_name = "Ctrip";
        String physic_path = "Ctrip/payment-base-managedservice";
		
		ExecutorService executeService = Executors.newCachedThreadPool();
		
		try {
			SocketClient client = new SocketClient(InetAddress.getByName(host), 8083);
			
			// 应用环境配置
			client.send(IOSigns.RUN_SYNC_COMMAND_SIGN);
			client.send("${C:\\Windows\\System32\\inetsrv\\appcmd.exe list apppool}");
			String sign = client.readUTFString();
			boolean isExistPool = false;
    		if (sign.equals(IOSigns.FOUND_COMMAND)) {
    			while (true) {
    				sign = client.readUTFString();
    				System.out.println(sign);
    				if (sign.contains(site_name)) { // 说明存在和网站名一样的应用池
    					isExistPool = true;
    				}
    				if (sign.equals(IOSigns.FINISH_RUN_COMMAND_SIGN)) {
    					System.out.println("列出所有的应用池成功");
    					break;
    				}
				}
    		}
    		if (!isExistPool) { // 新增应用池
    			client.send(IOSigns.RUN_SYNC_COMMAND_SIGN);
    			client.send("${C:\\Windows\\System32\\inetsrv\\appcmd.exe add apppool /name:\""+site_name+"\"}");
    			sign = client.readUTFString();
        		if (sign.equals(IOSigns.FOUND_COMMAND)) {
        			while (true) {
        				sign = client.readUTFString();
        				System.out.println(sign);
        				if (sign.equals(IOSigns.FINISH_RUN_COMMAND_SIGN)) {
        					System.out.println("新增应用池成功");
        					break;
        				}
    				}
        		}
    		}
    		
    		// 将应用对应的应用池修改成和网站名一样的应用池
    		client.send(IOSigns.RUN_SYNC_COMMAND_SIGN);
			client.send("${C:\\Windows\\System32\\inetsrv\\appcmd.exe set app \""+physic_path+"\" /applicationpool:\""+site_name+"\"}");
			sign = client.readUTFString();
    		if (sign.equals(IOSigns.FOUND_COMMAND)) {
    			while (true) {
    				sign = client.readUTFString();
    				System.out.println(sign);
    				if (sign.equals(IOSigns.FINISH_RUN_COMMAND_SIGN)) {
    					System.out.println("将应用对应的应用池修改成和网站名一样的应用池成功");
    					break;
    				}
				}
    		}
			
			// 1,发现并杀死  OpenCover.Console.exe, w3wp.exe 进程
			client.send(IOSigns.RUN_SYNC_COMMAND_SIGN);
			client.send("${"+command_stop_iis_opencover+"}");
			sign = client.readUTFString();
			if (sign.equals(IOSigns.FOUND_COMMAND)) {
				while (true) {
    				sign = client.readUTFString();
    				System.out.println(sign);
    				if (sign.equals(IOSigns.FINISH_RUN_COMMAND_SIGN)) {
    					System.out.println("完成发现并杀死  OpenCover.Console.exe, w3wp.exe 进程");
    					break;
    				}
				}
			}
			
			// 2.生成coverage.xml
			client.send(IOSigns.RUN_ASYNC_COMMAND_SIGN);
			client.send("${"+command_use_opencover_start_iis+"}");
			sign = client.readUTFString();
    		if (sign.equals(IOSigns.FOUND_COMMAND)) {
    			while (true) {
    				sign = client.readUTFString();
    				System.out.println(sign);
    				if (sign.contains("Successfully opened log file")) { // 表示可以发送Q了
    					System.out.println("API请求了应用");
    					// 发送Q通知服务器关闭Opencover并生成覆盖率报告
    					Future<?> result = executeService.submit(new SendFinishSignThread(client));
    					try {
							result.get();
							System.out.println("发送Q成功");
						} catch (Exception e) {
							System.out.println("发送Q通知服务器关闭Opencover并生成覆盖率报告出现异常");
						}
    				}
    				if (sign.contains("Executing: C:\\Windows\\System32\\inetsrv\\w3wp.exe")) { //　表示可以开始请求API了
    					Future<?> result = executeService.submit(new ExecuteAPIThread());
    					try {
							result.get();
							System.out.println("执行API成功");
						} catch (Exception e) {
							System.out.println("执行API出现异常");
						}
    				}
    				if (sign.equals(IOSigns.FINISH_RUN_ASYNC_COMMAND_SIGN)) {
    					System.out.println("完成生成coverage.xml");
    					break;
    				}
				}
    		}
    		
    		// 3.将coverage.xml转成html报告
    		client.send(IOSigns.RUN_SYNC_COMMAND_SIGN);
			client.send("${"+command_convert_opencover_xml_2_html+"}");
			sign = client.readUTFString();
    		if (sign.equals(IOSigns.FOUND_COMMAND)) {
    			while (true) {
    				sign = client.readUTFString();
    				System.out.println(sign);
    				if (sign.equals(IOSigns.FINISH_RUN_COMMAND_SIGN)) {
    					System.out.println("完成将coverage.xml转成html报告");
    					break;
    				}
				}
    		}
    		// 4.将summary.htm回传到本地
    		String path = "H:/io-command-files/";
    		client.send(IOSigns.READ_FILE_SIGN);
    		client.send("${D:/WebSites/test/report/summary.htm}");
    		sign = client.readUTFString();
    		if (sign.equals(IOSigns.FOUND_COMMAND)) {
    			sign = client.readUTFString();
    			if (sign.equals(IOSigns.FOUND_FILE_SIGN)) {
    				String fileName = client.readUTFString();
    				Long fileSize = client.readLong();
    				String localFile = path+fileName;
    				String result = client.getFileFromServer(localFile, fileSize);
    				if (result == null) {
    					System.out.println("文件传输完毕, path = " + localFile + ", size = " + fileSize + " byte");
    				} else {
    					System.err.println(result);
    				}
    			}
    		}
    		
    		// 将应用对应的应用池恢复到原来的样子
    		client.send(IOSigns.RUN_SYNC_COMMAND_SIGN);
			client.send("${C:\\Windows\\System32\\inetsrv\\appcmd.exe set app \""+physic_path+"\" /applicationpool:\""+app_pool_name+"\"}");
			sign = client.readUTFString();
    		if (sign.equals(IOSigns.FOUND_COMMAND)) {
    			while (true) {
    				sign = client.readUTFString();
    				System.out.println(sign);
    				if (sign.equals(IOSigns.FINISH_RUN_COMMAND_SIGN)) {
    					System.out.println("将应用对应的应用池修改成和网站名一样的应用池成功");
    					break;
    				}
				}
    		}
    		
    		// 通知服务器端关闭 socket
    		client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe(); 
			System.out.println("关闭socket连接成功");
			
			// 5.解析覆盖行数 
			// 从文件中加载 HTML 文档
			File input = new File("H:/io-command-files/summary.htm"); 
			try {
				Document doc = Jsoup.parse(input,"UTF-8");
				Element table = doc.select("table").first();
				
				Elements elements = table.select("tbody tr");
				for (Element element : elements) {
					System.out.print(element.select("th").first().text() + ":");
					System.out.println(element.select("td").first().text());
				}
				
				String coverageLine = table.select("tbody tr:eq(7) td").text();
				System.out.println("覆盖行数为:" + coverageLine);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
	
	//@Test
	public void getCoverageLine() {
		// 从文件中加载 HTML 文档
		File input = new File("H:/io-command-files/summary.htm"); 
		try {
			Document doc = Jsoup.parse(input,"UTF-8");
			Element table = doc.select("table").first();
			
			Elements elements = table.select("tbody tr");
			for (Element element : elements) {
				System.out.print(element.select("th").first().text());
				System.out.println(element.select("td").first().text());
			}
			
			String coverageLine = table.select("tbody tr:eq(7) td").text();
			System.out.println("覆盖行数为:" + coverageLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//@Test
	public void findAndCloseProcess() {
		String[] killProcess = {"python.exe", "TaobaoProtect.exe"};
		StringBuffer tasks = new StringBuffer();
		StringBuffer killResult = new StringBuffer();
		CommandUtils.execTask("cmd.exe /C tasklist", "GBK", tasks);
		String[] taskArr = tasks.toString().split("\n");
		for (String taskname : taskArr) {
			for (String tokill : killProcess) {
				if (taskname.contains(tokill)) {
					CommandUtils.execTask("cmd.exe /C taskkill /F /im "+tokill, "GBK", killResult);
					System.out.println(taskname);
				}
			}
		}
		System.out.println(killResult.toString());
	}
	
	public static class ExecuteAPIThread implements Runnable {

		@Override
		public void run() {
			// 模拟API请求消耗的时间
    		try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 告诉OpenCover可以生成请求报告了,启动机器人按Q退出
	public static class SendFinishSignThread implements Runnable {

		private SocketClient client;
		
		public SendFinishSignThread(SocketClient client) {
			super();
			this.client = client;
		}

		@Override
		public void run() {
    		while (true) {
    			// 通知可以启动robot按Q退出
	    		client.send(IOSigns.WRITE_FILE_SIGN);
				client.send("${start_robot}");
				String sign = client.readUTFString();
				if (sign.equals(IOSigns.FOUND_COMMAND)) {
					sign = client.readUTFString();
					if (sign.equals(IOSigns.WRITE_FILE_SUCCESS)) {
						System.out.println("robot push Q");
						break;
					}
				}
    		}
    		
		}
		
	}
	
}
