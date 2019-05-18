package com.hanshow.support.upgrade.ap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hanshow.support.util.Config;
import com.hanshow.support.util.HttpClient;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpgradeApByHttp {

	
	private static final String FILE_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private final static String AP_PROTOCOL;
	private final static int AP_PORT;
	private String url = "";
	private String ip = "";
	static {
		AP_PROTOCOL = Config.getInstance().getString("ap.web.protocol").isEmpty() ? "http" : Config.getInstance().getString("ap.web.protocol");
		AP_PORT = Config.getInstance().getInt("ap.web.port") == 0 ? 80 : Config.getInstance().getInt("ap.web.port");
	} 

	public void upgrade(String ip) throws IOException {
		this.ip = ip;
		this.url = AP_PROTOCOL + "://" + ip + ":" + AP_PORT + "/";
		String path = System.getProperty("user.dir");
		File file = new File(path, "appack.bin");
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException("Cannot found file at " + file.getPath());
		}
		// 先登录
		login();
		// 上传文件
		upload(file);
		// 执行提交
		executeUpgrade();
	}

	/**
	 * 登录
	 * @throws IOException
	 */
	private void login() throws IOException {

		FormBody.Builder formbody = new FormBody.Builder();
		formbody.add("login_pwd", "admin");
		Request request = new Request.Builder().url(url + "login").post(formbody.build()).build();
		Response response = new HttpClient().getHttpClient(request.isHttps()).newCall(request).execute();
		if (!response.message().equals("OK")) {
			System.out.println(this.ip + " login failed!");
		}
		else {
			System.out.println(this.ip + " login success!");
		}
		response.close();
	}
	
	/**
	 * 上传升级包
	 * @param file
	 * @throws IOException
	 */
	private void upload(File file) throws IOException {
		RequestBody fileBody = RequestBody.create(MediaType.parse(FILE_CONTENT_TYPE), file);
		RequestBody requestBody = new MultipartBody.Builder().addFormDataPart("status", "prepare").addFormDataPart("file", file.getName(), fileBody).build();
		Request request = new Request.Builder().url(url + "upgrade").post(requestBody).build();
		
		Response response = new HttpClient().getHttpClient(request.isHttps()).newCall(request).execute();
		if (!response.message().equals("OK")) {
			System.out.println(this.ip + " " + file.getPath() + " upload file failed!");
		} else {
			System.out.println(this.ip + " " + file.getPath() + " upload file successed!");
		}
		response.close();
	}
	
	/**
	 * 执行升级命令
	 * @throws IOException
	 */
	private void executeUpgrade() throws IOException {
		RequestBody requestBody = new FormBody.Builder().add("status", "upgrade").build();
		Request request = new Request.Builder().url(url + "upgrade").post(requestBody).build();
		Response response = new HttpClient().getHttpClient(request.isHttps()).newCall(request).execute();
		if (!response.message().equals("OK")) {
			System.out.println(this.ip + " upgrade failed!");
		}
		else {
			System.out.println(this.ip + " ap upgrade successed!");
		}
		response.close();
	}	
}
