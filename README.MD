[![Build Status](https://travis-ci.org/toulezu/io-command.svg?branch=master)](https://travis-ci.org/toulezu/io-command)

# 基于 Java Socket 技术实现的客户端与服务器端交互的中间件

## 简单说明

- 基于 Java socket
- 完全基于事件处理, 服务器端支持阻塞和非阻塞操作
- 客户端基于事件来处理指令的服务器端返回的结果
- 服务器端基于接口和命名约束来处理客户端发送过来的指令, 以便客户端可以动态扩展新的 Handler(编写后上传到服务器端)

## 具体使用

- 引入其他项目后在代码中直接启动

```java
new SocketServer(19800);
```

- 或者打成单独的 jar 后通过命令来启动

## 目前支持的功能

#### 客户端在服务器端执行命令

示例代码如下

```java
SocketClient client = new SocketClient(InetAddress.getLocalHost(), port);
String osType = OSUtils.getCurrentOSType();
String command = "ifconfig";
if (osType.equals(OSUtils.WINDOWS)) {
	command = "ipconfig;c:/";
}
command = command.concat(";charset=GBK");

client.send(IOSigns.RUN_COMMAND_SIGN).send("${"+command+"}").onReceiveCommandListener(new DefaultOnReceiveCommandListenerImpl(client));
```

客户端的发送的第一条指令为 `Command`, 根据命名规则服务器端对应的 Handler 为 [CommandHandler](https://github.com/toulezu/io-command/blob/master/src/main/java/com/ckjava/io/command/server/handler/impl/CommandHandler.java)

第二条指令为 `${ipconfig;c:/;charset=GBK}`, 其中以 `;` 分隔. 共分为三个部分, 第一部分表示指令的具体内容, 第二部分表示指令具体在目标系统哪个目录下执行, 第三部分表示目标系统的系统编码

在服务端开始处理指令后,系统提供的客户端默认事件处理类如下

```java
public class DefaultOnReceiveCommandListenerImpl implements OnReceiveCommandListener {

	private SocketClient client;
	
	private StringBuilder result = new StringBuilder();
	
	public DefaultOnReceiveCommandListenerImpl(SocketClient client) {
		super();
		this.client = client;
	}

	
	@Override
	public void onStartExecuteCommand(String sign) {
		System.out.println("onStartExecuteCommand = " + sign);
	}

	@Override
	public void onExecuteCommand(String sign) {
		result.append(sign).append(StringUtils.LF);
	}

	@Override
	public void onFinishExecuteCommand() {
		client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe();
	}

}
```

当然也可以自己实现 [OnReceiveCommandListener](https://github.com/toulezu/io-command/blob/e8accec457da17dc1c8ab6c0b75b1f3a9fbf266e/src/main/java/com/ckjava/io/command/client/listener/OnReceiveCommandListener.java) 接口来编写自己的事件处理逻辑.

#### 客户端向服务器端传输文件

示例代码如下

```java
// send file to server
String localFilePath = TestFileHandler.class.getResource("/local-file/verifycode.jpg").getPath();
String remoteFilePath = TestFileHandler.class.getResource("/remote-file/").getPath();
File localFile = new File(localFilePath);

SocketClient client = new SocketClient(InetAddress.getLocalHost(), port);
client.send(IOSigns.WRITE_FILE_SIGN).send("${"+remoteFilePath+","+ localFile.getName() +","+localFile.length()+"}")
		.onSendFileToServerListener(new DefaultOnSendFileToServerListenerImpl(client, localFile));
```

其中客户端的发送的第一条指令为 `SendFileToServer`, 根据命名规则服务器端对应的 Handler 为 [SendFileToServerHandler](https://github.com/toulezu/io-command/blob/master/src/main/java/com/ckjava/io/command/server/handler/impl/SendFileToServerHandler.java). 有一点需要注意的是该 Handler 实现了 BlockAble 接口, 表示服务端在处理该指令的时候会以阻塞的方式进行, 也就是说在当前文件没有传输成功前无法执行其他命令.

第二条指令为 `${remoteFilePath,localFileName,localFileSize}`, 其中以 `,` 分隔. 共分为三个部分, 第一部分表示上传到服务器端的文件目录, 第二部分表示本地文件名称, 本地文件大小

在服务端开始处理指令后,系统提供的客户端默认事件处理类如下

```java
public class DefaultOnSendFileToServerListenerImpl implements OnSendFileToServerListener {
	
	private Logger logger = LoggerFactory.getLogger(DefaultOnSendFileToServerListenerImpl.class);

	private SocketClient client;
	private File localFile;

	public DefaultOnSendFileToServerListenerImpl(SocketClient client, File localFile) {
		super();
		this.client = client;
		this.localFile = localFile;
	}

	@Override
	public void onStartSendFile(String sign) {
		logger.info("onStartSendFile, sign: {}", sign);
	}

	@Override
	public File onSendFile() {
		return localFile;
	}

	@Override
	public void onFailSendFile() {
		logger.error("fail send file to server");
	}

	@Override
	public void onFinishSendFile(String clientResult, String serverResult) {
		logger.info("finish send file to server, clientResult: {}, serverResult = {}", clientResult, serverResult);
		
		client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe();
	}

}
```

### 客户端获取服务器端文件

示例代码如下

```java
// get file from server
String remoteFile = TestFileHandler.class.getResource("/remote-file/testReadFile.txt").getPath();

String localPath = TestFileHandler.class.getResource("/local-file/").getPath();

SocketClient client = new SocketClient(InetAddress.getLocalHost(), port);

client.send(IOSigns.READ_FILE_SIGN).send("${"+remoteFile+"}").onGetFileFromServerListener(new DefaultOnGetFileFromServerListenerImpl(client, localPath));
```

其中客户端的发送的第一条指令为 `GetFileFromServer`, 根据命名规则服务器端对应的 Handler 为 [GetFileFromServerHandler](https://github.com/toulezu/io-command/blob/master/src/main/java/com/ckjava/io/command/server/handler/impl/GetFileFromServerHandler.java).

第二条指令为 `${remoteFile}`, 表示服务器端单个文件的完整路径.

在服务端开始处理指令后,系统提供的客户端默认事件处理类如下

```java
public class DefaultOnGetFileFromServerListenerImpl implements OnGetFileFromServerListener {
	
	private Logger logger = LoggerFactory.getLogger(DefaultOnGetFileFromServerListenerImpl.class);

	private SocketClient client;
	private String localPath;
	
	public DefaultOnGetFileFromServerListenerImpl(SocketClient client, String localPath) {
		super();
		this.client = client;
		this.localPath = localPath;
	}

	@Override
	public void onStartGetFile(String sign) {
		logger.info("onStartGetFile, sign = {} ", sign);
	}

	@Override
	public String onFindFile(String fileName, Long fileSize) {
		logger.info("find server file, fileName is {}, size is {} ", fileName, fileSize);
		return localPath + fileName;
	}

	@Override
	public void onNotFindFile() {
		logger.info("not find server file ");
	}

	@Override
	public void onFinishSaveFile(String saveResult) {
		logger.info("finish save file from server, result: {} ", saveResult);
		client.send(IOSigns.CLOSE_SERVER_SIGN).closeMe();
	}

}
```

## TODO LIST

- 支持SSL/TLS加密
- 以插件的方式扩展Handler
- 文件分段多线程传输
- 定义相关接口，让client的class类可以在服务器端可以执行
- 增加客户端权限校验功能
- 可编程,方便扩展
- 文件传输增加 hash 校验
- 可随 tomcat 自动启动
- 支持脚本, 编写相关脚本后系统读取后根据配置自行执行脚本中定义的命令或者配置