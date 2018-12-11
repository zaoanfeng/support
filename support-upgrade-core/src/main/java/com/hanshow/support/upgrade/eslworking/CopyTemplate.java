package com.hanshow.support.upgrade.eslworking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hanshow.support.util.SystemCmdManager;

public class CopyTemplate {
	private static final Logger logger = LoggerFactory.getLogger(CopyTemplate.class);

	public void exec(String fileName) {
		String dir = System.getProperty("user.dir");
		if (fileName == null) {
			System.out.println("undefined file name!");
			return;
		}
		File file = new File(dir, fileName);
		if (!file.exists()) {
			System.out.println("'" + fileName + "' can not found");
			return;
		}
		List<String[]> stores = new ArrayList<String[]>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] str = line.split(",");
				stores.add(str);
			}

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		SystemCmdManager manager = new SystemCmdManager();
		for (String[] store : stores) {
			new Thread(new Runnable() {
				public void run() {
					String cmd = null;
					try {
						cmd = "net use \\\\" + store[0] + "\\ipc$ " + store[2] + " /user:" + store[1];
						manager.shell(cmd, 600);
						System.out.println(store[0] + "store connection success");
						cmd = "xcopy " + store[3] + " \\\\" + store[0] + "\\" + store[4].replace(":", "$") + " /S /Y";
						manager.shell(cmd, 600);
						System.out.println(store[0] + " copy file success");
						try {
							cmd = "net use \\\\" + store[0] + "\\ipc$ /delete";
							manager.shell(cmd, 600);
						} catch (IOException | InterruptedException | TimeoutException e) {
							System.out.println(e.getMessage());
							CopyTemplate.logger.error(e.getMessage(), e);
						}
						System.out.println(store[0] + " remote close");
					} catch (IOException | InterruptedException | TimeoutException e) {
						System.out.println(e.getMessage());

					}
				}
			}).start();
		}
	}
}
