package com.hanshow.support.server.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanshow.support.server.mapper.BaseMapper;
import com.hanshow.support.server.mybatis.Pages;
import com.hanshow.support.server.service.BaseService;

@Transactional
@Service
public class BaseServiceImpl<T, ID> implements BaseService<T, ID> {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BaseMapper<T, ID> baseMapper;

	@Override
	public boolean insert(T t) {
		return baseMapper.insert(t) > 0 ? true : false;
	}

	@Override
	public boolean updateById(T t, ID id) {
		String queryName = "";
		try {
			Field[] fields = t.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(Id.class)) {
					field.setAccessible(true);
					field.set(t, id);
					queryName = field.getName();
				}
			}
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage(), e);
		}
		return baseMapper.updateById(t, queryName) > 0 ? true : false;
	}

	@Override
	public boolean updateSelectiveById(T t, ID id) {
		String queryName = "";
		try {
			Field[] fields = t.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(Id.class)) {
					field.setAccessible(true);
					field.set(t, id);
					queryName = field.getName();
				}
			}
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage(), e);
		}
		return baseMapper.updateSelectiveById(t, queryName) > 0 ? true : false;
	}

	@Override
	public boolean deleteById(ID id) {
		return baseMapper.deleteById(id) > 0 ? true : false;
	}

	@Override
	public boolean delete(T t) {
		return baseMapper.delete(t) > 0 ? true : false;
	}

	@Transactional(readOnly=true)
	@Override
	public T queryById(ID id) {
		Type type = getClass().getGenericSuperclass();
		ParameterizedType p = (ParameterizedType) type;
		@SuppressWarnings("unchecked")
		Class<T> c = (Class<T>) p.getActualTypeArguments()[0];
		T t = null;
		try {
			t = c.newInstance();
			Field[] fields = t.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(Id.class)) {
					field.setAccessible(true);
					field.set(t, id);
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
		}
		List<Map<String, Object>> items = baseMapper.select(t);
		if (items.size() > 0) {
			try {
				return mapConvertT(items.get(0));
			} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	@Transactional(readOnly=true)
	@SuppressWarnings("unchecked")
	@Override
	public int queryCount(T t) {
		if (null == t) {
			Type type = getClass().getGenericSuperclass();
			ParameterizedType p = (ParameterizedType) type;
			Class<T> c = (Class<T>) p.getActualTypeArguments()[0];
			try {
				t = c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return baseMapper.selectCount(t);
	}

	@Transactional(readOnly=true)
	@SuppressWarnings("unchecked")
	@Override
	public List<T> queryForPage(T t, int offset, int limit) {
		// TODO Auto-generated method stub
		if (null == t) {
			Type type = getClass().getGenericSuperclass();
			ParameterizedType p = (ParameterizedType) type;
			Class<T> c = (Class<T>) p.getActualTypeArguments()[0];
			try {
				t = c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error(e.getMessage(), e);
			}
		}
		List<Map<String, Object>> items = baseMapper.selectForPage(t, offset, limit);
		try {
			return mapConvertT(items);
		} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Transactional(readOnly=true)
	@Override
	public Pages<T> queryForPageAndTotal(T t, int offset, int limit) {
		Pages<T> pages = new Pages<>();
		pages.setTotal(queryCount(t));
		pages.setItems(queryForPage(t, offset, limit));
		pages.setPageNo(offset);
		pages.setPageSize(limit);
		return pages;
	}

	@SuppressWarnings("unchecked")
	private List<T> mapConvertT(List<Map<String, Object>> mapList) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		List<T> list = new ArrayList<>();
		if (mapList != null && mapList.size() > 0) {
			Type type = getClass().getGenericSuperclass();
			ParameterizedType p = (ParameterizedType) type;
			Class<T> c = (Class<T>) p.getActualTypeArguments()[0];
			for (Map<String, Object> map : mapList) {
				T t = c.newInstance();
				for (String key : map.keySet()) {
					Field field = t.getClass().getDeclaredField(key);
					field.setAccessible(true);
					field.set(t, map.get(key));
				}
				list.add(t);
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private T mapConvertT(Map<String, Object> map) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Type type = getClass().getGenericSuperclass();
		ParameterizedType p = (ParameterizedType) type;
		Class<T> c = (Class<T>) p.getActualTypeArguments()[0];
		T t = c.newInstance();
		for (String key : map.keySet()) {
			Field field = t.getClass().getDeclaredField(key);
			field.setAccessible(true);
			field.set(t, map.get(key));
		}
		return t;
	}

}
