import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticPower {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("args error = " + args);
			return;
		}
		String root = System.getProperty("user.dir");
		System.out.println("current dir = " + root);
		//找配置文件
		File configFile = new File(root,args[0]);
		if (!configFile.exists()) {
			System.out.println("Cannot found config file");
		}
		//找数据文件
		File dataFile = new File(root,args[1]);
		if (!dataFile.exists() || !dataFile.isDirectory()) {
			System.out.println("Cannot found data dir");
		}
		//读配置文件信息
		List<String> list = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				list.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//解析配置文件内容
		String[] freezerEsls = new String[] {};
		String[] esls154 = new String[] {};
		if (list.size() <= 0) {
			System.out.println("Cannot load config file content!");
		} else if (list.size() == 1) {
			freezerEsls = list.get(0).split(",");
			System.out.println("Cannot load 1.54 esl firmware!");
		} else  {
			freezerEsls = list.get(0).split(",");
			esls154 = list.get(1).split(",");
		}
		//输出文件名
		File outFile = new File(root, "out_" + sdf.format(new Date()) +".csv");
		//读源数源
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
			//writer.write(",1.54,1.54,1.54,1.54,1.54,1.54,1.54,1.54\n");
			writer.write(",1.54_26,1.54_27,1.54_28,1.54_29,1.54_30,1.54_31,1.54_32,1.54_33,26,27,28,29,30,31,32,33,\n");
			for (File file : dataFile.listFiles()) {
				//定义变量并赋值
				Map<String, Integer> statistics154 = new HashMap<>();
				statistics154.put("2.6", 0);
				statistics154.put("2.7", 0);
				statistics154.put("2.8", 0);
				statistics154.put("2.9", 0);
				statistics154.put("3.0", 0);
				statistics154.put("3.1", 0);
				statistics154.put("3.2", 0);
				statistics154.put("3.3", 0);
				Map<String, Integer> statisticsAll = new HashMap<>();
				statisticsAll.put("2.6", 0);
				statisticsAll.put("2.7", 0);
				statisticsAll.put("2.8", 0);
				statisticsAll.put("2.9", 0);
				statisticsAll.put("3.0", 0);
				statisticsAll.put("3.1", 0);
				statisticsAll.put("3.2", 0);
				statisticsAll.put("3.3", 0);
				//解析每一个文件的每一行数据
				try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String line = "";
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll("\"", "").substring(1);
						String[] kvs = line.split(",");
						Map<String, String> map = new HashMap<>();
						for (String kv : kvs) {
							if (kv.split(":").length == 2) {
								map.put(kv.split(":")[0], kv.split(":")[1]);
							}
						}
						//价签id非5的过滤掉
						if (!map.get("ESL_ID").startsWith("5")) {
							continue;
						}
						//冷冻价签不计算
						if (Arrays.asList(freezerEsls).contains(map.get("FIRMWARE"))) {
							continue;
						}
						// 仅计算1.54价签电量
						if (Arrays.asList(esls154).contains(map.get("FIRMWARE"))) {
							if (statistics154.get(map.get("BATTERY")) != null) {
								statistics154.put(map.get("BATTERY"), statistics154.get(map.get("BATTERY")) + 1);
							}
						}
						//计算所有价签电量
						if (statisticsAll.get(map.get("BATTERY")) != null) {
							statisticsAll.put(map.get("BATTERY"), statisticsAll.get(map.get("BATTERY")) + 1);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				//输出
				writer.write(file.getName().split("\\.")[0] + ",");
				writer.write(statistics154.get("2.6") + ",");
				writer.write(statistics154.get("2.7") + ",");
				writer.write(statistics154.get("2.8") + ",");
				writer.write(statistics154.get("2.9") + ",");
				writer.write(statistics154.get("3.0") + ",");
				writer.write(statistics154.get("3.1") + ",");
				writer.write(statistics154.get("3.2") + ",");
				writer.write(statistics154.get("3.3") + ",");
				writer.write(statisticsAll.get("2.6") + ",");
				writer.write(statisticsAll.get("2.7") + ",");
				writer.write(statisticsAll.get("2.8") + ",");
				writer.write(statisticsAll.get("2.9") + ",");
				writer.write(statisticsAll.get("3.0") + ",");
				writer.write(statisticsAll.get("3.1") + ",");
				writer.write(statisticsAll.get("3.2") + ",");
				writer.write(statisticsAll.get("3.3") + ",");
				writer.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
