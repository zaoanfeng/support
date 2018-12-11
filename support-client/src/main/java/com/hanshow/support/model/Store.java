package com.hanshow.support.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="store")
public class Store implements Serializable{

	private static final long serialVersionUID = 5904192426510830551L;

	@Id
	private Integer id;
	
	private String code;
	
	private String name;

	public Store() {}
	
	public Store(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
