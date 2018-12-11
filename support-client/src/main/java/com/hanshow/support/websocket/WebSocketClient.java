package com.hanshow.support.websocket;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hanshow.support.monitor.Config;
import com.hanshow.support.net.Download;
import com.hanshow.support.net.DownloadCallback;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient {
	
	private static WebSocket webSocketClient = null;
	private static final Logger logger = LoggerFactory.getLogger(WebSocketClient.class);
	
	private WebSocketClient() {}
	
	public static WebSocket getWebSocket() {
		if (webSocketClient == null) {
			createWebSocket();
		}
		return webSocketClient;
	}
	
	/**
	 * 创建socket连接
	 */
	private static void createWebSocket() {
		try {
			String url = Config.getInstance().getString("server.address") + "/websocket/" + Config.getInstance().getString("user.store");
			// http或https转成ws请求
			url = url.replace("https", "ws").replace("http", "ws");
			Request request = new Request.Builder().url(url).build();
			new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(180, TimeUnit.SECONDS).build().newWebSocket(request, new WebSocketListener() {
				@Override
				public void onOpen(WebSocket webSocket, Response response) {
					webSocketClient = webSocket;
					webSocket.send("hello");
				}
				
				@Override
				public void onMessage(WebSocket webSocket, String text) {
					System.out.println("receive: " + text);
					if (!text.endsWith(".zip")) {
						return;
					}
					Download.downloadFile(Config.getInstance().getString("server.address") + "/" + text, System.getProperty("user.dir") + File.separator + "download", new DownloadCallback() {
						
						@Override
						public void finish() {
							// TODO Auto-generated method stub
							System.out.println("下载成功");
						}
						
						@Override
						public void failed() {
							// TODO Auto-generated method stub
							System.out.println("下载失败");
						}
					});
					/*switch(text) {
						//TODO 根据指令做相应的应用
					ca
						Download.downloadFile("", savePath, callback);
					}*/
				}
				
				@Override
				public void onClosed(WebSocket webSocket, int code, String reason) {
					if (code != 1000) {
						createWebSocket();
					}
				}
			});
		} catch (Exception e) {
			if (e instanceof SocketTimeoutException) {
				
			} else if (e instanceof ConnectException) {
				try {
					Thread.sleep(300 * 1000);
				} catch (InterruptedException e1) {
					logger.error(e.getMessage(), e);
				}
			}
			if (webSocketClient != null) {
				webSocketClient.close(1000, "bye-bye");
			}
			createWebSocket();
		}
	}
}
