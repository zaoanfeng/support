package com.hanshow.support.service;

import java.sql.SQLException;
import java.util.List;

public interface BaseService<T, ID> {

	boolean insert(T t) throws SQLException;
	
	List<T> queryAll();
	
	void deleteAll();

	List<T> queryAll(T t);
	
	Long count();
}
