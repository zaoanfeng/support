package com.hanshow.support.permission.service;

import java.sql.SQLException;

import com.hanshow.support.permission.model.Users;

public interface UsersService extends BaseService<Users, String> {

	boolean updateBySelective(Users user) throws SQLException;
}
