package com.hanshow.support.upgrade.ap;

import com.hanshow.support.upgrade.model.Ap;
import com.hanshow.support.util.SSH2Tools;

public class ApPcieConfig extends ApConfig {

	private static String[] SSH_CONNECT = {"root", "hanshow-imx6", "22"};
	
	@Override
	protected boolean modifyApPort(Ap ap, String url, String port) throws Exception {
		try(SSH2Tools ssh = new SSH2Tools().connect(ap.getIp(), Integer.valueOf(SSH_CONNECT[2]), SSH_CONNECT[0], SSH_CONNECT[1])) {		
			ssh.shell(String.format("cgi -a ew_ipaddr=\"%s\"", url));
	    	ssh.shell("reboot");
	    	System.out.println(ap.getIp() + " is reboot ...........");
	    	ssh.reconnect();
			System.out.println(ap.getIp() + " already started ...........");
			return true;
		} catch (Exception e) {
			throw e;
		}
	}
}