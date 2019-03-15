package com.hanshow.support.permission.service.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.hanshow.support.permission.dao.BaseRepository;
import com.hanshow.support.permission.service.BaseService;

public class BaseServiceImpl<T, ID extends Serializable> implements BaseService<T, ID> {

	@Autowired
	private BaseRepository<T, ID> baseRepository;

	@Override
	public boolean insert(T t) throws SQLException {
		if (t != null) {
			return baseRepository.save(t) == null ? false : true;
		}
		return false;
	}

	@Override
	public boolean update(T t) throws SQLException {
		// TODO Auto-generated method stub
		return insert(t);
	}

	@Override
	public boolean updateBySelective(T t, ID id) throws SQLException {
		T queryT = baseRepository.findById(id).get();
		Field[] fields = t.getClass().getDeclaredFields();
		for (Field field : fields) {	
			try {
				field.setAccessible(true);
				if(null == field.get(t)) {
					Field queryField = queryT.getClass().getDeclaredField(field.getName());
					queryField.setAccessible(true);
					field.set(t, queryField.get(queryT));
				}
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				throw new SQLException(e);
			}
		}
		return insert(t);
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

	@Override
	public boolean existsById(ID id) throws SQLException {
		if (id != null && baseRepository.existsById(id)) {
			return true;
		}
		return false;
	}

	@Override
	public Page<T> queryForPage(int page, int size) {	
		return baseRepository.findAll(PageRequest.of(page, size));
	}

	@Override
	public Page<T> queryForPage(T t, int page, int size) {
		// TODO Auto-generated method stub
		return baseRepository.findAll(Example.of(t), PageRequest.of(page, size));
	}

}
