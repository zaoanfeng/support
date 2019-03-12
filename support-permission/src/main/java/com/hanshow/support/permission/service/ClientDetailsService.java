package com.hanshow.support.permission.service;

import java.sql.SQLException;

import com.hanshow.support.permission.model.ClientDetails;
public interface ClientDetailsService extends BaseService<ClientDetails, String> {

	boolean updateBySelective(ClientDetails client) throws SQLException;
}
