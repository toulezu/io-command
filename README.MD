[![Build Status](https://travis-ci.org/toulezu/io-command.svg?branch=master)](https://travis-ci.org/toulezu/io-command)

# 基于 Java Socket 技术实现的客户端与服务器端交互的中间件

## 简单说明

- 基于 Java socket 编写,最终打成一个可执行的jar,指定一个未占用的端口即可

- 每启动一个 SocketClient 服务器端会启动一个 ConnectionThread 进行处理

## 基本功能

- 服务器端启动

```java
new SocketServer(19800);
```

### 客户端在服务器端执行命令

```java
try {
	SocketClient client = new SocketClient(InetAddress.getLocalHost(), port);
	String osType = OSUtils.getCurrentOSType();
	String command = "ifconfig";
	if (osType.equals(OSUtils.WINDOWS)) {
		command = "ipconfig";
	}
	command = command.concat(";charset=GBK");
	
	String result = client.send(IOSigns.RUN_COMMAND_SIGN).send("${"+command+"}").getRunCommandResult(client);
	System.out.println(result);
} catch (Exception e) {
	e.printStackTrace();
}
```

### 客户端向服务器端传输文件

```java
try {
	// send file to server
	String localFilePath = TestFileHandler.class.getResource("/local-file/verifycode.jpg").getPath();
	String remoteFilePath = TestFileHandler.class.getResource("/remote-file/").getPath();
	File localFile = new File(localFilePath);
	
	SocketClient client = new SocketClient(InetAddress.getLocalHost(), port);
	String result = client.send(IOSigns.WRITE_FILE_SIGN).send("${"+remoteFilePath+","+ localFile.getName() +","+localFile.length()+"}").getSendFileToServerResult(client, localFile);
	
	System.out.println(result);
} catch (Exception e) {
	e.printStackTrace();
}
```

### 客户端获取服务器端文件

```java
try {
	// get file from server
	String remoteFile = TestFileHandler.class.getResource("/remote-file/testReadFile.txt").getPath();
	
	String localPath = TestFileHandler.class.getResource("/local-file/").getPath();
	
	SocketClient client = new SocketClient(InetAddress.getLocalHost(), port);
	String result = client.send(IOSigns.READ_FILE_SIGN).send("${"+remoteFile+"}").getGetFileFromServerResult(client, localPath);
	
	System.out.println(result);
} catch (Exception e) {
	logger.error("GetFileFromServer has error", e);
}
```

## TODO LIST

- 支持SSL/TLS加密
- 以插件的方式扩展Handler
- 文件分段多线程传输
- 定义相关接口，让client的class类可以在服务器端可以执行
- 增加客户端权限校验功能
- 基于事件(服务器端和客户端)
- 可编程,方便扩展
- 文件传输增加 hash 校验
- 可随 tomcat 自动启动