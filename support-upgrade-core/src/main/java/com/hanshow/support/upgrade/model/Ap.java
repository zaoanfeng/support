package com.hanshow.support.upgrade.model;

public class Ap {

	private Integer id;
	
	private String ip;
	
	private String version;
	
	private String mac;
	
	private boolean status = true;
	
	public Ap() {}
	
	public Ap(String ip) {
		this.ip = ip;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
}
