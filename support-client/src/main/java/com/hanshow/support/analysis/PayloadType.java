package com.hanshow.support.analysis;

import com.hanshow.support.SupportType;

public enum PayloadType {

	NETLINE("NETLINK"),
	SCREEN_CLEAR("SCREEN_CLEAR"),
	UPDATE("UPDATE"),
	QUERY("QUERY");
	
private String value;
	
	private PayloadType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static PayloadType getPayloadTypeByValue(String value) {
		for(PayloadType type : PayloadType.values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		return null;
	}
	
	public SupportType[] getValues() {
		return SupportType.values();
	}
}
