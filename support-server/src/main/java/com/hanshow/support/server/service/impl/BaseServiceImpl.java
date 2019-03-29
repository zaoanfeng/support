package com.hanshow.support.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hanshow.support.server.mapper.BaseMapper;
import com.hanshow.support.server.mybatis.Pages;
import com.hanshow.support.server.service.BaseService;

@Service
public class BaseServiceImpl<T, ID> implements BaseService<T, ID> {

	Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private BaseMapper<T, ID> baseMapper;
	
	@Override
	public boolean insert(T t) {
		return baseMapper.insert(t) > 0 ? true : false;
	}

	@Override
	public boolean updateById(T t, ID id) {
		return baseMapper.updateById(id, t) > 0 ? true : false;
	}

	@Override
	public boolean updateSelectiveById(T t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteById(ID id) {
		return baseMapper.deleteById(id) > 0 ? true : false;
	}

	@Override
	public boolean delete(T t) {
		return baseMapper.delete(t) > 0 ? true : false;
	}
	
	@Override
	public T queryById(ID id) {
		// TODO Auto-generated method stub
		return baseMapper.selectById(id);
	}

	@Override
	public Pages<T> queryForPage(T t, int offset, int limit) {
		// TODO Auto-generated method stub
		return baseMapper.selectForPage(t, offset, limit);
	}
}
