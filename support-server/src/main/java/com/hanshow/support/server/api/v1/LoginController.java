package com.hanshow.support.server.api.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


@RestController
@RequestMapping("/v1")
public class LoginController {

	@Value("${security.oauth2.client.accessTokenUri}")
	private String accessTokenUri;
	@Value("${security.oauth2.client.clientId}")
	private String clientId;
	@Value("${security.oauth2.client.clientSecret}")
	private String clientSecret;
	@Value("${security.oauth2.client.grantType}")
	private String grantType;
	private String scope = "write";
	
	@PostMapping(value="/login")
	public HttpEntity<JSONObject> userInfo(@RequestBody String user) {	
		JSONObject jo = (JSONObject) JSON.parse(user);
		HttpHeaders headers = new HttpHeaders();
		//  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		//  封装参数，千万不要替换为Map与HashMap，否则参数无法传递
		MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
		params.add("username", jo.getString("username"));
		params.add("password", jo.getString("password"));
		params.add("grant_type", grantType);
		params.add("scope", scope);	
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, headers);
		ResponseEntity<String> response = new RestTemplate().postForEntity(accessTokenUri, requestEntity, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			return ResponseEntity.ok(JSONObject.parseObject(response.getBody()));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}
}
