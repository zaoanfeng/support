package com.hanshow.upgrade.ap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(ApConfig.class);
	
	public static void main(String[] args) {
		if (args.length <= 0) {
			logger.warn("ApConfig input args error, please input 'ap2' or 'pcie'");
		}
		if (args[0].equals("ap2")) {
			new com.hanshow.support.upgrade.ap.Ap2Config().exec();
		} else if (args[0].equals("pcie")) {
			new com.hanshow.support.upgrade.ap.ApPcieConfig().exec();
		}
		
	}
}
