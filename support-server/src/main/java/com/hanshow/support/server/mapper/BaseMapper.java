package com.hanshow.support.server.mapper;

import java.util.List;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import com.hanshow.support.server.mybatis.Pages;
import com.hanshow.support.server.mybatis.SqlProvider;

@Mapper
public interface BaseMapper<T, ID> {

	@InsertProvider(type=SqlProvider.class, method="insert")
	int insert(T t);
	
	@UpdateProvider(type=SqlProvider.class, method="updateById")
	int updateById(ID id, T t);
	
	@UpdateProvider(type=SqlProvider.class, method="updateSelectiveById")
	int updateSelectiveById(ID id, T t);
	
	@DeleteProvider(type=SqlProvider.class, method="deleteById")
	int deleteById(ID id);
	
	@DeleteProvider(type=SqlProvider.class, method="delete")
	int delete(T t);
	
	@SelectProvider(type=SqlProvider.class, method="selectById")
	T selectById(ID id);
	
	@SelectProvider(type=SqlProvider.class, method="select")
	List<T> select(T t);
	
	@SelectProvider(type=SqlProvider.class, method="select")
	Pages<T> selectForPage(T t, @Param("currPage")int offset, @Param("pageSize")int limit);
}