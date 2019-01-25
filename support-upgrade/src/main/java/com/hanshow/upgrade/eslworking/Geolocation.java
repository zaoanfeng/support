package com.hanshow.upgrade.eslworking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Geolocation {

	private static final Logger logger = LoggerFactory.getLogger(Geolocation.class);
	
	public static void main(String[] args) {
		if (args.length < 2) {
			logger.warn("Geolocation input args error");
		}
		//
		
		if (args[0].equals("ap2")) {
			new com.hanshow.support.upgrade.ap.Ap2Config().exec();
		} else if (args[0].equals("pcie")) {
			new com.hanshow.support.upgrade.ap.ApPcieConfig().exec();
		}
		
		new com.hanshow.support.upgrade.eslworking.CopyTemplate().exec(args[0]);
		
		
	}
}
