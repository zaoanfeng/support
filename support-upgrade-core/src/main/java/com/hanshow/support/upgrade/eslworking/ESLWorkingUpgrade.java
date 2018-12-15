package com.hanshow.support.upgrade.eslworking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hanshow.support.util.Config;
import com.hanshow.support.util.SystemCmdManager;
import com.hanshow.support.util.ConfigCompare;
import com.hanshow.support.util.FileUtils;

public class ESLWorkingUpgrade {
	
	private static final String ESLWORKING_SERVICE_NAME = "eslworking.service.name";
	private static final String ESLWORKING_OLD_PATH = "eslworking.old.path";
	private static final String ESLWORKING_NEW_PATH = "eslworking.new.path";
	private static final String ESLWORKING_PACKAGE_PATH = "eslworking.package.path";
	private static final String DATA_FOLDER = File.separator + "data";
	private static final String ESLWORKING_LINUX_USER = "eslworking.linux.user";
	private static final String ESLWORKING_LINUX_USER_GROUP = "eslworking.linux.user.group";
    private static final String[] CONFIG_FILES = {"config.properties", "server.conf"};
    private static Logger logger = LoggerFactory.getLogger(ESLWorkingUpgrade.class);
	
    
    
	public void exec() {
		/**
		 *  关服务
			解压文件到当前目录
			老系统的安装目录 
			新系统的安装目录
			新系统的升级包路径
			cp flash文件（递归），
			修改配置文件
			重新组网？
			卸载、重装服务
			done 
			启动服务
		 */
		System.out.println("Begin upgrade ESL-Working >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		//关闭服务
		try {
			if (new SystemCmdManager().status(Config.getInstance().getString(ESLWORKING_SERVICE_NAME))) {
				System.out.println("closing " + Config.getInstance().getString(ESLWORKING_SERVICE_NAME) + " service");
				new SystemCmdManager().stop(Config.getInstance().getString(ESLWORKING_SERVICE_NAME), 600000);
				System.out.println("closed " + Config.getInstance().getString(ESLWORKING_SERVICE_NAME) + " service");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			System.out.println(Config.getInstance().getString(ESLWORKING_SERVICE_NAME) + " not found");
			return;
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
			return;
		} catch (TimeoutException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
			return;
		}
		
		//获取
		String eslworkingOldPath = Config.getInstance().getString(ESLWORKING_OLD_PATH);
		String eslworkingNewPath = Config.getInstance().getString(ESLWORKING_NEW_PATH);
		String eslworkingPackagePath = Config.getInstance().getString(ESLWORKING_PACKAGE_PATH);
		
		try {
			// 解压新软件包
			String folderName = FileUtils.unzip(new File(eslworkingPackagePath), eslworkingNewPath);
			if (folderName == null) {
				logger.warn(eslworkingPackagePath + " content error!!!");
				System.out.println(eslworkingPackagePath + " content error!!!");
				return;
			} else {
				eslworkingNewPath = eslworkingNewPath + File.separator + folderName;
			}
			// copy文件
			FileUtils.copyFolder(eslworkingOldPath + DATA_FOLDER, eslworkingNewPath + DATA_FOLDER, Config.getInstance().getStringArray("eslworking.exclude.folder"));	
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
			return;
		}
		
		//修改properties配置文件
		String result = null;
		for (String configFile : CONFIG_FILES) {
			try {
				result = ConfigCompare.compare(new File(eslworkingNewPath + File.separator + "config" + File.separator + configFile), new File(eslworkingOldPath + File.separator + "config" + File.separator + configFile), Config.getInstance().getStringArray("eslworking.exclude.config"));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			try(BufferedWriter out = new BufferedWriter(new FileWriter(new File(eslworkingNewPath + File.separator + "config" + File.separator + configFile)))){
				if (result != null) {
					out.write(result);
				}	
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				System.out.println(e.getMessage());
			}
		}
		
		// 服务使用指令
		String oldWindowsCmd = eslworkingOldPath + File.separator + "bin" + File.separator + "service.bat";
		String oldLinuxCmd = eslworkingOldPath + File.separator + "bin" + File.separator + "eslworking.sh";
		String newWindowsCmd = eslworkingNewPath + File.separator + "bin" + File.separator + "service.bat";
		String newLinuxCmd = eslworkingNewPath + File.separator + "bin" + File.separator + "eslworking.sh";
		String linuxUser = Config.getInstance().getString(ESLWORKING_LINUX_USER);
		String linuxUserGroup = Config.getInstance().getString(ESLWORKING_LINUX_USER_GROUP);
		
		// 修改服务信息
		try {
			result = ConfigCompare.compareServiceScript(new File(newWindowsCmd), new File(oldWindowsCmd), new File(newLinuxCmd), new File("/etc/init.d/" + Config.getInstance().getString(ESLWORKING_SERVICE_NAME)), eslworkingOldPath, eslworkingNewPath);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		}
		File outFile = null;
		if (System.getProperty("os.name").toLowerCase().startsWith("window")) {
			outFile = new File(newWindowsCmd);
		} else {
			if (linuxUser == null || linuxUserGroup == null) {
				System.out.println("user an user.group not defined !");
				System.exit(0);
			}
			
			try {
				new SystemCmdManager().shellOfLinux(new String[] {"sh", "-c", "/bin/chmod +x " + eslworkingNewPath + File.separator + "bin" + File.separator + "*"}, 600);
				new SystemCmdManager().shellOfLinux(new String[] {"sh", "-c", "/bin/chown -R " + linuxUser + ":" + linuxUserGroup + " "+ new File(eslworkingNewPath).getParent()}, 600);
			} catch (IOException | InterruptedException | TimeoutException e) {
				logger.error(e.getMessage(), e);
				System.out.println(e.getMessage());
			}
			outFile = new File(newLinuxCmd);
		}
		try(BufferedWriter out = new BufferedWriter(new FileWriter(outFile))){
			if (result != null) {	
				out.write(result);
			}	
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		}

		// 卸载服务
		try {			
			new SystemCmdManager().uninstall(oldWindowsCmd, oldLinuxCmd, Config.getInstance().getString(ESLWORKING_SERVICE_NAME), 600);
			System.out.println("Uninstall old service successed");
		} catch (IOException | InterruptedException | TimeoutException e) {
			logger.error(e.getMessage(), e);
			System.out.println("Uninstall old service failed");
		}
		
		// 安装新服务
		try {
			new SystemCmdManager().install(newWindowsCmd, newLinuxCmd, Config.getInstance().getString(ESLWORKING_SERVICE_NAME), 600);
			System.out.println("Install new service successed");
		} catch (IOException | InterruptedException | TimeoutException e) {
			logger.error(e.getMessage(), e);
			System.out.println("Install new service failed");
		}
		
		//启动新服务
		try {
			new SystemCmdManager().start(Config.getInstance().getString(ESLWORKING_SERVICE_NAME), 600);
			System.out.println("Startup new service successed");
		} catch (IOException | InterruptedException | TimeoutException e) {
			logger.error(e.getMessage(), e);
			System.out.println("Startup new service failed");
		}
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Upgrade finished!");
	}
}
