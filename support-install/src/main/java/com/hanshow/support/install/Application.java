package com.hanshow.support.install;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	
	private static final String INSTALL = "install", UNINSTALL = "uninstall", START = "start", STOP = "stop";
	
	public static void main(String[] args) {
		if (args.length <=0) {
			System.out.println("Please input args");
			System.exit(0);
		}
		switch(args[0]) {
		case INSTALL:
			// 安装
			new Install().exec();
			break;
		case UNINSTALL:
			// 卸载
			new Uninstall().exec();
			break;
		case START:
			new StartService().exec();
			break;
		case STOP:
			new StopService().exec();
			break;
			default:
				System.out.println("Undefined command");
				System.exit(0);
				break;
				
		}
	}
}