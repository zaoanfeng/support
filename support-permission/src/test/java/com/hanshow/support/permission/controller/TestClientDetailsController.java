package com.hanshow.support.permission.controller;

import java.util.Map;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import com.hanshow.support.permission.model.ClientDetails;

public class TestClientDetailsController {

	private RestTemplate restTemplate = new RestTemplate();
	
	@Test
	public void insert() {
		
		String url = "http://localhost:8080/client/register";
		
		ClientDetails client = new ClientDetails();
		client.setClientId("001");
		client.setClientSecret("123456");
		client.setResourceIds("oauth2-resource");
		client.setScope("read,write");
		client.setAuthorizedGrantTypes("authorization_code,refresh_token");
		client.setAccessTokenValidity(1800);
		client.setRefreshTokenValidity(7200);
		System.out.println(restTemplate.postForObject(url, client, Map.class));
	}
	
	@Test
	public void patch() {
		
		String url = "http://localhost:8080/client";
		
		ClientDetails client = new ClientDetails();
		client.setClientId("001");
		client.setScope("write");
		client.setAccessTokenValidity(1200);
		client.setRefreshTokenValidity(7200);
		restTemplate.put(url, client, Map.class);
	}
}
