package com.hanshow.support.analysis;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hanshow.support.model.ApRecord;
import com.hanshow.support.monitor.Config;
import com.hanshow.support.service.ApRecordService;
import com.hanshow.support.service.ConfigService;
import com.hanshow.support.util.FileUtils;

@Component
public class RealTimeLogAnalysis {

	@Autowired
	private ApRecordService apRecordService;
	
	@Autowired
	private ConfigService configService;
	
	private RestTemplate restClient = new RestTemplate();
	
	private static final String LOG_FOLDER = "log";
	private static final String LOG_FILE_NAME = "eslworking.log";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Map<String, String> eslworkingConfig;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public List<ApRecord> exec() throws IOException {
		//获取eslworking的日志文件目录，及日志文件
		String eslworkingPath = Config.getInstance().getString("monitor.eslworking.path");
		File configFile = new File(eslworkingPath + File.separator + "config" + File.separator + "config.properties");
		if (!configFile.exists()) {
			logger.error("Can not found config.properties file");
			return null;
		}
		eslworkingConfig = FileUtils.readProperties(configFile);
		File logFolder = new File(eslworkingPath, LOG_FOLDER);
		if (!logFolder.exists() && !logFolder.isDirectory()) {
			logger.error(logFolder.getPath() + " cannot found!");
			return null;
		}
		File logFile = new File(logFolder, LOG_FILE_NAME);
		if (!logFile.exists()) {
			logger.warn(logFile.getPath() + " cannot found!");
			return null;
		}
		try {
			if(analysisLog(logFile.getPath(), Config.getInstance().getString("user.store.code"))) {
				return analysisNetwork();
			}
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private boolean analysisLog(String path, String storeCode) throws Exception {
		
		boolean flag = false;
		try(RandomAccessFile file = new RandomAccessFile(path, "r")) {
			//从数据库读出上次读取日志的位置
			com.hanshow.support.model.Config config = configService.queryAll().stream().findFirst().orElse(new com.hanshow.support.model.Config());
			
			long position = 0;
			config.getPosition();
			logger.debug("position=" + config.getPosition());
			logger.debug("file.length()=" + file.length());
			if(file.length() > config.getPosition()) {
				position = config.getPosition();
			}
			file.seek(position);
			
			String line = null;
			JSONObject aps = null;
			while((line = file.readLine()) != null) {
				//存在换行字节数量的统计，无法累加
				//position += (line.getBytes().length + 1);
				if (line.indexOf("WARN") != -1 && line.indexOf("offline due to connection closed by peer") != -1) {
					logger.info(line);
					ApRecord record = new ApRecord();
					record.setLogTime(sdf.parse(line.substring(0, 19)));
					line = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
					//{id=1 MAC=DC:07:C1:02:E8:1C ip=192.9.192.101}
					String[] strs = line.split(" ");
					for (String str : strs) {
						String[] result = str.split("=");
						switch(result[0].toLowerCase()) {
						case "id":
							record.setApId(Integer.valueOf(result[1]));
							continue;
						case "mac":
							record.setApMac(result[1]);
							continue;
						case "ip":
							record.setApIp(result[1]);
							continue;
						default:
							continue;
						}	
					}
					//插入数据库
					apRecordService.insert(record);
					flag = true;
				} else if(line.indexOf("APg1: [FTP-MAIN] WARN") != -1 && line.indexOf("idle time exceed limit, it is closed") != -1) {
					logger.info(line);
					ApRecord record = new ApRecord();
					record.setLogTime(sdf.parse(line.substring(0, 19)));
					line = line.substring(line.indexOf("(/") + 2);
					line = line.substring(0, line.indexOf(":"));
					record.setApIp(line);
					if (aps == null || aps.size() < 0) {
						aps =restClient.getForObject(String.format("http://127.0.0.1:%s/api2/aps", eslworkingConfig.get("httpserver.port")), JSONObject.class);
						logger.info(aps.toJSONString());
					}				
					JSONArray array = aps.getJSONObject("ap_list").getJSONArray("g1");
					for (int i = 0; i < array.size(); i++) {		
						JSONObject ap = (JSONObject) array.get(i);
						if (ap.getString("ip").equals(record.getApIp())) {
							record.setApId(ap.getInteger("id"));
							record.setApMac(ap.getString("mac"));
							//插入数据库
							apRecordService.insert(record);
							flag = true;
							break;
						}
					}	
				}
			}
			//更新文件读取标志
			config.setPosition(file.length());
			config.setCreateTime(new Date());
			configService.insert(config);
		} catch (Exception e) {
			throw e;
		}	
		return flag;
	}
	
	
	private List<ApRecord> analysisNetwork() {
		int interval = Config.getInstance().getInt("ap.offline.interval");
		int times = Config.getInstance().getInt("ap.offline.times");
		//删除超时的数据
		apRecordService.deleteByDateBefore(new Date(new Date().getTime() - interval * 1000));
		List<ApRecord> list = apRecordService.queryAll();
		
		Map<Integer, Long> map = list.stream().collect(Collectors.groupingBy(l -> l.getApId() , Collectors.counting()));
		
		List<ApRecord> apList = new ArrayList<>();
		map.forEach((k,v) -> {
			logger.info("apid=" + k + ", amount=" + v);
			if (v >= times) {
				ApRecord ap = new ApRecord();
				ap.setApId(k);
				ap.setAmount(v.intValue());
				apList.add(ap);	
			}
		});
		return apList;
	}
}
