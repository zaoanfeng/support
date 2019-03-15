package com.hanshow.support.monitor.mail;

public class AnalysisStatus {
	public static final byte NORMAL = 0;
	public static final byte WARNING = 1;
	public static final byte SERIOUS = 2;
	public static final byte COLLAPSE = 3;

	private byte status;
	private int packageAmount;

	public byte getStatus() {
		return this.status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	public int getPackageAmount() {
		return this.packageAmount;
	}

	public void setPackageAmount(int packageAmount) {
		this.packageAmount = packageAmount;
	}
}
