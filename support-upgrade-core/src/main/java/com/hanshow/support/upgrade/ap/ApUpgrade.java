package com.hanshow.support.upgrade.ap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hanshow.support.util.Config;
import com.hanshow.support.upgrade.model.Ap;
import com.hanshow.support.util.SSH2Tools;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApUpgrade {

	/**
	 * args
	 * 
	 * @param 0
	 *            eslworking ip地址， 2上传文件夹
	 * @throws IOException
	 */
	private static String REMOTE_PATH = "/home/elinker/bin/";
	private static String FAST_UPGRADE = "fast_upgrade.sh";
	private static String REMOTE_CONFIG_PATH = "/home/elinker/etc/";
	private static String REMOTE_CONFIG_FILE_NAME = "config.ini";
	private static String AP_VERSION_SPLIT = "3.2.0";
	private static String TXT = ".txt";
	private static String[] SSH_CONNECT = {"root", "root", "22"};
	private static Logger logger = LoggerFactory.getLogger(ApUpgrade.class);

	public void exec() {
		// 获取ap信息
		List<Ap> apList = null;
		try {
			apList = getApList();
		} catch (IOException | URISyntaxException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		}
		if (apList == null) {
			return ;
		}
		// 准备升级需要文件
		String upgradeCmd = System.getProperty("user.dir") + File.separator + FAST_UPGRADE;
		String upgradePackage = Config.getInstance().getString("ap.upgrade.package");
		File upgradeCmdFile = new File(upgradeCmd);
		if (!upgradeCmdFile.exists()) {
			System.out.println(upgradeCmdFile + " path is error!");
			return;
		}
		File upgradePackageFile = new File(upgradePackage);
		if (!upgradePackageFile.exists()) {
			System.out.println(upgradePackageFile + " path is error!");
			return;
		} 
		// 截取将要升级版本号
		String version = upgradePackageFile.getName().substring(upgradePackageFile.getName().indexOf("_") + 1, upgradePackageFile.getName().lastIndexOf(".tar"));
		
		// 循环升级
		boolean isUpgrade = false;
		for (Ap ap : apList) {
			if (version.indexOf(ap.getVersion()) != -1){
				continue;
			}
			isUpgrade = true;
			System.out.println("begin upgrade " + ap.getId() + "(" + ap.getIp() + ")");
			new Thread(new Runnable() {

				@Override
				public void run() {
					upgrade(ap, upgradeCmdFile, upgradePackageFile, version);
				}
			}).start();
		}
		
		/**
		 * 等待10分钟，输出日志看是否升级成功
		 */
		if (isUpgrade) {
			try {
				Thread.sleep(500000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
				System.out.println(e.getMessage());
			}
		}
		Map<Integer, Ap> map = new HashMap<>();
		for(Ap ap : apList) {
			map.put(ap.getId(), ap);
		}
		try {
			apList = getApList(Config.getInstance().getString("ap.eslworking.aps.url"));
		} catch (IOException | URISyntaxException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		}
		for(Ap ap : apList) {
			if(!ap.getVersion().equals(map.get(ap.getId()).getVersion())) {
				System.out.println("ap:" + ap.getId() + " is newset version(" + ap.getVersion() + ")!");
			}
		}
	}
	
	/**
	 *  升级AP
	 * @param ap
	 * @param upgradeCmdFile
	 * @param upgradePackageFile
	 * @param version
	 */
	private static void upgrade(Ap ap, File upgradeCmdFile, File upgradePackageFile, String version) {
		// TODO Auto-generated method stub
		try (SSH2Tools ssh = new SSH2Tools().connect(ap.getIp(), Integer.valueOf(SSH_CONNECT[2]), SSH_CONNECT[0], SSH_CONNECT[1])) {
			System.out.println(ap.getIp() + " connect successed");
			// 拷贝升级文件及升级包到远程ap上
			ssh.scpFileToRemote(upgradeCmdFile.getPath(), REMOTE_PATH);
			System.out.println("uploaded " + upgradeCmdFile.getName());
			ssh.scpFileToRemote(upgradePackageFile.getPath(), REMOTE_PATH);
			System.out.println("uploaded " + upgradePackageFile.getName());
			// 赋权
			ssh.shell("chmod +x " + REMOTE_PATH + upgradeCmdFile.getName());
			System.out.println("chmod +x" + upgradeCmdFile.getName());
			// 执行升级命令
			StringBuffer sb = new StringBuffer();
			sb.append(". ").append(REMOTE_PATH).append(upgradeCmdFile.getName()).append(" ")
			.append(REMOTE_PATH).append(upgradePackageFile.getName()).append(" ")
			.append(version);
			ssh.shell(sb.toString());
			System.out.println(sb.toString());
			// 检查配置文件
			String result = ssh.exec("cat " + REMOTE_CONFIG_PATH + REMOTE_CONFIG_FILE_NAME);
			// 根据系统版本修改文件，当配置文件中的端口未配置时，3.2.0（含）以前版本为1234，之后版本为37021
			if (result.indexOf("port") == -1 && ap.getVersion().compareTo(AP_VERSION_SPLIT) <= 0) {
				String localPath = System.getProperty("user.dir").toString();
				// 创建临时文件夹存放配置文件
				File temp = new File(localPath + File.separator + "temp");
				if (!temp.exists()) {
					temp.mkdirs();
				}
				// 放到本地的临时文件用ip+文件名重命令，防止并发
				String localConfigName = ap.getIp() + "_" + REMOTE_CONFIG_FILE_NAME;
				ssh.scpFileFromRemote(REMOTE_CONFIG_PATH + REMOTE_CONFIG_FILE_NAME, temp.getPath() + File.separator + localConfigName);				
				Wini ini = new Wini(new File(temp, localConfigName));
				ini.getConfig().setLineSeparator("\n");
		        ini.put("esl-working", "port", "1234");
		        ini.store();
		        // 修改完成上传
		    	ssh.scpFileToRemote(temp.getPath() + File.separator + localConfigName, REMOTE_CONFIG_PATH + REMOTE_CONFIG_FILE_NAME);
		    	System.out.println("set ap port = 1234");
			} 
			ssh.shell("reboot");
			System.out.println(ap.getIp() + " is reboot ...........");
		} catch(ConnectException e) {
			System.out.println(ap.getId() + "(" + ap.getIp() + ")" + " connection failed!");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		} 
	}
	
	/**
	 * 先通过eslworking的接口文件获取ap,如果未配置接口路径从ip列表文件中获取
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private static List<Ap> getApList() throws IOException, URISyntaxException {
		String eslworkingApsUrl = Config.getInstance().getString("ap.eslworking.aps.url");
		String apIpList = Config.getInstance().getString("ap.ip.list");
		List<Ap> apList = new ArrayList<>();
		//接口获取ip地址
		if (eslworkingApsUrl != null && !eslworkingApsUrl.equals("")) {
			apList = getApList(eslworkingApsUrl.toString());
		} else if (apIpList != null && !apIpList.equals("")) {
			// 从ip列表文件获取ip地址
			File file = new File(apIpList);
			if (file.exists() && file.getName().endsWith(TXT)) {
				// 文件逐行读取，并验证是否为ip地址，ip格式正确加到ap列表中
				try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String line = "";
					while ((line = reader.readLine()) != null) {
						if (line.trim().matches("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}")) {
							Ap ap = new Ap(line.trim());
							ap.setVersion("1.0.0");
							apList.add(ap);
						}
					}
				}
			} else {
				System.out.println(file.getAbsolutePath() + " file not found or file format error");
				return null;
			}
		} else {
			System.out.println("Cann't found ap ip address, system exit");
			return null;
		}
		return apList;
	}

	/**
	 * 获取ap列表
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private static List<Ap> getApList(String url) throws IOException, URISyntaxException {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(url).get().build();
		Response response = client.newCall(request).execute();
		JSONObject jo = (JSONObject) JSONObject.parse(response.body().string());
		if (jo.getIntValue("errno") == 0) {
			List<Ap> apList = JSONArray.parseArray(((JSONObject) jo.get("ap_list")).get("g2").toString(), Ap.class);
			return apList;
		}
		return null;
	}
}
