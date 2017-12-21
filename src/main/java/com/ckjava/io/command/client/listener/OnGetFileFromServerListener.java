package com.ckjava.io.command.client.listener;

/**
 * 发送获取服务器端的文件指令后的回调事件
 * 
 * @author chen_k
 *
 */
public interface OnGetFileFromServerListener {
	
	/**
	 * 开始处理指令
	 * 
	 * @param sign
	 */
	public void onStartGetFile(String sign);
	
	/**
	 * 发现服务器端的文件, 并且要返回保存到本地的文件完整地址
	 * 
	 * @param fileName 服务器端的文件名
	 * @param fileSize 服务器端的文件大小
	 * 
	 * @return 返回保存到本地的文件完整地址
	 */
	public String onFindFile(String fileName, Long fileSize);
	
	/**
	 * 没有发现服务器端的文件
	 * 
	 */
	public void onNotFindFile();
	
	/**
	 * 文件保存到本地成功后
	 * 
	 * @param saveResult
	 */
	public void onFinishSaveFile(String saveResult);
}
