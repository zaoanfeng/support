package com.hanshow.support.upgrade.shopweb;

import com.hanshow.support.upgrade.eslworking.ESLWorkingUpgrade;
import com.hanshow.support.util.Config;
import com.hanshow.support.util.ConfigCompare;
import com.hanshow.support.util.FileUtils;
import com.hanshow.support.util.SystemCmdManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
	private static Logger logger = LoggerFactory.getLogger(ESLWorkingUpgrade.class);

	public void exec() {
		System.out.println("Begin upgrade Shopweb >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		try {
			if (new SystemCmdManager().status(Config.getInstance().getString("shopweb.service.name"))) {
				System.out.println("closing " + Config.getInstance().getString("shopweb.service.name") + " service");
				new SystemCmdManager().stop(Config.getInstance().getString("shopweb.service.name"), 600000);
				System.out.println("closed " + Config.getInstance().getString("shopweb.service.name") + " service");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			System.out.println(Config.getInstance().getString("shopweb.service.name") + " not found");
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
			shopwebDeployPath = URLDecoder.decode(Config.getInstance().getString("shopweb.deploy.path"), "utf-8");
			shopwebBackupPath = URLDecoder.decode(Config.getInstance().getString("shopweb.backup.path"), "utf-8");
			shopwebPacakgePath = URLDecoder.decode(Config.getInstance().getString("shopweb.package.path"), "utf-8");
			linuxUser = Config.getInstance().getString("shopweb.linux.user");
			linuxUserGroup = Config.getInstance().getString("shopweb.linux.user.group");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
			return;
		}
		try {
			FileUtils.copyFolder(shopwebDeployPath,
					shopwebBackupPath + File.separator + new File(shopwebDeployPath).getName());

			FileUtils.deleteFolder(new File(shopwebDeployPath));

			FileUtils.copyFile(new File(shopwebPacakgePath), new File(shopwebDeployPath + ".war"));

			FileUtils.unzip(new File(shopwebDeployPath + ".war"), shopwebDeployPath);
			shopwebBackupPath = new File(shopwebBackupPath, new File(shopwebDeployPath).getName()).getPath();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		}
		String result = null;
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
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(new File(shopwebDeployPath + File.separator
						+ "WEB-INF" + File.separator + "classes" + File.separator + configFile)));
				Throwable localThrowable3 = null;
				try {
					if (result != null) {
						out.write(result);
					}
				} catch (Throwable localThrowable1) {
					localThrowable3 = localThrowable1;
					throw localThrowable1;
				} finally {
					if (out != null) {
						if (localThrowable3 != null) {
							try {
								out.close();
							} catch (Throwable localThrowable2) {
								localThrowable3.addSuppressed(localThrowable2);
							}
						} else {
							out.close();
						}
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				System.out.println(e.getMessage());
			}
		}
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
