package com.hanshow.support.upgrade.ap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hanshow.support.upgrade.model.Ap;
import com.hanshow.support.util.Config;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class ApConfig {
	
	private static String TXT = ".txt";
	
	private static Logger logger = LoggerFactory.getLogger(ApConfig.class);
	
	String eslworkingIp = Config.getInstance().getString("ap.eslworking.ip");
	String eslworkingPort = Config.getInstance().getString("ap.eslworking.port");
	
	
	public void exec() {
		String eslworkingApsUrl = Config.getInstance().getString("ap.eslworking.aps.url");
		String apIpList = Config.getInstance().getString("ap.ip.list");
		List<Ap> apList = new ArrayList<>();
		//接口获取ip地址
		if (eslworkingApsUrl != null && !eslworkingApsUrl.equals("")) {
			System.out.println("Get ip info from ESL-Working");
			try {
				apList = getApList(eslworkingApsUrl.toString());
			} catch (IOException | URISyntaxException e) {
				logger.error(e.getMessage(), e);
				System.out.println(e.getMessage());
			}
		} else if (apIpList != null && !apIpList.equals("")) {
			System.out.println("Get ip info from " + apIpList);
			// 从ip列表文件获取ip地址
			File file = new File(apIpList.toString());
			if (file.exists() && file.getName().endsWith(TXT)) {
				// 文件逐行读取，并验证是否为ip地址，ip格式正确加到ap列表中
				try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String line = "";
					while ((line = reader.readLine()) != null) {
						logger.info(line);
						//if (line.trim().matches("(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)")) {
							Ap ap = new Ap(line.trim());
							apList.add(ap);
						//}
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
				new Thread(new Runnable() {
					public void run() {
						try {	
							modifyApPort(ap, eslworkingIp.toString(), eslworkingPort.toString());
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							System.out.println(e.getMessage());
						}}
				}).start();
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
		response.close();
		if (jo.getIntValue("errno") == 0) {
			List<Ap> apList = JSONArray.parseArray(((JSONObject) jo.get("ap_list")).get("g2").toString(), Ap.class);
			return apList;
		}
		return null;
	}
	
	protected abstract boolean modifyApPort(Ap ap, String url, String port) throws Exception;
	
	public String getEslworkingIp() {
		return eslworkingIp;
	}

	public void setEslworkingIp(String eslworkingIp) {
		this.eslworkingIp = eslworkingIp;
	}

	public String getEslworkingPort() {
		return eslworkingPort;
	}

	public void setEslworkingPort(String eslworkingPort) {
		this.eslworkingPort = eslworkingPort;
	}
}
