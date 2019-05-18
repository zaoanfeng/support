package com.hanshow.support.server.model;

import java.io.Serializable;
import java.util.Date;

public class UserStore implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	
	private String username;
	
	private long storeId;
	
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public long getStoreId() {
		return storeId;
	}

	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
