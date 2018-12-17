package com.hanshow.support.monitor;

import com.hanshow.support.monitor.mail.ServiceStatus;
import com.hanshow.support.util.Config;
import com.hanshow.support.util.SystemCmdManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monitor {
	private static final Logger logger = LoggerFactory.getLogger(Monitor.class);

	public List<ServiceStatus> serviceMonitor() {
		List<ServiceStatus> serviceStatusList = new ArrayList<>();
		String[] serviceNames = Config.getInstance().getString("monitor.service.name").split(",");
		for (String serviceName : serviceNames) {
			try {
				if (!new SystemCmdManager().status(serviceName)) {
					if (new SystemCmdManager().start(serviceName, 600)) {
						if (new SystemCmdManager().status(serviceName)) {
							logger.info(serviceName + " start success");
							serviceStatusList.add(new ServiceStatus(serviceName, ServiceStatus.STARTED));
						} else {
							logger.info(serviceName + " start failed!");
							serviceStatusList.add(new ServiceStatus(serviceName, ServiceStatus.STOPED));
						}
					} else {
						logger.info(serviceName + " start failed!");
						serviceStatusList.add(new ServiceStatus(serviceName, ServiceStatus.STOPED));
					}
				}
			} catch (IOException | InterruptedException | TimeoutException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return serviceStatusList;
	}

	public String logMonitor() {
		if ((Config.getInstance().getProperties("log.path") != null)
				&& (Config.getInstance().getBoolean("mail.enable").booleanValue())) {
			String[] paths = Config.getInstance().getString("log.path").split(",");
			String content = "";
			for (String path : paths) {
				try {
					content = content + new WatchLog().watch(path) + "\n";
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if ((content != null) && (!content.equals(""))) {
				return content;
			}
			return null;
		}
		return null;
	}
}
