package com.hanshow.support.server.api.v1;

import java.util.Date;
import java.util.List;

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
import com.hanshow.support.server.model.Article;
import com.hanshow.support.server.mybatis.Pages;
import com.hanshow.support.server.service.ArticleService;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

@RestController
@RequestMapping("/v1/article")
public class ArticleController {

	//private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ArticleService articleService;
	
	@PostMapping
	public HttpEntity<Void> insert(@RequestBody String data) {
		Article article = JSON.parseObject(data, Article.class);
		article.setUpdateTime(new Date());
		article.setCreateTime(new Date());
		articleService.insert(article);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping(value="/{id}")
	public HttpEntity<Void> delete(@PathVariable long id) {
		articleService.deleteById(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	@PutMapping
	public HttpEntity<Void> update(@RequestBody String data) {
		Article article = JSON.parseObject(data, Article.class);
		if (articleService.updateById(article, article.getId())) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
		}
		
	}
	
	@PatchMapping
	public HttpEntity<Void> updateSelective(@RequestBody String data) {
		Article article = JSON.parseObject(data, Article.class);
		if (articleService.updateSelectiveById(article, article.getId())) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
		}	
	}
	
	@GetMapping
	public HttpEntity<Pages<Article>> query(@RequestParam(value="article") String params, @RequestParam(value="page") int page, @RequestParam(value="limit") int size) {	
		Article article = JSON.parseObject(params, Article.class);
		return ResponseEntity.ok().body(articleService.queryForPageAndTotal(article, (page <= 0 ? 1 : page), size));
	}
	
	@GetMapping("/{id}")
	public HttpEntity<Article> query(@PathVariable long id) {
		Article article = articleService.queryById(id);
		return ResponseEntity.ok().body(article);
	}

}
