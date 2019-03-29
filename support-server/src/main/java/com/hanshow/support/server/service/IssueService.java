package com.hanshow.support.server.service;

import java.util.List;

import com.hanshow.support.server.model.Issue;

public interface IssueService extends BaseService<Issue, Long> {

	List<Issue> search(String keywords, int offset, int limit);
}