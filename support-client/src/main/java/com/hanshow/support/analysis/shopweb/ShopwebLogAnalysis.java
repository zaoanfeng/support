package com.hanshow.support.analysis.shopweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ShopwebLogAnalysis {

	public static void main(String[] args) {
		File file = new File("C:\\Users\\Administrator\\Documents\\WeChat Files\\zaoanfeng\\FileStorage\\File\\2019-04\\shopweb-core-info.log");
		Map<String, String> list = new HashMap<String, String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
	            while ((line = reader.readLine()) != null) {
	            	//各种类型的打包数据（清屏、更新、查询、组网）
	                if (line.indexOf("record Integration data") != -1) {
	                	String str = line.substring(line.indexOf("{"));
	                	JSONObject jsonObject = (JSONObject) JSON.parse(str);
	                	JSONArray jsonArray = (JSONArray) jsonObject.get("items");
	                	for (Object obj : jsonArray) {
	                		JSONObject jo = (JSONObject)obj;
	                		String sku = jo.getString("sku");
	                		if (list.keySet().contains(sku)) {
	                			
	                			System.out.println("1 -> " + list.get(sku));
	                			System.out.println("2 -> " + JSON.toJSONString(jo));
	                		}
	                		list.put(sku, JSON.toJSONString(jo));
	                	}
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}
}
