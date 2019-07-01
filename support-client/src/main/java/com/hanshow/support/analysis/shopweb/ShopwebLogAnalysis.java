package com.hanshow.support.analysis.shopweb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hanshow.support.log.LogAnalysis;
import com.hanshow.support.model.Store;
import com.hanshow.support.service.StoreService;
import com.hanshow.support.util.Config;

@Service
public class ShopwebLogAnalysis {
	
	private static final String LOG_PATH = "/logs";
	private static final String[] LOG_LIST = new String[] {};
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private StoreService storeService;

	public void analysis() throws IOException {
		String shopwebUrl = Config.getInstance().getString("monitor.shopweb.path");
		if (shopwebUrl != null) {
			File file = new File(shopwebUrl);
			if (!file.exists()) {
				throw new FileNotFoundException(shopwebUrl + "cannot found!");
			}
		} else {
			throw new FileNotFoundException(shopwebUrl + "cannot found!");
		}
		File logDir = new File(shopwebUrl + LOG_PATH);
		if (logDir.exists()) {
			for(File logFile : logDir.listFiles()) {
				if (Arrays.asList(LOG_LIST).contains(logFile.getName())) {
					Store store = new Store();
					store.setLogPath(logFile.getPath());
					store = storeService.queryAll(store).stream().findFirst().orElse(new Store());
					long position = 0;
					store.getSeek();
					logger.debug("position=" + store.getSeek());
					logger.debug("file.length()=" + logFile.length());
					if(logFile.length() > store.getSeek()) {
						position = store.getSeek();
					}
					List<String> list = new ArrayList<>();
					LogAnalysis.analysisLogBack(logFile, position, list);
				}
			}
		}
	}
}
