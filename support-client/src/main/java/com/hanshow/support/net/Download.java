package com.hanshow.support.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Download {

	public static void downloadFile(String url, String savePath, DownloadCallback callback) {
		File saveDir = new File(savePath);
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}
		Request request = new Request.Builder().url(url).build();
		new OkHttpClient().newBuilder()
				.connectTimeout(60, TimeUnit.SECONDS)
				.readTimeout(1800, TimeUnit.SECONDS)
				.build().newCall(request).enqueue(new Callback() {
					
					@Override
					public void onResponse(Call call, Response response) throws IOException {
						// TODO Auto-generated method stub	
						byte[] buffer = new byte[1024];
						int len;
						String fileName  = url.substring(url.lastIndexOf("/"), url.length());
						File file = new File(saveDir, fileName);				
			            try (InputStream inputStream = response.body().byteStream();
			            		FileOutputStream fileOutputStream = new FileOutputStream(file)) {        
			            	while ((len = inputStream.read(buffer)) != -1) {
			                    fileOutputStream.write(buffer, 0, len);
			                    fileOutputStream.flush();
			                }
			            } catch (Exception e) {
			                throw e;
			            } 
						callback.finish();
					}
					
					@Override
					public void onFailure(Call call, IOException e) {
						// TODO Auto-generated method stub
						callback.failed();
					}
				});
	}
}
