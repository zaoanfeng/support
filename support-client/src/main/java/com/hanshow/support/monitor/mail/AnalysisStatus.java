package com.hanshow.support.monitor.mail;

import java.util.HashMap;
import java.util.Map;

public class AnalysisStatus {
	public static final byte NORMAL = 0;
	public static final byte WARNING = 1;
	public static final byte SERIOUS = 2;
	public static final byte COLLAPSE = 3;
	public static final String SMALL = "s";
	public static final String MIDDLE = "m";
	public static final String LARGE = "l";
	private byte status;
	private Map<String, Integer> packageAmount;

	public byte getStatus() {
		return this.status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	public Map<String, Integer> getPackageAmount() {
		return this.packageAmount;
	}

	public void setPackageAmount(String index, int value) {
		if (this.packageAmount == null) {
			this.packageAmount = new HashMap<>();
		}
		this.packageAmount.put(index, Integer.valueOf(value));
	}
}
