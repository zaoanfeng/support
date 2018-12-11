package com.hanshow.support.monitor;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

	private static Config config;
	private Map<String, Object> properties;
	private Logger logger = LoggerFactory.getLogger(getClass());

	private Config() {
		properties = new HashMap<>();
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class).configure(params.properties().setFileName("config.properties").setEncoding("UTF-8"));
		try {
			Configuration config = builder.getConfiguration();
			Iterator<String> iter = config.getKeys();
			while (iter.hasNext()) {
				String key = iter.next();
				properties.put(key, config.getProperty(key));
			}
		} catch (ConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static Config getInstance() {
		if (config == null) {
			config = new Config();
		}
		return config;
	}
	
	/**
	 * 获取对象类型数据，获取不到返回null
	 * @param key
	 * @return String
	 */
	public Object getProperties(String key) {
		return properties.getOrDefault(key, null);
	}
	
	/**
	 * 获取数字类型数据，获取不到返回0
	 * @param key
	 * @return String
	 */
	public Integer getInt(String key) {
		return Integer.valueOf(properties.getOrDefault(key, 0).toString());
	}
	
	/**
	 * 获取字符串类型数据，获取不到返回空字符串（""）
	 * @param key
	 * @return String
	 */
	public String getString(String key) {
		return properties.getOrDefault(key, "").toString();
	}
	
	/**
	 * 获取bool类型数据，获取不到返回false
	 * @param key
	 * @return String
	 */
	public Boolean getBoolean(String key) {
		return Boolean.valueOf(properties.getOrDefault(key, "false").toString());
	}
}
