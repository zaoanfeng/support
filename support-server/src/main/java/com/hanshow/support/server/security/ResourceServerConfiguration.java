package com.hanshow.support.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableResourceServer
//@EnableWebSecurity
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

	@Value("${http.allowed.origin}")
	private boolean allowedOrigin;
	
	@Autowired
    private MyFilterSecurityInterceptor myFilterSecurityInterceptor;

	@Override
	public void configure(HttpSecurity http) throws Exception {
		/*http.authorizeRequests().antMatchers("/v1/users/**").hasAuthority("ROLE_USER");
		http.authorizeRequests().antMatchers("/v1/store/**").hasAut hority("STORE");*/
		http.authorizeRequests().antMatchers("/v1/login", "/v1/search/**").permitAll()
		.and().authorizeRequests().anyRequest().authenticated()
		.and().formLogin().loginPage("/login.html").failureUrl("/login-error").permitAll();
		http.addFilterBefore(myFilterSecurityInterceptor, FilterSecurityInterceptor.class);
	}

	@Bean
	public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext, OAuth2ProtectedResourceDetails details) {		
		return new OAuth2RestTemplate(details, oauth2ClientContext);
	}

    @Bean  
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();  
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        if (allowedOrigin) {
        	corsConfiguration.addAllowedOrigin("*");
        	corsConfiguration.addAllowedHeader("*");
        	corsConfiguration.addAllowedMethod("*");
        	corsConfiguration.addExposedHeader("Authorization");
        	corsConfiguration.addExposedHeader("Content-Range");//这里是需要额外配置的header内容
        }
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
}
