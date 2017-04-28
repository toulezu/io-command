package com.ckjava.io.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ckjava.utils.ArrayUtils;

public class AppMain {

	private static Logger logger = LoggerFactory.getLogger(AppMain.class);
	
	public static void main(String[] args) {
		
		logger.info("args.length = " + args.length);
		// 用例文件
		String pOption = ArrayUtils.getValue(args, 0);
		Integer port = Integer.parseInt(ArrayUtils.getValue(args, 1));
		if (!pOption.equals("-p")) {
			logger.warn("want option -p, unkown option " + pOption);
			System.exit(0);
		}
		new SocketServer(port);
		
		logger.info("server start, port is " + port);
	}
}
