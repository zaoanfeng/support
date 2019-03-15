package com.hanshow.support.scheduled;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.mail.EmailException;
import org.apache.velocity.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hanshow.support.SpringUtil;
import com.hanshow.support.analysis.LogAnalysis;
import com.hanshow.support.analysis.RealTimeLogAnalysis;
import com.hanshow.support.mail.Mail;
import com.hanshow.support.model.ApRecord;
import com.hanshow.support.model.Store;
import com.hanshow.support.monitor.Config;
import com.hanshow.support.monitor.Monitor;
import com.hanshow.support.monitor.mail.AnalysisStatus;
import com.hanshow.support.monitor.mail.DiskAnalysisStatus;
import com.hanshow.support.monitor.mail.NetworkAnalysisStatus;
import com.hanshow.support.monitor.mail.ServiceStatus;
import com.hanshow.support.upgrade.eslworking.ESLWorkingUpgrade;
import com.hanshow.support.util.JsonUtils;

@Component
public class TaskScheduled {

	@Autowired
	private LogAnalysis diskAnalysis;
	@Autowired
	private RealTimeLogAnalysis realTimeLogAnalysis;
	private Monitor monitor = new Monitor();
	private Calendar lastActiveTime;
	Map<String, Object> translateMap = null;
	private static final Integer INIT = 0 * 1000;
	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private static Logger logger = LoggerFactory.getLogger(ESLWorkingUpgrade.class);

	public void run() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// 监控服务是否正常运行
				Store store = new Store(Config.getInstance().getString("user.store.code"), Config.getInstance().getString("user.store.name"));
				
				String checkTime = Config.getInstance().getString("disk.check.time");
				if (!checkTime.equals("")) {
					try {
						if (lastActiveTime == null) {
							lastActiveTime = Calendar.getInstance();
						}
						Date time = TIME_FORMAT.parse(checkTime);
						Calendar now = Calendar.getInstance();
						Calendar c = Calendar.getInstance();
						c.setTime(time);
						// 到达时间执行一次查询，一天只执行一次
						c.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
						List<AnalysisStatus> analysisStatusList = null;
						//if (c.after(lastActiveTime) && c.before(now)) {
							analysisStatusList = diskAnalysis.exec();
							lastActiveTime = Calendar.getInstance();
						//}
						sendMail(monitor.serviceMonitor(), analysisStatusList, realTimeLogAnalysis.exec(), store);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}

			}
		}, INIT, Config.getInstance().getInt("monitor.delay") * 1000);
	}

	/**
	 * 满足条件的发送邮件
	 * @param serviceStatusList 服务状态列表
	 * @param analysisStatusList 日志分析状态列表
	 * @param store 门店信息
	 * @throws JSONException
	 * @throws IOException
	 * @throws EmailException
	 * @throws URISyntaxException
	 */
	@SuppressWarnings("unchecked")
	private void sendMail(List<ServiceStatus> serviceStatusList, List<AnalysisStatus> analysisStatusList, List<ApRecord> apRecords, Store store)
			throws JSONException, IOException, EmailException, URISyntaxException {
		if ((Config.getInstance().getBoolean("mail.enable").booleanValue())
				&& (((serviceStatusList != null) && (serviceStatusList.size() > 0))
						|| ((analysisStatusList != null) && (analysisStatusList.size() > 0)))) {
			if (this.translateMap == null) {
				this.translateMap = loadTranslate();
			}
			Map<String, Object> map = new HashMap<>();
			map.put("store", store);
			map.put("service", serviceStatusList);
			if (analysisStatusList != null) {
				for (AnalysisStatus status : analysisStatusList) {
					if ((status instanceof DiskAnalysisStatus)) {
						map.put("disk", status);
					}
					if ((status instanceof NetworkAnalysisStatus)) {
						map.put("network", status);
					}
				}
			}
			map.put("ap", apRecords);
			String mailTitle = ((Map<String, String>)this.translateMap.get("mail")).get("title") + "-" + store.getName() + "(" + store.getCode() + ")";
			Template template = Mail.loadTemplate("vm/MailBody.vm");
			Mail.sendHtml(mailTitle, map, template, this.translateMap,
					Config.getInstance().getString("mail.recipients").split(","));
			logger.info("mail send successed !");
		}
	}

	/**
	 * 读翻译文件
	 * @return
	 * @throws IOException
	 */
	private Map<String, Object> loadTranslate() throws IOException {
		Locale locale = null;
		String language = Config.getInstance().getString("mail.language");
		if ((language != null) && (!"".equals(language))) {
			locale = new Locale(language.split("_")[0], language.split("_")[1]);
		} else {
			locale = Locale.getDefault();
		}
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		StringBuffer content = new StringBuffer();
		try {
			inputStream = SpringUtil.getApplicationContext()
					.getResource("i18n" + File.separator + locale.getLanguage() + "_" + locale.getCountry() + ".json")
					.getInputStream();
			if (inputStream == null) {
				inputStream = SpringUtil.getApplicationContext().getResource("i18n" + File.separator + "zh_CN.json")
						.getInputStream();
			}
			if (inputStream == null) {
				logger.error("Cannot found translate file in i18n folder");
				throw new FileNotFoundException();
			}
			inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				content.append(line);
			}
			return (Map<String, Object>) JsonUtils.readJson(content.toString(), new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e) {
			throw e;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}
}
