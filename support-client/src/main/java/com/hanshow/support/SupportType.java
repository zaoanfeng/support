package com.hanshow.support;

public enum SupportType {

	MONITOR("monitor"),
	ESLWorkingUpgrade("eslworkingUpgrade"),
	APUpgrade("apUpgrade"),
	APConfig("apConfig") ;
	
	private String value;
	
	private SupportType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static SupportType getSupportTypeByValue(String value) {
		for(SupportType type : SupportType.values()) {
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
