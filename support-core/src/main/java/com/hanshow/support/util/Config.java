package com.hanshow.support.util;

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
	private Configuration properties;
	private Logger logger = LoggerFactory.getLogger(getClass());

	private Config() {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class).configure(params.properties().setFileName("config.properties").setEncoding("utf-8"));
		try {
			this.properties = builder.getConfiguration();
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
	 * 
	 * @param key
	 * @return String
	 */
	public Object getProperties(String key) {
		return properties.getProperty(key);
	}

	/**
	 * 获取数字类型数据，获取不到返回0
	 * 
	 * @param key
	 * @return String
	 */
	public Integer getInt(String key) {
		return properties.getInteger(key, 0);
	}

	/**
	 * 获取字符串类型数据，获取不到返回空字符串
	 * 
	 * @param key
	 * @return String
	 */
	public String getString(String key) {
		return properties.getString(key, "");
	}

	/**
	 * 获取bool类型数据，获取不到返回false
	 * 
	 * @param key
	 * @return String
	 */
	public Boolean getBoolean(String key) {
		return properties.getBoolean(key, false);
	}

	/**
	 * 获取数组类型数据，以，分隔
	 * 
	 * @param key
	 * @return String
	 */
	public String[] getStringArray(String key) {
		return properties.getString(key).split(",");
	}
}
