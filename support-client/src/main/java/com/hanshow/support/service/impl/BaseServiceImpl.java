package com.hanshow.support.service.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import com.hanshow.support.dao.BaseRepository;
import com.hanshow.support.service.BaseService;

public class BaseServiceImpl<T, ID extends Serializable, R extends BaseRepository<T, ID>> implements BaseService<T, ID, R> {

	@Autowired
	private BaseRepository<T, ID> baseRepository;
	
	@Override
	public boolean insert(T t) throws SQLException {
		if (t != null) {
			baseRepository.save(t);
		}
		return false;
	}
	
	@Override
	public List<T> queryAll(T t) {
		// TODO Auto-generated method stub
		if (t != null) {
			return baseRepository.findAll(Example.of(t) );
		}
		return null;
	}

	@Override
	public List<T> queryAll() {
		return baseRepository.findAll();
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		baseRepository.deleteAll();
	}

	@Override
	public Long count() {
		// TODO Auto-generated method stub
		return baseRepository.count();
	}

}
