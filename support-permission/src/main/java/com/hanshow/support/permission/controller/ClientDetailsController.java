package com.hanshow.support.permission.controller;

import java.sql.SQLException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hanshow.support.permission.api.result.ApiResult;
import com.hanshow.support.permission.api.result.ApiResult.Status;
import com.hanshow.support.permission.model.ClientDetails;
import com.hanshow.support.permission.service.ClientDetailsService;

@RestController
@RequestMapping("/clients")
public class ClientDetailsController  {

	@Autowired
	private ClientDetailsService clientDetailsService;
	
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public ApiResult insert(@RequestBody ClientDetails clientDetails) throws SQLException {
		clientDetails.setClientSecret(new BCryptPasswordEncoder().encode(clientDetails.getClientSecret()));
		clientDetails.setCreateDate(new Date());
		if(!clientDetailsService.existsById(clientDetails.getClientId())) {
			if (clientDetailsService.insert(clientDetails)) {
				return ApiResult.success();
			} else {
				return ApiResult.fail();
			}	
		} else {
			return new ApiResult(Status.USER_EXISTS);
		}
	}
	
	@RequestMapping(method=RequestMethod.PUT)
	public ApiResult update(@RequestBody ClientDetails clientDetails) throws SQLException {
		if(clientDetailsService.existsById(clientDetails.getClientId())) {
			if (clientDetailsService.updateBySelective(clientDetails)) {
				return ApiResult.success();
			} else {
				return ApiResult.fail();
			}	
		} else {
			return new ApiResult(Status.USER_NOT_EXISTS);
		}
	}
}
