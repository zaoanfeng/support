package com.hanshow.support.install;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.hanshow.support.util.FileUtils;
import com.hanshow.support.util.SystemCmdManager;

public class Uninstall extends Base {

	public void exec() {
		// 获取所有软件目录
		getFileDir();
		// 读取服务名,并关闭所有服务
		stopService();
		//TODO 卸载服务
		Map<String, String> map = null;
		try {
			map = FileUtils.readProperties(new File(BIN_DIR, SERVICE_NAME_FILE));
		} catch (IOException e) {
			System.err.println(e);
		}
		// 卸载 Integration
		try {	
			uninstallIntegration(map.getOrDefault("integration", MYSQL_SERVICE_NAME));
			System.out.println("Uninstall Integration service finished!!!");
		} catch (IOException | InterruptedException | TimeoutException e) {
			System.err.println("Uninstall Integration service failed!!!");
		}
		//卸载Tomcat
		try {
			new SystemCmdManager().uninstall(TOMCAT_HOME + File.separator + "bin" + File.separator + "service.bat", null, map.getOrDefault("tomcat", TOMCAT_SERVICE_NAME), TIMEOUT);
			System.out.println("Uninstall Tomcat service finished!!!");
		} catch (IOException | InterruptedException | TimeoutException e) {
			System.err.println("Uninstall Tomcat service failed!!!");
		}
		//卸载MySQL
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(MYSQL_HOME + File.separator + "bin").append(File.separator).append("mysqld.exe").append(" ");
			sb.append("--remove").append(" ").append(map.getOrDefault("mysql", MYSQL_SERVICE_NAME));
			new SystemCmdManager().shell(sb.toString(), TIMEOUT);
			System.out.println("Uninstall MySQL service finished!!!");
		} catch (IOException | InterruptedException | TimeoutException e) {
			System.err.println("Uninstall MySQL service failed!!!");
		}
		//卸载ESL-Working
		try {
			new SystemCmdManager().uninstall(ESLWORKING_HOME + File.separator + "bin" + File.separator + "service.bat", null, map.getOrDefault("eslworking", ESLWORKING_SERVICE_NAME), TIMEOUT);
			System.out.println("Uninstall ESL-Working service finished!!!");
		} catch (IOException | InterruptedException | TimeoutException e) {
			System.err.println("Uninstall ESL-Working service failed!!!");
		}
		//TODO 还原配置文件
		// 还原ESL-Working服务文件 
		try {
			recoveryFile(new File(ESLWORKING_HOME, "bin" + File.separator + "service.bat"));
			System.out.println("Recovery ESL-Working install.bat finished!!!");
		} catch (IOException e) {
			System.err.println("Recovery ESL-Working service.bat failed!!!");
		}
		// 还原Tomcat服务文件 
		try {
			recoveryFile(new File(TOMCAT_HOME, "bin" + File.separator + "service.bat"));
			System.out.println("Recovery Tomcat install.bat finished!!!");
		} catch (IOException e) {
			System.err.println("Recovery Tomcat service.bat failed!!!");
		}
		// 还原Integration服务文件 
		try {
			recoveryFile(new File(INTEGRATION_HOME, "bin" + File.separator + "install.bat"));
			recoveryFile(new File(INTEGRATION_HOME, "bin" + File.separator + "uninstall.bat"));
			System.out.println("Recovery Integration install.bat finished!!!");
		} catch (IOException e) {
			System.err.println("Recovery Integration install.bat failed!!!");
		}
		System.out.println("Uninstall finished!!!");
	}	
	
	/**
	 * 还原文件
	 * @param file
	 * @throws IOException
	 */
	private void recoveryFile(File file) throws IOException {
		// 还原eslworking的service.bat文件
		File bakFile = new File(file.getParentFile(), file.getName() + BAK_SUFFIX);
		if (bakFile.exists()) {
			if(file.exists()) {
				file.delete();
			}
			bakFile.renameTo(file);
		}
	}
	
	/**
	 * 卸载对接模块
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private void uninstallIntegration(String serviceName) throws IOException, InterruptedException, TimeoutException {
		String bin = new File(INTEGRATION_HOME, "bin").getPath();
		// 备份一份service.bat文件
		File bak = new File(bin, "uninstall.bat" + BAK_SUFFIX);
		File serviceFile = new File(bin, "uninstall.bat");
		Charset charset = FileUtils.getFileEncode(serviceFile);
		serviceFile.renameTo(bak);
		// 在service.bat文件中增加set JAVA_HOME
		try {
			if (!serviceFile.exists()) {
				serviceFile.createNewFile();
			}
		} catch (IOException e) {
			throw e;
		}
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(bak), charset.name()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(serviceFile), charset.name()))) {
			String content = "";
			String line;
			while ((line = reader.readLine()) != null) {
				//修改安装脚本里的basedir路径
				if (line.equals("set BASEDIR=%CD%")) {
					content += ("\n\rset BASEDIR=" + INTEGRATION_HOME + "\n");
				} else if (line.trim().startsWith("set SERVICE_NAME=")) {
					content += ("set SERVICE_NAME=" + serviceName + "\n");
				} else {
					content += (line +"\n\r");
				}
				//增加java_home的设置
				if (line.equals("@echo off")) {
					content += ("\n\rset JAVA_HOME=" + JAVA_HOME + "\n");
				}
				
			}
			writer.write(content);
		} catch(IOException e) {
			throw e;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(bin).append(File.separator).append("uninstall.bat");
		new SystemCmdManager().shell(sb.toString(), TIMEOUT);
	}
}
