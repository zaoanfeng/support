package com.hanshow.support.server.service;

import java.util.List;

import com.hanshow.support.server.model.Article;

public interface ArticleService extends BaseService<Article, Long> {

	public List<Article> search(String keyword, int offset, int limit);
}
