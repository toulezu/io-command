package com.ckjava.io.command.client.listener;

/**
 * 接受到客户端发送的指令后基于事件对服务器端的响应进行处理
 * @author chen_k
 *
 * @param <T>
 */
public interface OnReceiveCommandListener {
	
	/**
	 * 开始处理指令
	 * 
	 * @param sign
	 */
	public void onStartExecuteCommand(String sign);
	
	/**
	 * 处理指令中
	 * 
	 * @param result
	 */
	public void onExecuteCommand(String result);
	
	/**
	 * 结束处理指令
	 * 
	 * @param t
	 */
	public void onFinishExecuteCommand();
}
