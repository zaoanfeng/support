package com.hanshow.support.server.api.v1;


import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
public class UsersController {
	
	@GetMapping("/info")
	public HttpEntity<Map> userInfo(Principal principal) {
		Map<String, Object> map = new HashMap<>();
		map.put("uesrname", principal.getName());
		map.put("roles", new String[] {"admin"});
		return ResponseEntity.ok(map);
	}
}
