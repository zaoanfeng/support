package com.hanshow.support.upgrade.shopweb;

import com.hanshow.support.upgrade.eslworking.ESLWorkingUpgrade;
import com.hanshow.support.util.Config;
import com.hanshow.support.util.ConfigCompare;
import com.hanshow.support.util.DBUtils;
import com.hanshow.support.util.FileUtils;
import com.hanshow.support.util.SystemCmdManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShopwebUpgrade {
	private static final String SHOPWEB_SERVICE_NAME = "shopweb.service.name";
	private static final String SHOPWEB_DEPLOY_PATH = "shopweb.deploy.path";
	private static final String SHOPWEB_BACKUP_PATH = "shopweb.backup.path";
	private static final String SHOPWEB_PACKAGE_PATH = "shopweb.package.path";
	private static final String SHOPWEB_LINUX_USER = "shopweb.linux.user";
	private static final String SHOPWEB_LINUX_USER_GROUP = "shopweb.linux.user.group";
	private static final String[] CONFIG_FILES = { "config.properties" };
	private static final String[] OVERRIDE_CONFIG_FILES = { "business_fields.properties", "business_fields.json" };
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
	private static Logger logger = LoggerFactory.getLogger(ESLWorkingUpgrade.class);

	public void exec() {
		System.out.println("Begin upgrade Shopweb >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		// 关闭服务
		try {
			if (new SystemCmdManager().status(Config.getInstance().getString(SHOPWEB_SERVICE_NAME))) {
				System.out.println("closing " + Config.getInstance().getString(SHOPWEB_SERVICE_NAME) + " service");
				new SystemCmdManager().stop(Config.getInstance().getString(SHOPWEB_SERVICE_NAME), 600000);
				System.out.println("closed " + Config.getInstance().getString(SHOPWEB_SERVICE_NAME) + " service");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			System.out.println(Config.getInstance().getString(SHOPWEB_SERVICE_NAME) + " not found");
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
		String shopwebDeployPath = null;
		String shopwebBackupPath = null;
		String shopwebPacakgePath = null;
		String linuxUser = null;
		String linuxUserGroup = null;
		try {
			shopwebDeployPath = URLDecoder.decode(Config.getInstance().getString(SHOPWEB_DEPLOY_PATH), "utf-8");
			shopwebBackupPath = URLDecoder.decode(Config.getInstance().getString(SHOPWEB_BACKUP_PATH), "utf-8");
			shopwebPacakgePath = URLDecoder.decode(Config.getInstance().getString(SHOPWEB_PACKAGE_PATH), "utf-8");
			linuxUser = Config.getInstance().getString(SHOPWEB_LINUX_USER);
			linuxUserGroup = Config.getInstance().getString(SHOPWEB_LINUX_USER_GROUP);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
			return;
		}
		try {
			// 备份项目
			FileUtils.copyFolder(shopwebDeployPath, shopwebBackupPath + File.separator + new File(shopwebDeployPath).getName());
			// 备份数据库
			Map<String, String> config = FileUtils.readProperties(new File(shopwebDeployPath + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "config.properties"));
			DBUtils.backup(DBUtils.MYSQL, config.get("db.url"), config.get("db.username"), config.get("db.password"), shopwebBackupPath + File.separator + sdf.format(new Date()) + File.separator + "shopweb.sql");
			//备份完成删除
			FileUtils.deleteFolder(new File(shopwebDeployPath));
			// 拷备war包到tomcat/webapps下
			FileUtils.copyFile(new File(shopwebPacakgePath), new File(shopwebDeployPath + ".war"));
			// 解压war包
			FileUtils.unzip(new File(shopwebDeployPath + ".war"), shopwebDeployPath);
			shopwebBackupPath = new File(shopwebBackupPath, new File(shopwebDeployPath).getName()).getPath();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
			System.exit(0);
		}
		String result = null;
		// 修改config配置文件
		for (String configFile : CONFIG_FILES) {
			try {
				result = ConfigCompare.compare(
						new File(shopwebDeployPath + File.separator + "WEB-INF" + File.separator + "classes"
								+ File.separator + configFile),
						new File(shopwebBackupPath + File.separator + "WEB-INF" + File.separator + "classes"
								+ File.separator + configFile),
						Config.getInstance().getStringArray("eslworking.exclude.config"));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(shopwebDeployPath + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + configFile)))) {
					if (result != null) {
						out.write(result);
					}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				System.out.println(e.getMessage());
			}
		}
		// 覆盖business_fields文件
		for (String configFile : OVERRIDE_CONFIG_FILES) {
			File file = new File(shopwebBackupPath + File.separator + "WEB-INF" + File.separator + "classes"
					+ File.separator + configFile);
			if (file.exists()) {
				if (configFile.equals("business_fields.properties")) {
					try {
						FileUtils.propertiesToJson(file, new File(shopwebDeployPath + File.separator + "WEB-INF"
								+ File.separator + "classes" + File.separator + "business_fields.json"));
					} catch (IOException e) {
						logger.error(configFile + " copy failed");
						logger.error(e.getMessage(), e);
					}
				} else {
					try {
						FileUtils.copyFile(file, new File(shopwebDeployPath + File.separator + "WEB-INF"
								+ File.separator + "classes" + File.separator + configFile));
					} catch (IOException e) {
						logger.error(configFile + " copy failed");
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		// linux下文件夹赋用户权限
		if (!System.getProperty("os.name").toLowerCase().startsWith("window")) {
			if ((linuxUser == null) || (linuxUserGroup == null)) {
				System.out.println("user an user.group not defined !");
				System.exit(0);
			}
			try {
				new SystemCmdManager()
						.shellOfLinux(
								new String[] { "sh", "-c",
										"/bin/chown -R " + linuxUser + ":" + linuxUserGroup + " " + shopwebDeployPath },
								600);
			} catch (IOException | InterruptedException | TimeoutException e) {
				logger.error(e.getMessage(), e);
				System.out.println(e.getMessage());
			}
		}
		// 开服务
		try {
			new SystemCmdManager().start(Config.getInstance().getString("shopweb.service.name"), 600);
			System.out.println("Startup Shopweb service successed");
		} catch (IOException | InterruptedException | TimeoutException e) {
			logger.error(e.getMessage(), e);
			System.out.println("Startup Shopweb service failed");
		}
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<Upgrade finished!");
	}
}
