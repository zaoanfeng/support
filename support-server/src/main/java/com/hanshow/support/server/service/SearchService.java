package com.hanshow.support.server.service;

import java.util.List;

import com.hanshow.support.server.model.Search;

public interface SearchService {

	List<Search> search(String keywords, int offset, int limit);
}