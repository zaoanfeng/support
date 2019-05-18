package com.hanshow.support.server.service;

import java.util.List;

import com.hanshow.support.server.mybatis.Pages;

public interface BaseService<T, ID> {

	boolean insert(T t);
	
	boolean updateById(T t, ID id);
	
	boolean updateSelectiveById(T t, ID id);
	
	boolean delete(T t);
	
	boolean deleteById(ID id);
	
	T queryById(ID id);
	
	int queryCount(T t);
	
	List<T> queryForPage(T t, int offset, int limit);
	
	Pages<T> queryForPageAndTotal(T t, int offset, int limit);

}
