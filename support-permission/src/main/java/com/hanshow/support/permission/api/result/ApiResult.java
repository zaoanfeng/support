package com.hanshow.support.permission.api.result;

import java.io.Serializable;

import lombok.Data;

@Data
public class ApiResult implements Serializable {

	private static final long serialVersionUID = 1L;

	protected int code;
	
	protected String message;
	
	public ApiResult(Status status) {
		this.code = status.getCode();
		this.message = status.name().toLowerCase();
	}
	
	public ApiResult(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public static ApiResult success() {
		return new ApiResult(Status.SUCCESS);
	}
	
	public static ApiResult fail() {
		return new ApiResult(Status.FAIL);
	} 
	
	public enum Status {
		SUCCESS(1001),
		FAIL(1002),
		USER_EXISTS(1003),
		USER_NOT_EXISTS(1004),
		NO_PERMISSION(1007);
		
		
		private int code;
		
		Status (int code) {
			this.code = code;
		}
		
		public int getCode() {
			return code;
		}
	}
}
