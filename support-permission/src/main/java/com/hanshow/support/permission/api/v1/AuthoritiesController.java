package com.hanshow.support.permission.api.v1;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanshow.support.permission.model.Authorities;
import com.hanshow.support.permission.service.AuthoritiesService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/authorities")
@Slf4j
public class AuthoritiesController {

	@Autowired
	private AuthoritiesService authoritiesService;
	
	@RequestMapping(method=RequestMethod.POST)
    public HttpEntity<Void> insert(@RequestBody String[] authors, @RequestParam String clientId) {
		try {
			List<Authorities> list = new ArrayList<>();
			for(String authority : authors) {
				Authorities authorities = new Authorities();
				authorities.setAuthority(authority);
				authorities.setClientId(clientId);
				authorities.setDescription("");
				list.add(authorities);
			}
			authoritiesService.insert(list);
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
    }
}
