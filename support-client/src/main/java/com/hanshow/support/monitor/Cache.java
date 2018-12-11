package com.hanshow.support.monitor;

import java.util.HashMap;
import java.util.Map;

public class Cache {

	private static Map<String, Object> CACHE = new HashMap<>();
	
	public static void set(String key, Object value) {
		CACHE.put(key, value);
	}
	
	public static Object get(String key) {
		return CACHE.get(key);
	} 
}
