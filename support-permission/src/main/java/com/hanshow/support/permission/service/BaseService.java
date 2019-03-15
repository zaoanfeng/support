package com.hanshow.support.permission.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.data.domain.Page;

public interface BaseService<T, ID> {

	boolean insert(T t) throws SQLException;
	
	boolean update(T t) throws SQLException;
	
	boolean updateBySelective(T t, ID id) throws SQLException;
	
	boolean existsById(ID id) throws SQLException;
	
	List<T> queryAll();
	
	void deleteAll();

	List<T> queryAll(T t);
	
	Page<T> queryForPage(int page, int size);
	
	Page<T> queryForPage(T t, int page, int size);
	
	Long count();
}
