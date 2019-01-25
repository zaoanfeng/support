package com.hanshow.support.install;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ini4j.Wini;

import com.hanshow.support.util.DBUtils;
import com.hanshow.support.util.FileUtils;
import com.hanshow.support.util.SystemCmdManager;

public class Install extends Base {
	
	public void exec() {
		// 获取所有软件目录
		getFileDir();
		// 安装服务
		// 安装MySQL
		try {
			installMysql();
			System.out.println("Install MySQL service finished!!!");
		} catch (IOException | InterruptedException | TimeoutException e) {
			System.err.println("Install MySQL service failed!!!");
			System.err.println(e);
			System.exit(0);
		}
		// 安装Tomcat
		try {
			installTomcat();
			System.out.println("Install Tomcat service finished!!!");
		} catch (IOException | InterruptedException | TimeoutException e) {
			System.err.println("Install Tomcat service failed!!!");
			System.err.println(e);
			System.exit(0);
		}
		// 安装ESL-Working
		try {
			installEslworking();
			System.out.println("Install ESL-Working service finished!!!");
		} catch (IOException | InterruptedException | TimeoutException e) {
			System.err.println("Install ESL-Working service failed!!!");
			System.err.println(e);
			System.exit(0);
		}
		// 安装对接模块
		try {
			installIntegration();
			System.out.println("Install Integration service finished!!!");
		} catch (IOException | InterruptedException | TimeoutException e) {
			System.err.println("Install Integration service failed!!!");
			System.err.println(e);
			System.exit(0);
		}
		// TODO 初始化数据
		try {
			init();
			System.out.println("Init Data finished!!!");
		} catch (Exception e) {
			System.err.println("Init Data failed!!!");
			System.err.println(e);
			System.exit(0);
		}
		//将服务名写到配置文件中，在后续开、关服务中使用
		try {
			writeServiceNameInFile();
		} catch (IOException e) {
			System.err.println("Write Service Name File failed!!!");
			System.err.println(e);
		}
		// TODO 启动服务
		//startService();
		
		System.out.println("System install finished!!!");
	}
	
