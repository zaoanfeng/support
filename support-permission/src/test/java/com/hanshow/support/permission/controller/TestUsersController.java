package com.hanshow.support.permission.controller;

import java.util.Map;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import com.hanshow.support.permission.model.Users;

public class TestUsersController {

	private RestTemplate restTemplate = new RestTemplate();
	
	@Test
	public void insert() {
		String url = "http://localhost:8080/user/register";
		
		Users user = new Users();
		user.setUsername("002");
		user.setPassword("123546");
		user.setEnabled(true);
		user.setLocale("zh_CN");
		user.setAdmin(true);

		System.out.println(restTemplate.postForObject(url, user, Map.class));
	}
}