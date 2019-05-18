package com.hanshow.support.server.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Service
public class MyFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

	private static final String URL_AUTHORITY_MAPPING_FILE_NAME = "url-authority.json";
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private  Map<String,String> urlRoleMap = new HashMap<String,String>();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private void getMapping() {
    	try(InputStream input = this.getClass().getResourceAsStream(URL_AUTHORITY_MAPPING_FILE_NAME)){
    		byte[] buffer = new byte[input.available()];
        	input.read(buffer);
        	String content = new String(buffer);
        	JSONArray jArray = JSONArray.parseArray(content);
        	for (Object jo : jArray) {
        		urlRoleMap.put(((JSONObject)jo).getString("url"),((JSONObject)jo).getString("authority"));
        	}
    	} catch (IOException e) {
    		logger.error(e.getMessage(), e);
		}
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        FilterInvocation fi = (FilterInvocation) object;
        String url = fi.getRequestUrl();
//        String httpMethod = fi.getRequest().getMethod();
        getMapping();
        for(Map.Entry<String,String> entry:urlRoleMap.entrySet()){
            if(antPathMatcher.match(entry.getKey(),url)){
                return SecurityConfig.createList(entry.getValue());
            }
        }
        //没有匹配到,默认是要登录才能访问
        //return SecurityConfig.createList("ROLE_USER");
        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
