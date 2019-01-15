package com.hanshow.support.install;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.hanshow.support.util.FileUtils;
import com.hanshow.support.util.SystemCmdManager;

public class Base {

	protected String JAVA_HOME = "";
	protected String MYSQL_HOME = "";
	protected String TOMCAT_HOME = "";
	protected String ESLWORKING_HOME = "";
	protected String INTEGRATION_HOME = "";
	protected String CONFIG_DIR = "";
	protected String BIN_DIR = "";
	protected String DATA_DIR = "";
	protected static final String MYSQL_SERVICE_NAME = "Hanshow-MySQL";
	protected static final String TOMCAT_SERVICE_NAME = "Hanshow-Tomcat";
	protected static final String ESLWORKING_SERVICE_NAME = "ESL-Working";
	protected static String INTEGRATION_SERVICE_NAME = "Hanshow-Integration";
	protected String SERVICE_NAME_FILE = "service.properties";
	protected static final Short TIMEOUT = 300; // 秒
	protected static final String BAK_SUFFIX = ".bak";
	static final String TOMCAT_SERVICE_KEY = "tomcat";
	static final String MYSQL_SERVICE_KEY = "mysql";
	static final String ESLWORKING_SERVICE_KEY = "eslworking";
	static final String INTEGRATION_SERVICE_KEY = "integration";

	/**
	 * 获取所有软件的根目录
	 */
	protected void getFileDir() {
		// 获取应该的根目录
		File dir = new File(System.getProperty("user.dir")).getParentFile();
		for (File file : dir.listFiles()) {
			if (file.getName().toLowerCase().startsWith("jdk")) {
				JAVA_HOME = new File(dir, file.getName()).getPath();
			} else if (file.getName().toLowerCase().startsWith("mysql")) {
				MYSQL_HOME = new File(dir, file.getName()).getPath();
			} else if (file.getName().toLowerCase().startsWith("tomcat")) {
				TOMCAT_HOME = new File(dir, file.getName()).getPath();
			} else if (file.getName().toLowerCase().startsWith("eslworking")) {
				ESLWORKING_HOME = new File(dir, file.getName()).getPath();
			} else if (file.getName().toLowerCase().startsWith("integration")) {
				INTEGRATION_HOME = new File(dir, file.getName()).getPath();
			} else if (file.getName().toLowerCase().equals("config")) {
				CONFIG_DIR = new File(dir, file.getName()).getPath();
			} else if (file.getName().toLowerCase().equals("data")) {
				DATA_DIR = new File(dir, file.getName()).getPath();
			} else if (file.getName().toLowerCase().equals("bin")) {
				BIN_DIR = new File(dir, file.getName()).getPath();
			}
		}
	}

	protected void startService() {
		// 读取服务名,并开启所有服务
		Map<String, String> map = null;
		try {
			map = FileUtils.readProperties(new File(BIN_DIR, SERVICE_NAME_FILE));
		} catch (IOException e) {
			System.err.println(e);
		}
		// 开启所有服务
		for (String serviceName : map.values()) {
			try {
				new SystemCmdManager().start(serviceName, TIMEOUT);
				System.out.println("Start " + serviceName + " service successed!!!");
			} catch (IOException | InterruptedException | TimeoutException e) {
				System.err.println("Start " + serviceName + " service failed!!!");
				System.err.println(e);
			}
		}
	}

	protected void stopService() {
		// 读取服务名,并关闭所有服务
		Map<String, String> map = null;
		try {
			map = FileUtils.readProperties(new File(BIN_DIR, SERVICE_NAME_FILE));
		} catch (IOException e) {
			System.err.println(e);
		}
		// 关闭所有服务
		for (String serviceName : map.values()) {
			try {
				System.out.println("Stop " + serviceName + " service successed!!!");
				new SystemCmdManager().stop(serviceName, TIMEOUT);
			} catch (IOException | InterruptedException | TimeoutException e) {
				System.err.println("Stop " + serviceName + " service failed!!!");
				System.err.println(e);
			}
		}
	}
}
