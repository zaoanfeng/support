package com.hanshow.support.server.api.v1;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanshow.support.server.model.Article;
import com.hanshow.support.server.model.Issue;
import com.hanshow.support.server.model.Search;
import com.hanshow.support.server.service.ArticleService;
import com.hanshow.support.server.service.IssueService;
import com.hanshow.support.server.service.SearchService;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

@RestController
@RequestMapping("/v1/search")
public class SearchController {

	//private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SearchService searchService;
	@Autowired
	private IssueService issueService;
	@Autowired
	private ArticleService articleService;
	
	@GetMapping
	public HttpEntity<List<Search>> query(@RequestParam(value="keyword")String keyword, @RequestParam(value="offset") int page, @RequestParam(value="limit") int size) {	
		// 查问题
		/*List<Issue> issueList = issueService.search(keyword, (page <= 0 ? 1 : page) - 1, size);
		List<Search> searchList = new ArrayList<>();
		if (issueList != null) {
			issueList.forEach(i -> {
				Search search = new Search(i.getId(), i.getClass().getSimpleName(), i.getTitle(), "", i.getContent());
				searchList.add(search);
			});		
		}
		// 查文档
		List<Article> articleList = articleService.search(keyword, (page <= 0 ? 1 : page) - 1, size);
		if (articleList != null) {
			articleList.forEach(i -> {
				Search search = new Search(i.getId(), i.getClass().getSimpleName(), i.getTitle(), "", i.getContent());
				searchList.add(search);
			});
		}*/
		List<Search> searchList = searchService.search(keyword, (page <= 0 ? 1 : page) - 1, size);
		return ResponseEntity.ok().body(searchList);
	}
	
	@GetMapping(value="detail")
	public HttpEntity<Search> query(@RequestParam(value="type")String type, @RequestParam(value="id") String id) {	
		Search search = null;
		if (type.equals(Article.class.getSimpleName())) {
			Article article = articleService.queryById(Long.valueOf(id));
			if (article != null) {
				search = new Search(article.getId(), article.getClass().getSimpleName(), article.getTitle(), article.getAuthor(), article.getContent());
			}
		} else if (type.equals(Issue.class.getSimpleName())) {
			Issue issue = issueService.queryById(Long.valueOf(id));
			if (issue != null) {
				search = new Search(issue.getId(), issue.getClass().getSimpleName(), issue.getTitle(), issue.getAuthor(), issue.getContent());
			}
		}
		MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        Node document = parser.parse(search.getContent());
        String html = renderer.render(document);
        search.setContent(html);
		return ResponseEntity.ok().body(search);
	}

}
