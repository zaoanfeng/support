package com.hanshow.support.permission.controller;

import java.sql.SQLException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanshow.support.permission.api.result.ApiResult;
import com.hanshow.support.permission.api.result.ApiResult.Status;
import com.hanshow.support.permission.api.result.DataApiResult;
import com.hanshow.support.permission.model.Users;
import com.hanshow.support.permission.service.UsersService;

@RestController
@RequestMapping("/users")
public class UsersController {

	@Autowired
	private UsersService usersService;
	
	@RequestMapping(value="register", method=RequestMethod.POST)
	public ApiResult insert(@RequestBody Users user) throws SQLException {
		user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
		user.setCreateDate(new Date());
		if(!usersService.existsById(user.getUsername())) {
			if (usersService.insert(user)) {
				return ApiResult.success();
			} else {
				return ApiResult.fail();
			}	
		} else {
			return new ApiResult(Status.USER_NOT_EXISTS);
		}
	}
	
	@RequestMapping(method=RequestMethod.PATCH)
	public ApiResult update(@RequestBody Users user) throws SQLException {
		if(!usersService.existsById(user.getUsername())) {
			if (usersService.updateBySelective(user)) {
				return ApiResult.success();
			} else {
				return ApiResult.fail();
			}	
		} else {
			return new ApiResult(Status.USER_EXISTS);
		}
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public DataApiResult query(@RequestParam(value="offset") int page, @RequestParam(value="offset") int size) {
		return DataApiResult.success(usersService.queryForPage(page, size));
	}
}
