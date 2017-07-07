package com.ckjava.io.command.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadService {
	
	private ThreadService() {
		new RuntimeException("wrong operation");
	}
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public static ExecutorService getExecutorService() {
		return executorService;
	}
}
