package com.hanshow.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.hanshow.support.scheduled.TaskScheduled;

@SpringBootApplication
public class Application {

	@Autowired
	private static TaskScheduled taskScheduled;
	//private static final Logger logger = LoggerFactory.getLogger(Application.class);
	
	public static void main(String[] args) {
		new SpringApplicationBuilder().sources(Application.class).web(WebApplicationType.NONE).run(args);
		// 新建定时任务
		taskScheduled = (TaskScheduled) SpringUtil.getBean(TaskScheduled.class);
		taskScheduled.run();
		// 获取一个socket连接
		/*new Thread(new Runnable() {	
			@Override
			public void run() {
				try {
					WebSocketClient.getWebSocket();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}).start();*/
	}
	
	
}
