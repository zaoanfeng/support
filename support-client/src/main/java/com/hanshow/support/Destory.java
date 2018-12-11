package com.hanshow.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import com.hanshow.support.websocket.WebSocketClient;

@Component
public class Destory implements DisposableBean, ExitCodeGenerator {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
    @Override
    public int getExitCode() {
        return 5;
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("<<<<<<<<<<<.........Bye-Bye...........>>>>>>>>>>>>>>>");
        if (WebSocketClient.getWebSocket() != null) {
        	try {
        		WebSocketClient.getWebSocket().close(1000, "bye-bye");
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
        }
    }

}
