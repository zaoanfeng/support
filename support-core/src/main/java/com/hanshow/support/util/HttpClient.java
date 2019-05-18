package com.hanshow.support.util;

import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509ExtendedTrustManager;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

public class HttpClient {

	public OkHttpClient getHttpClient(boolean isHttps) {
		try {
			Builder builder = new OkHttpClient().newBuilder();
			if (isHttps) {
				class X509TrustManager extends X509ExtendedTrustManager {
					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
					}

					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
					}

					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {
						// TODO Auto-generated method stub

					}

					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1, SSLEngine arg2) throws CertificateException {
						// TODO Auto-generated method stub

					}

					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {
						// TODO Auto-generated method stub

					}

					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1, SSLEngine arg2) throws CertificateException {
						// TODO Auto-generated method stub

					}

					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new java.security.cert.X509Certificate[] {};
					}
				};

				final SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, new X509TrustManager[] { new X509TrustManager() }, new SecureRandom());
				final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

				HostnameVerifier hostnameVerifier = new HostnameVerifier() {

					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}

				};
				builder.sslSocketFactory(sslSocketFactory, new X509TrustManager()).hostnameVerifier(hostnameVerifier);
			}
			builder.connectTimeout(60, TimeUnit.SECONDS).writeTimeout(300, TimeUnit.SECONDS).readTimeout(300, TimeUnit.SECONDS).cookieJar(new CookieJar() {

				@Override
				public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
					CookieManager.setCookie(httpUrl.host(), list);
				}

				@Override
				public List<Cookie> loadForRequest(HttpUrl httpUrl) {
					List<Cookie> cookies = CookieManager.getCookie(httpUrl.host());
					return cookies != null ? cookies : new ArrayList<Cookie>();
				}
			});	
			return builder.build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
