package com.hanshow.support.upgrade.ap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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

public class ApConfig {

	private static String REMOTE_PATH = "/home/elinker/etc/";
	private static String REMOTE_FILE_NAME = "config.ini";
	private static String TXT = ".txt";
	private static String[] SSH_CONNECT = {"root", "root", "22"};
	private static Logger logger = LoggerFactory.getLogger(ApConfig.class);
	
	public void exec() {
		String eslworkingApsUrl = Config.getInstance().getString("ap.eslworking.aps.url");
		String apIpList = Config.getInstance().getString("ap.ip.list");
		String eslworkingIp = Config.getInstance().getString("ap.eslworking.ip");
		String eslworkingPort= Config.getInstance().getString("ap.eslworking.port");
		List<Ap> apList = new ArrayList<>();
		//接口获取ip地址
		if (eslworkingApsUrl != null && !eslworkingApsUrl.equals("")) {
			try {
				apList = getApList(eslworkingApsUrl.toString());
			} catch (IOException | URISyntaxException e) {
				logger.error(e.getMessage(), e);
				System.out.println(e.getMessage());
			}
		} else if (apIpList != null && !apIpList.equals("")) {
			// 从ip列表文件获取ip地址
			File file = new File(apIpList.toString());
			if (file.exists() && file.getName().endsWith(TXT)) {
				// 文件逐行读取，并验证是否为ip地址，ip格式正确加到ap列表中
				try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String line = "";
					while ((line = reader.readLine()) != null) {
						if (line.trim().matches("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}")) {
							Ap ap = new Ap(line.trim());
							apList.add(ap);
						}
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
					logger.error(e.getMessage(), e);
				}
			} else {
				System.out.println(file.getAbsolutePath() + " file not found or file format error");
			}
			
		} else {
			System.out.println("Cann't found ap ip address, system exit");
			System.exit(0);
		}
		
		if (eslworkingIp != null && eslworkingPort != null && !eslworkingIp.equals("") && !eslworkingPort.equals("")) {
			for (Ap ap : apList) {
				try {
					
					modifyApPort(ap, eslworkingIp.toString(), eslworkingPort.toString());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					System.out.println(e.getMessage());
				}
			}
		}
		
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
	
	public static boolean modifyApPort(Ap ap, String url, String port) throws Exception {
		try(SSH2Tools ssh = new SSH2Tools().connect(ap.getIp(), Integer.valueOf(SSH_CONNECT[2]), SSH_CONNECT[0], SSH_CONNECT[1])) {		
			// 接取文件
			String localPath = System.getProperty("user.dir").toString();
			ssh.scpFileFromRemote(REMOTE_PATH + REMOTE_FILE_NAME, localPath);
			// 根据系统版本修改文件，当配置文件中的端口未配置时，3.2.0（含）以前版本为1234，之后版本为37021
			Wini ini = new Wini(new File(localPath + File.separator + REMOTE_FILE_NAME));
			ini.getConfig().setLineSeparator("\n");
			ini.put("esl-working", "ipaddr", url);
			ini.put("esl-working", "port", port);
	        ini.store();
	        // 修改完成上传
	    	ssh.scpFileToRemote(localPath +File.separator + REMOTE_FILE_NAME, REMOTE_PATH);
	    	ssh.shell("reboot");
			System.out.println(ap.getIp() + " is reboot ...........");
			return true;
		} catch (Exception e) {
			throw e;
		}
	}
}
