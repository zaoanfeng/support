package com.hanshow.support.permission.service.impl;

import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.hanshow.support.permission.model.ClientDetails;
import com.hanshow.support.permission.service.ClientDetailsService;

@Service
public class ClientDetailsServiceImpl extends BaseServiceImpl<ClientDetails, String> implements ClientDetailsService {

	@Override
	public boolean updateBySelective(ClientDetails client) throws SQLException {
		return super.updateBySelective(client, client.getClientId());
	}

}