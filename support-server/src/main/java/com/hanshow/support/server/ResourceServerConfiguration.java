package com.hanshow.support.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
	@Override
	public void configure(HttpSecurity http) throws Exception {

		http.formLogin().loginPage("/login").loginProcessingUrl("/login/form").failureUrl("/login-error").loginProcessingUrl("/user").permitAll();

		http.authorizeRequests().antMatchers("/product/**").hasRole("USER");
		http.authorizeRequests().antMatchers("/order/**").hasRole("ADMIN");

	}

}
