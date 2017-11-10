package com.ckjava.io.command.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadService {
	
	private ThreadService() {
		new RuntimeException("wrong operation");
	}
	
	/**
	 * 最大同时支持多少个客户端
	 */
	private static final int maxClientSize = 10;
	
	private static ExecutorService handleClientService = Executors.newFixedThreadPool(maxClientSize);
	
	private static ExecutorService executorService = Executors.newCachedThreadPool();
	
	public static ExecutorService getExecutorService() {
		return executorService;
	}
	
	public static ExecutorService getHandleClientService() {
		return handleClientService;
	}
}
