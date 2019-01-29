package com.hanshow.support.upgrade.location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hanshow.support.upgrade.ap.ApPcieConfig;
import com.hanshow.support.util.Config;
import com.hanshow.support.util.DBUtils;
import com.hanshow.support.util.SSH2Tools;
import com.hanshow.support.util.SystemCmdManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Location {

	private static final Logger logger = LoggerFactory.getLogger(Location.class);
	private static final short SECOND = 1000;
	
	public void exec() {
		
		// 开始执行任务
		String[] startTimes = Config.getInstance().getString("task.begin.time").split(":");
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(startTimes[0]));
		c.set(Calendar.MINUTE, Integer.valueOf(startTimes[1]));
		c.set(Calendar.SECOND, Integer.valueOf(startTimes[2]));
		if (c.getTime().getTime() < new Date().getTime()) {
			c.set(Calendar.DATE, c.get(Calendar.DATE) + 1);
		}
		System.out.println(c.getTime());
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					//开始任务
					begin();
					boolean locationEnable = Config.getInstance().getProperties("location.enable") == null ? true : Boolean.valueOf(Config.getInstance().getProperties("location.enable").toString());
					if (locationEnable) {
						//执行完以上操作，等待5分钟开始定位
						Thread.sleep((Config.getInstance().getInt("location.delay") == 0 ? 300 : Config.getInstance().getInt("location.delay")) * SECOND);
						//开始定位
						startLocation();
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}, c.getTime(), Config.getInstance().getInt("task.delay") * SECOND);
		
		//结束执行任务
		String[] endTimes = Config.getInstance().getString("task.end.time").split(":");
		Calendar c1 = Calendar.getInstance();
		c1.set(Calendar.HOUR_OF_DAY, Integer.valueOf(endTimes[0]));
		c1.set(Calendar.MINUTE, Integer.valueOf(endTimes[1]));
		c1.set(Calendar.SECOND, Integer.valueOf(endTimes[2]));
		if (c1.getTime().getTime() < new Date().getTime()) {
			c1.set(Calendar.DATE, c1.get(Calendar.DATE) + 1);
		}
		System.out.println(c1.getTime());
		Timer timer1 = new Timer();
		timer1.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					end();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}, c1.getTime(), Config.getInstance().getInt("task.delay") * SECOND);
	}
	
	@SuppressWarnings("resource")
	public void begin() throws Exception{
		//修改基站ip
		ApPcieConfig ap = new ApPcieConfig();
		ap.setEslworkingIp(Config.getInstance().getString("eslworking.remote.ip"));
		ap.setEslworkingPort(Config.getInstance().getString("ap.eslworking.port"));
		ap.exec();
		//关闭eslworking服务
		if (Config.getInstance().getBoolean("eslworking.local.service.stop")) {
			try {
				new SystemCmdManager().stop(Config.getInstance().getString("eslworking.local.service"), 600);
			} catch (IOException | InterruptedException | TimeoutException e) {
				logger.error("close local eslworking service failed, suspend!!!");
				logger.error(e.getMessage(), e);
				return;
			}
		}
		// 迁移mysql数据
		if (Config.getInstance().getBoolean("shopweb.db.copy.enable")) {
			try {
				// 导出
				DBUtils dbUtils = new DBUtils(Config.getInstance().getString("production.shopweb.db.url"), Config.getInstance().getString("production.shopweb.db.username"), Config.getInstance().getString("production.shopweb.db.password"));
				File backupFile = new File(System.getProperty("user.dir") + File.separator + "backup.sql");
				dbUtils.backupMysql(Config.getInstance().getStringArray("shopweb.tables"), backupFile);
				// 导入
				dbUtils = new DBUtils(Config.getInstance().getString("test.shopweb.db.url"), Config.getInstance().getString("test.shopweb.db.username"), Config.getInstance().getString("test.shopweb.db.password"));
				try (BufferedReader br = new BufferedReader(new FileReader(backupFile))) {
					String line;
					List<String> list = new ArrayList<>();
					while ((line = br.readLine()) != null) {
						if (list.size() == 1000) {
							dbUtils.execute(list.toArray(new String[] {}));
							list.clear();
						} else {
							list.add(line);
						}	
					}
					dbUtils.execute(list.toArray(new String[] {}));
				}		
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}		
		// 获取远程信息
		String host = Config.getInstance().getString("eslworking.remote.ip");
		String user = Config.getInstance().getString("eslworking.remote.user");
		String password = Config.getInstance().getString("eslworking.remote.password");
		String port = Config.getInstance().getString("eslworking.remote.port");
		try(SSH2Tools ssh = new SSH2Tools().connect(host, Integer.valueOf((port == null || port.isEmpty()) ?"22" : port), user, password)) {		
			if (Config.getInstance().getBoolean("eslworking.remote.restart.enable")) {
				//关闭远程服务系统
				ssh.shell(Config.getInstance().getString("eslworking.remote.service.stop.script"));
				logger.info( Config.getInstance().getString("eslworking.remote.service" + " is stoped!!!"));
			}
			if (Config.getInstance().getBoolean("eslworking.db.copy.enable")) {
				// 删除远程数据库文件
				ssh.shell("rm -rf " + Config.getInstance().getString("eslworking.remote.db.path") + "/*");
				// 将本地的数据库拷贝到远程上
				ssh.scpFileToRemote(Config.getInstance().getString("eslworking.local.db.path"), Config.getInstance().getString("eslworking.remote.db.path"));
			}
			if (Config.getInstance().getBoolean("eslworking.remote.restart.enable")) {
				// 启动远程服务
				ssh.shell(Config.getInstance().getString("eslworking.remote.service.start.script"));
				logger.info( Config.getInstance().getString("eslworking.remote.service" + " is started!!!"));
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	@SuppressWarnings("resource")
	public void end() throws Exception {
		String host = Config.getInstance().getString("eslworking.remote.ip");
		String user = Config.getInstance().getString("eslworking.remote.user");
		String password = Config.getInstance().getString("eslworking.remote.password");
		String port = Config.getInstance().getString("eslworking.remote.port");
		// 关闭远程服务
		if (Config.getInstance().getBoolean("eslworking.remote.restart.enable")) {
			try(SSH2Tools ssh = new SSH2Tools().connect(host, Integer.valueOf((port == null || port.isEmpty()) ?"22" : port), user, password)) {		
		    	ssh.shell(Config.getInstance().getString("eslworking.remote.service.stop.script"));
		    	logger.info( Config.getInstance().getString("eslworking.remote.service" + " is stoped!!!"));
			} catch (Exception e) {
				throw e;
			}
		}
		ApPcieConfig ap = new ApPcieConfig();
		ap.setEslworkingIp(Config.getInstance().getString("ap.eslworking.ip"));
		ap.setEslworkingPort(Config.getInstance().getString("ap.eslworking.port"));
		ap.exec();
		//开启本机服务
		if (Config.getInstance().getBoolean("eslworking.local.service.stop")) {
			try {
				new SystemCmdManager().start(Config.getInstance().getString("eslworking.local.service"), 600);
			} catch (IOException | InterruptedException | TimeoutException e) {
				logger.error("close local eslworking service failed, suspend!!!");
				logger.error(e.getMessage(), e);
				return;
			}
		}
	}
	
	/**
	 * 开始定位
	 * @throws IOException
	 */
	public void startLocation() throws IOException {
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{}");
		Request request = new Request.Builder().url(Config.getInstance().getString("location.url")).post(body).build();
		try {
			Response response = client.newCall(request).execute();
			JSONObject jo = (JSONObject) JSONObject.parse(response.body().string());
			if (jo.getIntValue("errno") == 0) {
				logger.info("start location!!!");
			}
		} catch (SocketTimeoutException e) {
			logger.error(e.getMessage(), e);
		} catch (ConnectException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		
	}
}
