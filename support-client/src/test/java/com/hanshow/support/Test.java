package com.hanshow.support;

import java.io.IOException;
import java.net.ConnectException;

import com.sun.mail.util.SocketConnectException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Test {

	public static void main(String[] args) {
		/*for(int i=0;i<10;i++) {
			if (i==5) {
				
				return;
			}
			System.out.println(i);
		}*/
		try {
		httpTest();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	private static void httpTest() throws IOException {
		Request request = new Request.Builder().url("http://127.0.0.1:9000").build();
		Call call = new OkHttpClient().newCall(request);
		try {
			StringBuffer sb = null;
			sb.toString();
			Response r = call.execute();
		} catch (IOException e) {
			throw e;
		}
	}
}
