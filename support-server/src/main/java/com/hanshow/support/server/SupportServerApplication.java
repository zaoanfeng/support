package com.hanshow.support.server;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.builder.SpringApplicationBuilder;


@SpringBootApplication
@EnableOAuth2Sso
public class SupportServerApplication {

	//private static final Logger logger = LoggerFactory.getLogger(Application.class);
	
	public static void main(String[] args) {
		new SpringApplicationBuilder().sources(SupportServerApplication.class).web(WebApplicationType.SERVLET).run(args);		
	}
}
