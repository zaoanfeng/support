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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hanshow.support.upgrade.model.Ap;
import com.hanshow.support.util.Config;
import com.hanshow.support.util.HttpClient;
import com.hanshow.support.util.SSH2Tools;

import okhttp3.Request;
import okhttp3.Response;

public class ApSSLUpgrade{
	
	private static String REMOTE_PATH = "/tmp/";
	private static String UPDATE = "update.sh";
	private static String TXT = ".txt";
	private static String[] SSH_CONNECT = {"root", "root", "22"};
	private static Logger logger = LoggerFactory.getLogger(ApUpgrade.class);
	Map<String, Boolean> finishedResult = new HashMap<>();
	public void exec() {
		// 获取ap信息
		List<Ap> apList = new ArrayList<>();
		try {
			apList = getApList().stream().filter(ap -> ap.isOnline()).collect(() -> new ArrayList<Ap>(), (list, ap) -> list.add(ap), (list1, list2) -> list1.addAll(list2));
		} catch (IOException | URISyntaxException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		}
		if (apList == null) {
			return ;
		}
		// 准备升级需要文件
		File upgradeCmdFile = new File(System.getProperty("user.dir"), UPDATE);
		if (!upgradeCmdFile.exists()) {
			System.out.println(upgradeCmdFile + " path is error!");
			return;
		}
		File upgradePackageFile = new File(System.getProperty("user.dir"),"update.zip");
		if (!upgradePackageFile.exists()) {
			System.out.println(upgradePackageFile + " path is error!");
			return;
		} 
		File binPackageFile = new File(System.getProperty("user.dir"),"appack.bin");
		if (!binPackageFile.exists()) {
			System.out.println(binPackageFile + " path is error!");
			return;
		} 

		// 循环升级
		for (Ap ap : apList) {
			System.out.println("begin upgrade " + ap.getId() + "(" + ap.getIp() + ")");
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						if(!upgrade(ap, upgradeCmdFile, upgradePackageFile)) {						
							throw new Exception("ap:" + ap.getId() + "(" + ap.getIp() + ")" + " secuity firmware upgrade failed!");
						}
						Thread.sleep(60 * 1000);
						System.out.println("ap:" + ap.getId() + "(" + ap.getIp() + ")" + " secuity firmware upgrade finished!");
						String webPassword = Config.getInstance().getString("ap.web.password");
						new UpgradeApByHttp().upgrade(ap.getIp(), webPassword == null || webPassword.isEmpty() ? "admin" : webPassword);
						System.out.println("ap:" + ap.getId() + "(" + ap.getIp() + ")" + " upgrade successed!");
						finishedResult.put(ap.getIp(), Boolean.TRUE);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						System.out.println("ap:" + ap.getId() + "(" + ap.getIp() + ")" + " upgrade failed!");
						finishedResult.put(ap.getIp(), Boolean.FALSE);
					}

				}

			}).start();
		}
		while(finishedResult.size() < apList.size()) {
			try {
				Thread.sleep(60 * 1000);				
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
		System.out.println(String.format("total %s ap upgrade finished, %s successed!, %s failed!", 
				finishedResult.size(),
				finishedResult.values().stream().filter(v -> v== Boolean.TRUE).count(),
				finishedResult.values().stream().filter(v -> v == Boolean.FALSE).count()));
		finishedResult.forEach((k,v) -> {
			if (v == Boolean.FALSE) {
				System.out.println(k);
			}
		});
		System.out.println("The ap is restarting, wait around 2-3 minute");
		String eslworkingApsUrl = Config.getInstance().getString("ap.eslworking.aps.url");
		if (eslworkingApsUrl != null && !eslworkingApsUrl.equals("")) {
			System.out.println("Waiting finally output result......");
			//等待5分钟从eslworking获取版本匹配
			int i=0;
			while (i <= 10) {
				i++;
				try {
					Thread.sleep(60 * 1000);
					List<Ap> lastApList = getApList().stream().filter(ap -> ap.isOnline()).collect(() -> new ArrayList<Ap>(), (list, ap) -> list.add(ap), (list1, list2) -> list1.addAll(list2));
					if (lastApList.size() >= apList.size()) {
						lastApList.forEach(ap -> {
							System.out.println(JSON.toJSONString(ap));
						});
						break;
					}
					if (i == 10) {
						lastApList.forEach(ap -> {
							System.out.println(JSON.toJSONString(ap));
						});
						System.out.println("Waiting finally output result timeout!!!");
					}
				} catch (IOException | URISyntaxException | InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
			
		System.exit(0);
	}
	
	/**
	 *  升级AP
	 * @param ap
	 * @param upgradeCmdFile
	 * @param upgradePackageFile
	 * @param version
	 * @throws Exception 
	 */
	private static boolean upgrade(Ap ap, File upgradeCmdFile, File upgradePackageFile) {
		String apPassword = Config.getInstance().getString("ap.password");
		String apNewPassword = Config.getInstance().getString("ap.new.password");
		if (apPassword == null || apPassword.isEmpty()) {
			apPassword = SSH_CONNECT[1];
		}
		// TODO Auto-generated method stub
		try (SSH2Tools ssh = new SSH2Tools().connect(ap.getIp(), Integer.valueOf(SSH_CONNECT[2]), SSH_CONNECT[0], apPassword)) {
			System.out.println(ap.getIp() + " connect successed");
			// 拷贝升级文件及升级包到远程ap上
			ssh.scpFileToRemote(upgradeCmdFile.getPath(), REMOTE_PATH);
			System.out.println(ap.getIp() + " uploaded " + upgradeCmdFile.getName());
			ssh.scpFileToRemote(upgradePackageFile.getPath(), REMOTE_PATH);
			System.out.println(ap.getIp() + " uploaded " + upgradePackageFile.getName());
			// 赋权
			ssh.shell("chmod a+x " + REMOTE_PATH + upgradeCmdFile.getName());
			System.out.println(ap.getIp() + " chmod a+x " + upgradeCmdFile.getName());
			// 执行升级命令
			StringBuffer sb = new StringBuffer();
			sb.append("sh ").append(REMOTE_PATH).append(upgradeCmdFile.getName());
			ssh.shell(sb.toString());
			System.out.println(ap.getIp() + " " + sb.toString());
			ssh.shell("reboot");
			System.out.println(ap.getIp() + " is reboot ...........");
			ssh.reconnect(SSH_CONNECT[0], apNewPassword);
			return true;
		} catch(ConnectException e) {
			System.out.println(ap.getId() + "(" + ap.getIp() + ")" + " connection failed!");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.out.println(e.getMessage());
		} 
		return false;
	}
	
	/**
	 * 先通过eslworking的接口文件获取ap,如果未配置接口路径从ip列表文件中获取
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private static List<Ap> getApList() throws IOException, URISyntaxException {
		String eslworkingApsUrl = Config.getInstance().getString("ap.eslworking.aps.url");
		String apIpList = System.getProperty("user.dir") + File.separator + Config.getInstance().getString("ap.ip.list");
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
							ap.setOnline(true);
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
		Request request = new Request.Builder().url(url).get().build();
		Response response = new HttpClient().getHttpClient(request.isHttps()).newCall(request).execute();
		JSONObject jo = (JSONObject) JSONObject.parse(response.body().string());
		if (jo.getIntValue("errno") == 0) {
			List<Ap> apList = new ArrayList<>();
			if (Config.getInstance().getBoolean("ap.eslworking.store.single")) {
				apList = JSONArray.parseArray(((JSONObject) jo.get("ap_list")).get("g2").toString(), Ap.class);
			} else {
				apList = JSONArray.parseArray(((JSONObject) jo.get("data")).get("g2").toString(), Ap.class);
			}
			return apList;
		}
		return null;
	}
	
	
}
