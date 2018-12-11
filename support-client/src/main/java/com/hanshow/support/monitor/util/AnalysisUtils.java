package com.hanshow.support.monitor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisUtils {

	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	/**
	 * 解词
	 * @param file
	 * @param problemMap
	 * @return
	 * @throws IOException
	 */
	public static Map<String, List<Integer>> analysis(File file, Map<String, String> problemMap) throws IOException {
		Map<String, List<Integer>> statistics = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {	
			String content = "";
			String line = "";
			boolean end = false;
			while(null != (line = reader.readLine())) {	
				//一行行读，以时间开头的认为是一行数据
				try {
					sdf.parse(line.substring(0, 22));
					end = true;
					//
					if (end && content != null && !content.equals("")){
						//迭代每个问题，进行单词匹配，数量的匹配
						for (String key : problemMap.keySet()) {
							String[] keywords = problemMap.get(key).split(" ");
							int count = 0;
							for (String keyword : keywords) {
								if (content.indexOf(keyword) != -1) {
									count++;
								}
							}
							List<Integer> list = statistics.get(key);
							if (list == null) {
								list = new ArrayList<>();
								statistics.put(key, list);
							}
							list.add(count);
						}
					}
					end = false;
					content = line;
					
					
				} catch (Exception e) {
					content += line;
				}	
			}
			return statistics;
		}  catch (IOException e) {
			throw e;
		}
	}
}
