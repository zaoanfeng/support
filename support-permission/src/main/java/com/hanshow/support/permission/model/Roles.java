package com.hanshow.support.permission.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class Roles implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(unique = true, nullable = false)
	private long id;
	
	@Column(nullable = false)
	private String name;
	
	private String description;
	
	private String clientId;
	
	private Date updateDate;
	
	private Date createDate;
}
