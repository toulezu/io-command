package test.com.ckjava.io.command;

import java.net.InetAddress;

import com.ckjava.io.command.client.SocketClient;
import com.ckjava.io.command.constants.IOSigns;
import com.ckjava.utils.ArrayUtils;

public class TestGitCommand {
	public static void main(String[] args) {
		try {
			SocketClient client = new SocketClient(InetAddress.getByName("10.3.6.168"), 16800);
			String command = "git show 323eff2686e8c38beb49deb324bf103a3dd56e67;/var/repo/2/";
			command = command.concat(";charset=UTF-8");
			
			String result = client.send(IOSigns.RUN_COMMAND_SIGN).send("${"+command+"}").getRunCommandResult(client);
			//System.out.println(result);
			String[] results = result.split("\n");
			
			int updateFile = 0;
			int addFile = 0;
			int deleteFile = 0;
			int addLine = 0;
			int deleteLine = 0;

			for (int i = 0, c = ArrayUtils.getSize(results); i < c; i++) {
				String lineStr = ArrayUtils.getValue(results, i);
				if (lineStr.startsWith("diff")) {
					String lineStr2 = ArrayUtils.getValue(results, i+1);	
					if (lineStr2.startsWith("index")) {
						updateFile ++;
						i += 3;
					}
					if (lineStr2.startsWith("deleted")) {
						deleteFile ++;
						i += 4; 
					}
					if (lineStr2.startsWith("new")) {
						addFile ++;
						i += 4;
					}
				}
				if (lineStr.startsWith("+")) {
					addLine ++;
				}
				if (lineStr.startsWith("-")) {
					deleteLine ++;
				}
			}
			System.out.println("updateFile = " + updateFile);
			System.out.println("addFile = " + addFile);
			System.out.println("deleteFile = " + deleteFile);
			System.out.println("addLine = " + addLine);
			System.out.println("deleteLine = " + deleteLine);
			String analysisResult = "修改文件数:"+updateFile+", 新增文件数:"+addFile+", 删除文件数:"+deleteFile+", 新增代码行数:"+addLine+", 删除代码行数:"+deleteLine;
			System.out.println(analysisResult);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
