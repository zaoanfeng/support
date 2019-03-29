package com.hanshow.support.server.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanshow.support.server.model.Issue;
import com.hanshow.support.server.service.IssueService;

@RestController
@RequestMapping("/v1/issue")
public class IssueController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IssueService issueService;
	
	@PostMapping
	public HttpEntity<Void> insert(@RequestBody Issue issue) {
		issue.setCreateTime(new Date());
		issueService.insert(issue);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping(value="/{id}")
	public HttpEntity<Void> delete(@PathVariable long id) {
		issueService.deleteById(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	@GetMapping
	public HttpEntity<List<Issue>> query(@RequestParam(value="keyword")String keyword, @RequestParam(value="offset") int page, @RequestParam(value="limit") int size) {	
		return ResponseEntity.ok().body(issueService.search(keyword, (page <= 0 ? 1 : page) - 1, size));
	}

}
