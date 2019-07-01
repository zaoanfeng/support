package com.hanshow.support.server.api.v1;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.hanshow.support.server.model.Issue;
import com.hanshow.support.server.mybatis.Pages;
import com.hanshow.support.server.service.IssueService;

@RestController
@RequestMapping("/v1/issue")
public class IssueController {

	//private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IssueService issueService;
	
	@PostMapping
	public HttpEntity<Void> insert(@RequestBody String data) {
		Issue issue = JSON.parseObject(data, Issue.class);
		issue.setCreateTime(new Date());
		issue.setUpdateTime(new Date());
		issueService.insert(issue);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping(value="/{id}")
	public HttpEntity<Void> delete(@PathVariable long id) {
		issueService.deleteById(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	@PutMapping
	public HttpEntity<Void> update(@RequestBody String data) {
		Issue issue = JSON.parseObject(data, Issue.class);
		issue.setUpdateTime(new Date());
		if (issueService.updateById(issue, issue.getId())) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
		}	
	}
	
	@PatchMapping
	public HttpEntity<Void> updateSelective(@RequestBody String data) {
		Issue issue = JSON.parseObject(data, Issue.class);
		issue.setUpdateTime(new Date());
		if (issueService.updateSelectiveById(issue, issue.getId())) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
		}	
	}
	
	@GetMapping
	public HttpEntity<Pages<Issue>> query(@RequestParam(value="issue")String params, @RequestParam(value="page") int page, @RequestParam(value="limit") int size) {	
		Issue issue = JSON.parseObject(params, Issue.class);
		return ResponseEntity.ok().body(issueService.queryForPageAndTotal(issue, (page <= 0 ? 1 : page), size));
	}

	@GetMapping("/{id}")
	public HttpEntity<Issue> query(@PathVariable long id) {
		Issue issue = issueService.queryById(id);
		return ResponseEntity.ok().body(issue);
	}
}
