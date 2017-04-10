# 远程在服务器端执行命令的小工具

## 简单说明

- 基于 Java socket 编写,最终打成一个可执行的jar,指定一个未占用的端口即可

- 每启动一个 SocketClient 服务器端会启动一个 ConnectionThread 进行处理

- 在 SocketClient 中通过 `send("${command}")` 发送命令, 通过 `getResult("GBK")` 获取命令的输出结果, 命令执行完毕后自动关闭 socket 连接

## 服务器端使用

- 启动如下命令即可

```
java -jar io-command.jar -p 8083
```

- 也可以通过如下方式启动

```
new SocketServer(8083, new CommandHandler());
```

## 客户端使用

```java
try {
	SocketClient client = new SocketClient(InetAddress.getByName("192.168.2.149"), 8083);
	client.send("${ipconfig}");
	String result = client.getResult("GBK");
	System.out.println(result);
} catch (UnknownHostException e) {
	e.printStackTrace();
}
```