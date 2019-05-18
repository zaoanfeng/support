package com.hanshow.support.upgrade.eslworking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hanshow.support.upgrade.model.Ap;
import com.hanshow.support.util.HttpClient;

import okhttp3.Request;
import okhttp3.Response;

public class GetAPFromESLWorking {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void exec() {
		File eslworkingIpAddreass = new File(System.getProperty("user.dir"), "eslworking_ip.txt");
		if (!eslworkingIpAddreass.exists()) {
			System.out.println(eslworkingIpAddreass + " path is error!");
			return;
		}
		List<String> eslworkingIp = new ArrayList<> ();
		try(BufferedReader reader = new BufferedReader(new FileReader(eslworkingIpAddreass))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				eslworkingIp.add(line);		
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} 
		
		// 迭代查询
		File out = new File(System.getProperty("user.dir"), "out.csv");
		if (out.exists()) {
			out.delete();
		}
		try {
			out.createNewFile();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(out))) {
			logger.info("eslworkingIP, apMac, apIP, apVersion");
			writer.write("eslworkingIP, apMac, apIP, apVersion \n");
			eslworkingIp.forEach(ip ->{ 
				List<Ap> apList;
				try {
					apList = getApList(ip + "/api2/aps");
					for (Ap ap : apList) {
						logger.info(String.format("%s, %s, %s, %s", ip, ap.getMac(), ap.getIp(), ap.getVersion()));
						writer.write(String.format("%s, %s, %s, %s \n", ip, ap.getMac(), ap.getIp(), ap.getVersion()));
					}
				} catch (IOException | URISyntaxException e) {
					logger.error(e.getMessage(), e);
				}			
			});
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
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
		Request request = new Request.Builder().url(url).get().build();
		Response response = new HttpClient().getHttpClient(request.isHttps()).newCall(request).execute();
		JSONObject jo = (JSONObject) JSONObject.parse(response.body().string());
		if (jo.getIntValue("errno") == 0) {
			List<Ap> apList = JSONArray.parseArray(jo.get("G2").toString(), Ap.class);
			return apList;
		}
		return null;
	}
}
