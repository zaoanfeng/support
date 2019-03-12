package com.hanshow.support.permission.service.impl;

import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.hanshow.support.permission.model.Users;
import com.hanshow.support.permission.service.UsersService;

@Service
public class UsersServiceImpl extends BaseServiceImpl<Users, String> implements UsersService {

	@Override
	public boolean updateBySelective(Users user) throws SQLException {
		return super.updateBySelective(user, user.getUsername());
	}

}
