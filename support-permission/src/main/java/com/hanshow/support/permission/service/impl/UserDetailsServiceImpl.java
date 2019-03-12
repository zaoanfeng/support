package com.hanshow.support.permission.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.hanshow.support.permission.dao.AuthoritiesRepository;
import com.hanshow.support.permission.dao.UsersRepository;
import com.hanshow.support.permission.model.Authorities;
import com.hanshow.support.permission.model.Users;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UsersRepository usersRepository;
	@Autowired
	private AuthoritiesRepository authoritiesRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// 获取用户
		Users users = usersRepository.findById(username).get();
		//获取当前用户的权限
		Authorities authorities = new Authorities();
		authorities.setUsername(username);
		List<Authorities> authoritiesList = authoritiesRepository.findAll(Example.of(authorities));
		
		return new User(users.getUsername(), users.getPassword(), authoritiesList);
	}
}
