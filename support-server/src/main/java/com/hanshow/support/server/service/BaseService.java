package com.hanshow.support.server.service;

import com.hanshow.support.server.mybatis.Pages;

public interface BaseService<T, ID> {

	boolean insert(T t);
	
	boolean updateById(T t, ID id);
	
	boolean updateSelectiveById(T t);
	
	boolean delete(T t);
	
	boolean deleteById(ID id);
	
	T queryById(ID id);
	
	Pages<T> queryForPage(T t, int offset, int limit);
}
