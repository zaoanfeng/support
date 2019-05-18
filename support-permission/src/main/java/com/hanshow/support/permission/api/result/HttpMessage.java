package com.hanshow.support.permission.api.result;

import java.io.Serializable;

public class HttpMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String error;
	
	public static HttpMessage error(String errorMessage) {
		HttpMessage obj = new HttpMessage();
		obj.setError(errorMessage);
		return obj;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
