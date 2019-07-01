package com.hanshow.support.server.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hanshow.support.server.lucene.LuceneDao;
import com.hanshow.support.server.model.Article;
import com.hanshow.support.server.service.ArticleService;

@Service
public class ArticleServiceImpl extends BaseServiceImpl<Article, Long> implements ArticleService {
	@Autowired
	private LuceneDao<Article> luceneDao;
	
	/**
	 * 插入数据时会做分词
	 */
	@Override
	public boolean insert(Article t) {
		// TODO Auto-generated method stub
		if (super.insert(t)) {
			try {
				luceneDao.insert(t);
				return true;
			} catch (IOException | IllegalAccessException e) {
				super.logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	@Override
	public boolean deleteById(Long id) {
		// TODO Auto-generated method stub
		if (super.deleteById(id)) {
			try {
				luceneDao.delete("id", Long.toString(id));
				return true;
			} catch (IOException e) {
				super.logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	@Override
	public boolean updateById(Article t, Long id) {
		// TODO Auto-generated method stub
		if (super.updateById(t, id)) {
			try {
				luceneDao.update(t, "id", Long.toString(t.getId()));
				return true;
			} catch (IllegalAccessException | IOException e) {
				super.logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	@Override
	public List<Article> search(String keyword, int offset, int limit) {
		try {
			List<Article> list =luceneDao.queryForPage(keyword, new String[]{"content"}, offset, limit, new Article());
			return list;
		} catch (Exception e) {
			super.logger.error(e.getMessage(), e);
		}
		return null;
	}
}
