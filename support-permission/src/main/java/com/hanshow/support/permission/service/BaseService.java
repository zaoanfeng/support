package com.hanshow.support.permission.service;

import java.sql.SQLException;
import java.util.List;

public interface BaseService<T, ID> {

	boolean insert(T t) throws SQLException;
	
	boolean update(T t) throws SQLException;
	
	boolean updateBySelective(T t, ID id) throws SQLException;
	
	boolean existsById(ID id) throws SQLException;
	
	List<T> queryAll();
	
	void deleteAll();

	List<T> queryAll(T t);
	
	Long count();
}
