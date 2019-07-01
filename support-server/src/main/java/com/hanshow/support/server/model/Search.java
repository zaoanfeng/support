package com.hanshow.support.server.model;

import java.io.Serializable;

public class Search implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	private String type;
	
	private String content;
	
	private String author;
	
	private String title;
	
	public Search() {}
	
	public Search(Long id, String type, String title, String author, String content) {
		this.id = id;
		this.type = type;
		this.title = title;
		this.author = author;
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	
}
