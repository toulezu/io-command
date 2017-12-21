package com.ckjava.io.command.client.listener;

import java.io.File;

/**
 * 向服务器端发送文件后的回调事件
 * 
 * @author chen_k
 *
 */
public interface OnSendFileToServerListener {
	
	/**
	 * 开始处理指令
	 * 
	 * @param sign
	 */
	public void onStartSendFile(String sign);
	
	/**
	 * 将本地的文件传输到服务器端
	 * 
	 * @return 本地文件地址
	 */
	public File onSendFile();
	
	/**
	 * 没有发现服务器端的文件
	 * 
	 */
	public void onFailSendFile();
	
	/**
	 * 成功将文件传输到服务器后
	 * 
	 * @param clientResult 客户端传输结果
	 * @param serverResult 服务端接受结果
	 */
	public void onFinishSendFile(String clientResult, String serverResult);
}
