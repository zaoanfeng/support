package com.hanshow.support.permission;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

	@Value("${http.allowed.origin}")
	private boolean allowedOrigin;

	
	@Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
        .antMatchers( "/v1", "/v1/users/register").permitAll()
        .antMatchers("/**").authenticated()
        .anyRequest().authenticated();
    }
	
	@Bean  
    public CorsFilter corsFilter() {  
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();  
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        if (allowedOrigin) {
        	corsConfiguration.addAllowedOrigin("*");
        	corsConfiguration.addAllowedHeader("*");
        	corsConfiguration.addAllowedMethod("*");
        }
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
}
