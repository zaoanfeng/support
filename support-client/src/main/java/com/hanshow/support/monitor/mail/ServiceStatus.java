package com.hanshow.support.monitor.mail;

import java.io.Serializable;

public class ServiceStatus implements Serializable {
	private boolean status;
	private String name;
	public static boolean STARTED = true;
	public static boolean STOPED = false;
	private static final long serialVersionUID = -4045031058413549918L;

	public ServiceStatus(String name, boolean status) {
		this.name = name;
		this.status = status;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isStatus() {
		return this.status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
}
