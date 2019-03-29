package com.hanshow.support.server.mybatis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.ibatis.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hanshow.support.server.utils.IdUtils;

public class SqlProvider {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 拼接插入sql
	 * 
	 * @param t
	 * @return
	 */
	public <T> String insert(T t) throws IllegalAccessException {

		return new SQL() {
			{
				INSERT_INTO(getTableName(t));
				String[] columns = getColumn(t, true);
				for (String column : columns) {
					VALUES(translateColumn(column), "#{" + column + "}");
				}
			}
		}.toString();

		/*
		 * StringBuffer sql = new StringBuffer("INSERT INTO ");
		 * sql.append("`").append(getTableName(t)).append("`("); String[] columns =
		 * getColumn(t); Set<String> keySet = map.keySet(); Iterator<String> keys =
		 * keySet.iterator(); int i=0; while(keys.hasNext()) { i++;
		 * sql.append("`").append(keys.next()); if (i < keySet.size()) {
		 * sql.append("`, "); } } sql.append(") VALUES ("); i=0; Set<Entry<String,
		 * Object>> entrySet = map.entrySet(); Iterator<Entry<String, Object>> entrys =
		 * entrySet.iterator(); while(entrys.hasNext()) { i++;
		 * sql.append("`").append(entrys.next()); if (i < entrySet.size()) {
		 * sql.append("`, "); } } sql.append(");"); return new
		 * SQL().INSERT_INTO(sql.toString()).toString();
		 */
	}

	/**
	 * 拼接更新sql
	 * 
	 * @param t
	 * @return
	 */
	public <T, ID> String updateById(ID id, T t) throws IllegalAccessException {
		return new SQL() {
			{
				UPDATE(getTableName(t));
				String[] columns = getColumn(t);
				for (String column : columns) {
					SET(translateColumn(column) + " = #{" + column + "}");
				}
				String where = id.getClass().getSimpleName();
				WHERE(translateColumn(where) + " = #{" + where + "}");
			}
		}.toString();
	}

	/**
	 * 拼接更新sql
	 * 
	 * @param t
	 * @return
	 */
	public <T, ID> String updateSelectiveById(ID id, T t) throws IllegalAccessException {
		return new SQL() {
			{
				UPDATE(getTableName(t));
				String[] columns = getColumnIsNotNull(t);
				for (String column : columns) {
					SET(translateColumn(column) + " = #{" + column + "}");
				}
				String where = id.getClass().getSimpleName();
				WHERE(translateColumn(where) + " = #{" + where + "}");
			}
		}.toString();
	}

	/**
	 * 根据id删除sql
	 * 
	 * @param t
	 * @return
	 */
	public <T, ID> String deleteById(T t, ID id) {
		return new SQL() {
			{
				DELETE_FROM(getTableName(t));
				String where = id.getClass().getSimpleName();
				WHERE(translateColumn(where) + " = #{" + where + "}");
			}
		}.toString();
	}

	/**
	 * 删除sql
	 * 
	 * @param t
	 * @return
	 * @throws IllegalAccessException
	 */
	public <T> String delete(T t) throws IllegalAccessException {
		return new SQL() {
			{
				DELETE_FROM(getTableName(t));
				Map<String, Object> map = getColumnAndValue(t);
				for (String column : map.keySet()) {
					if (map.get(column) != null) {
						WHERE(translateColumn(column) + " = #{" + column + "}");
					}
				}

			}
		}.toString();
	}

	/**
	 * 基于id查询
	 * 
	 * @param t
	 * @param id
	 * @return
	 * @throws IllegalAccessException
	 */
	public <T, ID> String selectById(T t, ID id) throws IllegalAccessException {
		return new SQL() {
			{
				SELECT(getColumn(t));
				FROM(getTableName(t));
				String where = id.getClass().getSimpleName();
				WHERE(translateColumn(where) + " = #{" + where + "}");
			}
		}.toString();
	}

	/**
	 * 基于对象查询
	 * 
	 * @param t
	 * @param id
	 * @return
	 * @throws IllegalAccessException
	 */
	public <T> String select(T t) throws IllegalAccessException {
		return new SQL() {
			{
				SELECT(getColumn(t));
				FROM(getTableName(t));
				Map<String, Object> map = getColumnAndValue(t);
				for (String column : map.keySet()) {
					if (map.get(column) != null) {
						WHERE(translateColumn(column) + " = #{" + column + "}");
					}
				}
			}
		}.toString();
	}