	/**
	 * 安装MySQL服务
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private void installMysql() throws IOException, InterruptedException, TimeoutException {
		String bin = new File(MYSQL_HOME, "bin").getPath();
		// 安装服务
		StringBuilder sb = new StringBuilder();
		sb.append(bin).append(File.separator).append("mysqld.exe").append(" ");
		sb.append("--install").append(" ");
		sb.append(MYSQL_SERVICE_NAME);
		new SystemCmdManager().shell(sb.toString(), TIMEOUT);
		// 配置数据目录
		Wini ini = new Wini(new File(MYSQL_HOME, "my.ini"));
		ini.getConfig().setLineSeparator("\n");
        ini.put("mysqld", "datadir", (MYSQL_HOME + File.separator + "Data").replaceAll("\\\\", "/"));
        ini.put("mysqld", "secure-file-priv", "\"" +(MYSQL_HOME + File.separator + "Uploads\"").replaceAll("\\\\", "/"));
        ini.store();
	}
	
	/**
	 *  安装tomcat服务
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private void installTomcat() throws IOException, InterruptedException, TimeoutException {
		String bin = new File(TOMCAT_HOME, "bin").getPath();
		// 备份一份service.bat文件
		File bak = new File(bin, "service.bat" + BAK_SUFFIX);
		File serviceFile = new File(bin, "service.bat");
		serviceFile.renameTo(bak);
		// 在service.bat文件中增加set JAVA_HOME
		try {
			if (!serviceFile.exists()) {
				serviceFile.createNewFile();
			}
		} catch (IOException e) {
			throw e;
		}
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(bak), "UTF-8"));
				BufferedWriter writer = new BufferedWriter(new FileWriter(serviceFile))) {
			String content = "";
			String line;
			while ((line = reader.readLine()) != null) {
				content += (line + "\n");
				if (line.equals("setlocal")) {
					content += ("\nset JRE_HOME=" + JAVA_HOME + File.separator + "jre" + "\n");
				}
			}
			writer.write(content);
		} catch(IOException e) {
			throw e;
		}	
		new SystemCmdManager().install(bin + File.separator + "service.bat", null, TOMCAT_SERVICE_NAME, TIMEOUT);
	}
	
	/**
	 *  安装ESL-Working服务
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private void installEslworking() throws IOException, InterruptedException, TimeoutException {
		String bin = new File(ESLWORKING_HOME, "bin").getPath();
		// 备份一份service.bat文件
		File bak = new File(bin, "service.bat" + BAK_SUFFIX);
		File serviceFile = new File(bin, "service.bat");
		serviceFile.renameTo(bak);
		// 在service.bat文件中增加set JAVA_HOME
		try {
			if (!serviceFile.exists()) {
				serviceFile.createNewFile();
			}
		} catch (IOException e) {
			throw e;
		}
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(bak), "UTF-8"));
				BufferedWriter writer = new BufferedWriter(new FileWriter(serviceFile))) {
			String content = "";
			String line;
			while ((line = reader.readLine()) != null) {		
				if (line.equals("set \"APP_HOME=%cd%\"")) {
					content += ("set JRE_HOME=" + JAVA_HOME + File.separator + "jre" + "\n");
				}
				content += (line +"\n");
			}
			writer.write(content);
		} catch(IOException e) {
			throw e;
		}
		new SystemCmdManager().install(bin + File.separator + "service.bat", null, "", TIMEOUT);
	}
	
	/**
	 * 安装对接模块
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private void installIntegration() throws IOException, InterruptedException, TimeoutException {
		String bin = new File(INTEGRATION_HOME, "bin").getPath();
		// 备份一份service.bat文件
		File bak = new File(bin, "install.bat" + BAK_SUFFIX);
		File serviceFile = new File(bin, "install.bat");
		Charset charset = FileUtils.getFileEncode(serviceFile);
		serviceFile.renameTo(bak);
		// 在service.bat文件中增加set JAVA_HOME
		try {
			if (!serviceFile.exists()) {
				serviceFile.createNewFile();
			}
		} catch (IOException e) {
			throw e;
		}
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(bak), charset.name()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(serviceFile), charset.name()))) {
			String content = "";
			String line;
			while ((line = reader.readLine()) != null) {
				//修改安装脚本里的basedir路径
				if (line.equals("set BASEDIR=%CD%")) {
					content += ("\n\rset BASEDIR=" + INTEGRATION_HOME + "\n");
				} else {
					content += (line +"\n\r");
				}
				//增加java_home的设置
				if (line.equals("@echo off")) {
					content += ("\n\rset JAVA_HOME=" + JAVA_HOME + "\n");
				}
				if (line.trim().startsWith("set SERVICE_EN_NAME=")) {
					INTEGRATION_SERVICE_NAME = line.substring(line.indexOf("set SERVICE_EN_NAME=") + 20);
				}
			}
			writer.write(content);
		} catch(IOException e) {
			throw e;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(bin).append(File.separator).append("install.bat");
		new SystemCmdManager().shell(sb.toString(), TIMEOUT);
	}
	
	/**
	 * 初始化数据，由于要操作mysql,会先启动服务
	 * @throws Exception
	 */
	private void init() throws Exception {
		//开启mysql服务 
		new SystemCmdManager().start(MYSQL_SERVICE_NAME, TIMEOUT);
		Map<String, String> config = FileUtils.readProperties(new File(CONFIG_DIR, "config.properties"));
		config.put("storeCode", config.getOrDefault("storeCode", "001"));
		config.put("customerStoreCode", config.getOrDefault("customerStoreCode", "001"));
		config.put("storeName", config.getOrDefault("storeName", "store1"));
		config.put("storeIpAddress", config.getOrDefault("eslworkingUrl", "http://127.0.0.1:9000"));
		config.put("shopwebName", config.getOrDefault("shopwebName", "shopweb"));
		config.put("shopwebPort", config.getOrDefault("shopwebPort", "8080"));
		//从Shopweb配置中读取mysql连接信息 
		Map<String, String> shopwebConfig = FileUtils.readProperties(new File(TOMCAT_HOME, "webapps/" + config.get("shopwebName") + "/WEB-INF/classes/config.properties"));
		DBUtils dbUtils = new DBUtils(shopwebConfig.get("db.url"), shopwebConfig.get("db.username"), shopwebConfig.get("db.password"));
		// 查询存不存在此门店编号的数据，存在跳过
		List<String> sqls = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(DATA_DIR, "store.sql")));) {
			String line;	
			Pattern p = Pattern.compile("(\\$\\{)([\\w]+)(\\})");
			while ((line = reader.readLine()) != null) {
				//将句子变量替换成值
				StringBuffer sb = new StringBuffer();
				Matcher m = p.matcher(line);
				while(m.find()) {
					String group = m.group(2);
			          //下一步是替换并且把替换好的值放到sb中
			          m.appendReplacement(sb, config.get(group));
				}
				m.appendTail(sb);
			    System.out.println(sb.toString());
				sqls.add(sb.toString());
			}
		}
		//执行sql
		dbUtils.execute(sqls.toArray(new String[] {}));
		//修改eslworking的url配置信息
		String content = "";
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(ESLWORKING_HOME, "config/config.properties")))) {	
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("api1.uplink.url=")) {
					content += "api1.uplink.url=" + "http://127.0.0.1:" + config.get("shopwebPort") + "/" + config.get("shopwebName") + "/" + config.get("storeCode") + "/stationHandler" + "\n";
				} else {
					content += line + "\n";
				}
			}
		}
		if (null != content && !content.equals("")) {
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(ESLWORKING_HOME, "config/config.properties")))) {
				writer.write(content);
			}
		}
	}
	
	/**
	 * 将服务写到配置文件中
	 * @throws IOException 
	 */
	private void writeServiceNameInFile() throws IOException {
		// TODO Auto-generated method stub
		File file = new File(BIN_DIR, SERVICE_NAME_FILE);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			throw e;
		}
		Properties prop = new Properties();
		try(OutputStream out = new FileOutputStream(file)) {
			prop.setProperty(TOMCAT_SERVICE_KEY, TOMCAT_SERVICE_NAME);
			prop.setProperty(ESLWORKING_SERVICE_KEY, ESLWORKING_SERVICE_NAME);
			prop.setProperty(INTEGRATION_SERVICE_KEY, INTEGRATION_SERVICE_NAME);
			prop.setProperty(MYSQL_SERVICE_KEY, MYSQL_SERVICE_NAME);
			prop.store(out, "service name");
		} catch (IOException e) {
			throw e;
		} 
	}
}
