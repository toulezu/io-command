package com.ckjava.io.command.server.handler;

/**
 * 实现该接口的 Handler 将会导致服务器端不会接受新的命令
 * 比如客户端在向服务器端发送文件的时候服务器就不能接受新的命令,直到文件传输完毕
 * 
 * @author chen_k
 *
 */
public interface BlockAble {

}
