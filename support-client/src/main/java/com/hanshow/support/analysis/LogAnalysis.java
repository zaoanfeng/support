package com.hanshow.support.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;
import com.hanshow.support.model.DiskAnalysis;
import com.hanshow.support.model.NetworkAnalysis;
import com.hanshow.support.model.Receive;
import com.hanshow.support.monitor.Config;
import com.hanshow.support.monitor.mail.AnalysisStatus;
import com.hanshow.support.monitor.mail.DiskAnalysisStatus;
import com.hanshow.support.monitor.mail.NetworkAnalysisStatus;
import com.hanshow.support.service.DiskAnalysisService;
import com.hanshow.support.service.NetworkAnalysisService;
import com.hanshow.support.service.ReceiveService;

@Component
public class LogAnalysis {

	private final static Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();
    private final static Splitter EQUALS_SPLITTER = Splitter.on('=').trimResults();
    private final static Splitter BLANK_SPLITTER = Splitter.on(' ').trimResults();
    private final static Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private final static String DELIMETER = " INFO  - ";
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    private final static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final static Integer DAY = 24 * 60 * 60 * 1000, HOUR = 60 * 60 * 1000;
    @Autowired
    private DiskAnalysisService diskAnalysisService;
    @Autowired
    private ReceiveService receiveService;
    @Autowired
    private NetworkAnalysisService networkAnalysisService;
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());

	public List<AnalysisStatus> exec() throws Exception {
		// 获取eslworking路径
		String eslworkingPath = Config.getInstance().getString("monitor.eslworking.path");
		// 获取eslworking配置文件信息
		File configFile = new File(eslworkingPath + File.separator + "config" + File.separator + "config.properties");
		if (!configFile.exists()) {
			logger.error("Can not found config.properties file");
			return null;
		}
		//从api1.uplink.url中读取配置访问shopweb的url，从中截取storeCode
		String storeCode = null;
		Properties properties = new Properties();
		try (InputStream configIn = new FileInputStream(configFile)) {
			properties.load(configIn);
			String url = properties.getProperty("api1.uplink.url");
			String[] strs = url.split("/");
			storeCode = strs[strs.length - 2];
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.error(e.getMessage(), e);
		}
		// 获取前一天的日志，并进行解析
		File logFolder = new File(eslworkingPath, "log");
		if (logFolder.exists() && logFolder.isDirectory()) {
			String logName_prefix = "eslworking_";
			String logName = "";
			for (int i=0; i<=10; i++) {
				if (i == 0) {
					logName = logName_prefix + DAY_FORMAT.format(new Date(new Date().getTime() - DAY)) + ".log.gz";
				} else {
					logName = logName_prefix + DAY_FORMAT.format(new Date(new Date().getTime() - DAY)) + "-" + i + ".log.gz";
				}
				
				File logFile = new File(logFolder, logName);
				if (!logFile.exists()) {
					if (i == 0) {
						continue;
					} else {
						break;
					}
				}
				try {		
					analysisLog(logFile, storeCode);
				} catch (Exception e) {
					throw e;
				}
			}	
		}
		// 分析解析后的日志数据
		 DiskAnalysisStatus diskStatus = analysisDisk();
		    NetworkAnalysisStatus networkStatus = analysisNetwork();
		    List<AnalysisStatus> list = new ArrayList<>();
		    if (diskStatus != null) {
		      list.add(diskStatus);
		    }
		    if (networkStatus != null) {
		      list.add(networkStatus);
		    }
		    return list;
	}
	
	/**
	 * 分析网络情况
	 */
	private NetworkAnalysisStatus analysisNetwork() {
		// TODO Auto-generated method stub
		NetworkAnalysisStatus status = null;
		if (networkAnalysisService.count() > 0 && receiveService.count() > 0) {
			double result = (double)(networkAnalysisService.count()) / receiveService.count();
			logger.info("ap connection timeout rate = " + (int)result);
			if (result > Config.getInstance().getInt("ap.ack.timeout.rate")) {
				status = new NetworkAnalysisStatus();
		        status.setPackageAmount(AnalysisStatus.SMALL, (int)result);
		        status.setStatus(AnalysisStatus.WARNING);	
		}} else {
			logger.info("ap connection timeout rate non-statistics");
		}
		return status;
	}

	/**
	 * 分析磁盘性能
	 * @param storeCode
	 * @throws EmailException 
	 */
	private DiskAnalysisStatus analysisDisk() throws EmailException {
		DiskAnalysisStatus status = new DiskAnalysisStatus();
		Integer packageSizeSmall = Config.getInstance().getInt("package.size.small");
		Integer packageSizeLarge = Config.getInstance().getInt("package.size.large");
		Integer packageSizeSmallRate = Config.getInstance().getInt("package.size.small.rate");
		Integer packageSizeMiddleRate = Config.getInstance().getInt("package.size.middle.rate");
		Integer packageSizeLargeRate = Config.getInstance().getInt("package.size.large.rate");
		DiskAnalysis small = diskAnalysisService.queryAllByFrameSizeAndTypeIsUpdateGroupBy(0, packageSizeSmall);
		DiskAnalysis middle = diskAnalysisService.queryAllByFrameSizeAndTypeIsUpdateGroupBy(packageSizeSmall, packageSizeLarge);
		DiskAnalysis large = diskAnalysisService.queryAllByFrameSizeAndTypeIsUpdateGroupBy(packageSizeLarge, Integer.MAX_VALUE);
		int level = 0;
		// 计算小包每小时的打包数量
		if (small != null && small.getSpentTime() > 0 && small.getEslCount() > 0) {
			int smallRate = (int)(HOUR / ((double)small.getSpentTime() / small.getEslCount()));
			logger.info("package size small rate = " + smallRate);
			status.setPackageAmount(AnalysisStatus.SMALL, smallRate);
			if (smallRate < packageSizeSmallRate) {
				level ++;
			}
		} else {
			logger.info("package size small rate non-statistics");
		}
		// 计算中包每小时的打包数量
		if (middle != null && middle.getSpentTime() > 0 && middle.getEslCount() > 0) {
			int middleRate = (int)(HOUR / ((double)middle.getSpentTime() / middle.getEslCount()));
			logger.info("package size middle rate = " + middleRate);
			status.setPackageAmount(AnalysisStatus.MIDDLE, middleRate);
			if (middleRate < packageSizeMiddleRate) {
				level ++;
			}
		}else {
			logger.info("package size middle rate non-statistics");
		}
		// 计算大包每小时的打包数量
		if (large != null && large.getSpentTime() > 0 && large.getEslCount() > 0 ) { 
			int largeRate = (int)(HOUR / ((double)large.getSpentTime() / large.getEslCount() ));
			logger.info("package size large rate = " + largeRate);
			status.setPackageAmount(AnalysisStatus.LARGE, largeRate);
			if (largeRate < packageSizeLargeRate) {
				level ++;
			}
		} else {
			logger.info("package size large rate non-statistics");
		}
		switch (level) {
		    case 3: 
		    	status.setStatus(AnalysisStatus.COLLAPSE);
		    	break;
		    case 2: 
		    	status.setStatus(AnalysisStatus.SERIOUS);
		    	break;
		    case 1: 
		    	status.setStatus(AnalysisStatus.WARNING);
		    	break;
		    default: 
		      status.setStatus(AnalysisStatus.NORMAL);
	    }
	    return status;
	}
	
	/**
	 * 解析日志，将打包信息整理存入数据库
	 * @param file 日志文件
	 * @param storeCode 门店编号
	 * @throws Throwable
	 */
	private void analysisLog(File file, String storeCode) throws Exception {
		// 处理前先将之前数据清空
		diskAnalysisService.deleteAll();
		receiveService.deleteAll();
		networkAnalysisService.deleteAll();
		String line = null;
		try (GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(file));
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((line = reader.readLine()) != null) {
            	//各种类型的打包数据（清屏、更新、查询、组网）
                if (line.contains("category=esl,action=session_created") && line.contains("result=success") && line.contains("UPDATE")) {
                    Map<String, Object> record = parseRecord(line);
                    DiskAnalysis analysis = new DiskAnalysis();
                    analysis.setLogTime(DATE_FORMAT.parse(record.get("log_time").toString()));
                    analysis.setStoreCode(storeCode);
                    analysis.setEslId(record.get("eslid").toString());
                    analysis.setSessionType(record.get("payload_type").toString());
                    analysis.setFrames(Long.valueOf(record.get("frame_size").toString()));
                    analysis.setRetryTimes(Byte.valueOf(record.get("payload_retry_time").toString()));
                    analysis.setSpentTime(Long.valueOf(record.get("time").toString()));
                    diskAnalysisService.insert(analysis);      
                } else if (line.contains("category=esl,action=receive") && line.contains("UPDATE")) {
                	//接收任务
                	Map<String, Object> record = parseRecord(line);
                	Receive receive = new Receive();
                    receive.setLogTime((Date)record.get("log_item"));
                    receive.setStoreCode(storeCode);
                    receive.setEslId(record.get("eslid").toString());
                    receive.setType(record.get("payload_type").toString());
                    receive.setRetryTimes(Integer.valueOf(record.get("payload_retry_time").toString()));
                    receiveService.insert(receive);
                } else if (line.contains("action=esl_ack_timeout")) {
                	Map<String, Object> record = parseRecord(line);
                	NetworkAnalysis networkAnalysis = new NetworkAnalysis();
                	networkAnalysis.setApId(Integer.valueOf(record.get("apid").toString()));
                	networkAnalysis.setEslId(record.get("eslid").toString());
                	networkAnalysis.setLogTime(DATE_FORMAT.parse(record.get("log_time").toString()));
                	networkAnalysis.setRfPower(Integer.valueOf(record.get("rf_power").toString()));
                	networkAnalysis.setTaskId(Long.valueOf(record.get("task_id").toString()));
                	networkAnalysis.setStoreCode(storeCode);
                	networkAnalysisService.insert(networkAnalysis);
                }
            }
        } catch (Exception e) {
            throw e;
        }
	}
	
	/**
	 * 解析日志格式，转成map对象
	 * @param line 一行日志数据
	 * @return map对象
	 * @throws Exception
	 */
    private Map<String, Object> parseRecord(String line) throws Exception {
        Map<String, Object> record = new LinkedHashMap<>();
        int start = line.indexOf(DELIMETER);
        List<String> fields = COMMA_SPLITTER.splitToList(line.substring(start + DELIMETER.length()));
        for (int i = 2, len = fields.size(); i < len; ++i) {
            List<String> kv = EQUALS_SPLITTER.splitToList(fields.get(i));
            if (NUMBER_PATTERN.matcher(kv.get(1)).matches()) {
                record.put(kv.get(0), Long.valueOf(kv.get(1)));
            } else {
                record.put(kv.get(0), kv.get(1));
            }
        }

        String prefix = line.substring(0, start);
        fields = BLANK_SPLITTER.splitToList(prefix);
        record.put("log_time", fields.get(0) + ' ' + fields.get(1));
        return record;
    }
}