	/**
	 * 获取表名
	 * 
	 * @param t
	 * @return
	 */
	private <T> String getTableName(T t) {
		String tableName = "";
		if (t.getClass().isAnnotationPresent(Table.class)) {
			tableName = t.getClass().getAnnotation(Table.class).name();
		} else {
			tableName = t.getClass().getSimpleName();
		}
		return tableName;
	}

	/**
	 * 获取字段名及值
	 * 
	 * @param t
	 * @return
	 */
	private <T> String[] getColumn(T t) throws IllegalAccessException {
		return getColumn(t, false);
	}

	/**
	 * 获取字段名及值
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	private <T> String[] getColumn(T t, boolean isGenerateId) throws IllegalAccessException {
		// 获取字段
		Field[] fields = t.getClass().getDeclaredFields();
		List<String> columns = new ArrayList<>();
		// 遍例每一个属性
		for (Field field : fields) {
			if (field.getName().equals("serialVersionUID")) {
				continue;
			}
			String key = "";
			// 判断是否有注解，有使用注解的表字段名，没有使用对象属性表
			boolean isAnnotation = field.isAnnotationPresent(Id.class);
			if (isAnnotation && isGenerateId) {
				try {
					field.setAccessible(true);
					field.set(t, IdUtils.generateId());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error(e.getMessage(), e);
					throw e;
				}
			}
			isAnnotation = field.isAnnotationPresent(Column.class);
			if (isAnnotation) {
				key = field.getAnnotation(Column.class).name();
			}
			if (key == null || key.isEmpty()) {
				key = field.getName();
			}
			if (key == null || key.isEmpty()) {
				throw new IllegalArgumentException("column cannot found");
			} else {
				columns.add(key);
			}
		}
		return columns.toArray(new String[columns.size()]);
	}

	/**
	 * 获取字段名及值
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	private <T> String[] getColumnIsNotNull(T t) throws IllegalAccessException {
		// 获取字段
		Field[] fields = t.getClass().getDeclaredFields();
		List<String> columns = new ArrayList<>();
		// 遍例每一个属性
		for (Field field : fields) {
			if (field.getName().equals("serialVersionUID")) {
				continue;
			}
			String key = null;
			// 判断是否有注解，有使用注解的表字段名，没有使用对象属性表
			boolean isAnnotation = field.isAnnotationPresent(Column.class);
			if (isAnnotation) {
				key = field.getAnnotation(Column.class).name();
			}
			if (key == null) {
				key = field.getName();
			}
			if (key != null && field.get(t) != null) {
				columns.add(key);
			}
		}
		return columns.toArray(new String[columns.size()]);
	}

	/**
	 * 获取字段名及值
	 * 
	 * @param t
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private <T> Map<String, Object> getColumnAndValue(T t) throws IllegalAccessException {
		// 获取字段
		Field[] fields = t.getClass().getDeclaredFields();
		Map<String, Object> map = new HashMap<>();
		// 遍例每一个属性
		for (Field field : fields) {
			if (field.getName().equals("serialVersionUID")) {
				continue;
			}
			String key = null;
			// 判断是否有注解，有使用注解的表字段名，没有使用对象属性表
			boolean isAnnotation = field.isAnnotationPresent(Column.class);
			if (isAnnotation) {
				key = field.getAnnotation(Column.class).name();
			}
			if (key == null) {
				key = field.getName();
			}
			if (key != null) {
				field.setAccessible(true);
				try {
					map.put(key, field.get(t));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error(e.getMessage(), e);
					throw e;
				}
			} else {
				throw new IllegalAccessError("column cannot found");
			}
		}
		return map;
	}

	private String translateColumn(String column) {
		// 使用对象属性时，当遇到大写字母转成_小写
		StringBuffer newKey = new StringBuffer();
		for (char value : column.toCharArray()) {
			if (Character.isUpperCase(value)) {
				newKey.append("_").append(Character.toLowerCase(value));
			} else {
				newKey.append(value);
			}
		}
		return newKey.toString();
	}
}
