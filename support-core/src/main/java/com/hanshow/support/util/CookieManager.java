package com.hanshow.support.util;

import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;

public class CookieManager {

	private static HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
	
	public static void setCookie(String key, List<Cookie> cookies) {
		cookieStore.put(key, cookies);
	}
	
	public static List<Cookie> getCookie(String key) {
		return cookieStore.get(key);
	}
	
	public static void removeCookie(String key) {
		cookieStore.remove(key);
	}
}
