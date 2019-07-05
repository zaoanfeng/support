package com.hanshow.support.server.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hanshow.support.server.lucene.LuceneDao;
import com.hanshow.support.server.model.Search;
import com.hanshow.support.server.service.SearchService;

@Service
public class SearchServiceImpl implements SearchService {
	@Autowired
	private LuceneDao<Search> luceneDao;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<Search> search(String keyword, int offset, int limit) {
		try {
			List<Search> list =luceneDao.queryForPage(keyword, new String[]{"content"}, offset, limit, new Search());
			return list;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}