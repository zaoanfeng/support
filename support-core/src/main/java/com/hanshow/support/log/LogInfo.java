package com.hanshow.support.log;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class LogInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private int seek;
	
	private Map<String, List<String>> data;

	public int getSeek() {
		return seek;
	}

	public void setSeek(int seek) {
		this.seek = seek;
	}

	public Map<String, List<String>> getData() {
		return data;
	}

	public void setData(Map<String, List<String>> data) {
		this.data = data;
	}

}
