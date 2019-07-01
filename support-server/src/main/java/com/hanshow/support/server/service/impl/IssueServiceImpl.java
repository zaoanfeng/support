package com.hanshow.support.server.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hanshow.support.server.lucene.LuceneDao;
import com.hanshow.support.server.model.Issue;
import com.hanshow.support.server.service.IssueService;

@Transactional
@Service
public class IssueServiceImpl extends BaseServiceImpl<Issue, Long> implements IssueService  {
	
	@Autowired
	private LuceneDao<Issue> luceneDao;
	
	/**
	 * 插入数据时会做分词
	 */
	
	@Override
	public boolean insert(Issue t) {
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
	public boolean updateById(Issue t, Long id) {
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

	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	@Override
	public List<Issue> search(String keyword, int offset, int limit) {
		try {
			List<Issue> list =luceneDao.queryForPage(keyword, new String[]{"content"}, offset, limit, new Issue());
			return list;
		} catch (Exception e) {
			super.logger.error(e.getMessage(), e);
		}
		return null;
	}
}
