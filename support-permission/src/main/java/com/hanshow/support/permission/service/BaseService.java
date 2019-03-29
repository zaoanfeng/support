package com.hanshow.support.permission.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.data.domain.Page;

public interface BaseService<T, ID> {

	/**
	 * 添加一条数据
	 * @param 对象
	 * @return 成功或失败状态
	 * @throws SQLException
	 */
	boolean insert(T t) throws SQLException;
	
	/**
	 * 根据id删除一个用户
	 * @param id
	 */
	void deleteById(ID id);
	
	/**
	 * 根据对象条件删除
	 * @param t
	 */
	void delete(T t);
	
	/**
	 * 全删除
	 */
	@Deprecated
	void deleteAll();
	
	/**
	 * 所有字段更新，传入字段为null，数据库中值也会置为null
	 * @param t
	 * @return
	 * @throws SQLException
	 */
	boolean update(T t) throws SQLException;
	
	/**
	 * 局部更新，当对象中某个字段为null,不更新
	 * @param t
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	boolean updateBySelective(T t, ID id) throws SQLException;
	
	/**
	 * 判断数据是否存在
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	boolean existsById(ID id) throws SQLException;
	
	/**
	 * 查询全部
	 * @return
	 */
	@Deprecated
	List<T> queryAll();
	
	/**
	 * 根据id查一条
	 */
	T queryById(ID id);

	/**
	 * 查出满足条件的全部数量
	 * @param t
	 * @return
	 */
	List<T> queryAll(T t);
	
	/**
	 * 分页查询
	 * @param page 页码第一页为0
	 * @param size 每页长度
	 * @return
	 */
	Page<T> queryForPage(int page, int size);
	
	/**
	 * 带查询条件的分页
	 * @param t 查询条件
	 * @param page 页码第一页为0
	 * @param size 每页长度
	 * @return
	 */
	Page<T> queryForPage(T t, int page, int size);
	
	/**
	 * 查总数量
	 * @return
	 */
	Long count();
}