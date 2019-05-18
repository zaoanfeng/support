package com.hanshow.support.server.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import com.hanshow.support.server.mybatis.SqlProvider;

@Mapper
public interface BaseMapper<T, ID> {

	@InsertProvider(type=SqlProvider.class, method="insert")
	int insert(T t);
	
	@UpdateProvider(type=SqlProvider.class, method="updateById")
	int updateById(@Param("t")T t,  @Param("queryId") String queryId);
	
	@UpdateProvider(type=SqlProvider.class, method="updateSelectiveById")
	int updateSelectiveById(@Param("t")T t, @Param("queryId") String queryId);
	
	@DeleteProvider(type=SqlProvider.class, method="deleteById")
	int deleteById(ID id);
	
	@DeleteProvider(type=SqlProvider.class, method="delete")
	int delete(T t);
	
	@SelectProvider(type=SqlProvider.class, method="selectById")
	@Deprecated
	T selectById(@Param("t")T t, @Param("id")ID id);
	
	@SelectProvider(type=SqlProvider.class, method="select")
	List<Map<String, Object>> select(T t);
	
	@SelectProvider(type=SqlProvider.class, method="selectCount")
	int selectCount(T t);
	
	@SelectProvider(type=SqlProvider.class, method="selectForPage")
	List<Map<String, Object>> selectForPage(@Param("t")T t, @Param("currPage")int offset, @Param("pageSize")int limit);
}