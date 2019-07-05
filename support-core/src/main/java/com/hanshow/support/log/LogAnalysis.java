package com.hanshow.support.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LogAnalysis {
	static final SimpleDateFormat LOGBACK_DATE_FORMAT = new SimpleDateFormat("YY/MM/dd HH:mm:ss:SSS");
	static final SimpleDateFormat LOG4J_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
	
	public static final int LOGBACK = 1, LOG4J2 = 2;
	
	/**
	 * 
	 * @param logFile 日志文件
	 * @param seek 读取文件开始处
	 * @param conditions 过滤数据条件
	 * @param logType logBack或 log4j2
	 * @return
	 * @throws IOException 
	 */
	public static LogInfo analysisLog(File logFile, long seek, List<String> conditions, int logType) throws IOException {
		// 判断文件是否存在,不存在抛出找不到文件异常
		if (!logFile.exists()) {
			throw new FileNotFoundException("Cannot found " + logFile.getParent());
		}
		LogInfo logInfo = new LogInfo();
		// 基于传入的位置开始读数据
		try(RandomAccessFile file = new RandomAccessFile(logFile, "r")) {
			if(file.length() < seek) {
				seek = 0;
			}
			file.seek(seek);
			logInfo.setData(new HashMap<>());
			String line = null;
			boolean flag = false;
			StringBuffer errLog = null;	
			while ((line = file.readLine()) != null) {
				String date =line.trim().substring(0, 22);
				// 以日志开头的打印时间作为一条日志的起始条件
				try {
					if (logType == LOGBACK) {
						LOGBACK_DATE_FORMAT.parse(date);
					} else {
						LOGBACK_DATE_FORMAT.parse(date);
					}
					String tempCondition = "";
					// 匹配表达式,分别放入不同的列表
					for (String condition : conditions) {
						if (flag) {
							flag = false;
							List<String> list = logInfo.getData().get(tempCondition);
							if (list == null) {
								list = new ArrayList<>();
							}
							list.add(errLog.toString());
						}
						if (line.matches(condition)) {
							tempCondition = condition;
							flag = true;
							errLog = new StringBuffer();
							errLog.append(line + "\n");
						} 	
					}
					
				} catch (ParseException e) {
					// 第一行不是数字是错误的下一行继续追加数据
					if (flag && errLog != null) {
						errLog.append(line + "\n");
					}
				}
			}
		} catch(IOException e) {
			throw e;
		}
		return logInfo;
	}
	
	/**
	 * logback日志解析
	 * @param logFile
	 * @return
	 * @throws IOException
	 */
	public static LogInfo analysisLogBack(File logFile, long position, List<String> conditions) throws IOException {
		return analysisLog(logFile, position, conditions, LOGBACK);
	}
	
	/**
	 * log4j2日志解析
	 * @param logFile
	 * @param position
	 * @return
	 * @throws IOException
	 */
	public static LogInfo analysisLog4j(File logFile, long position, List<String> conditions) throws IOException {
		return analysisLog(logFile, position, conditions, LOG4J2);
	}
}
