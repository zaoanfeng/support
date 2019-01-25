package com.hanshow.support.upgrade.ap;

import java.io.File;

import org.ini4j.Wini;

import com.hanshow.support.upgrade.model.Ap;
import com.hanshow.support.util.SSH2Tools;

public class Ap2Config extends ApConfig {
	
	private static String REMOTE_PATH = "/home/elinker/etc/";
	private static String REMOTE_FILE_NAME = "config.ini";
	private static String[] SSH_CONNECT = {"root", "root", "22"};
	
	@Override
	protected boolean modifyApPort(Ap ap, String url, String port) throws Exception {
		try(SSH2Tools ssh = new SSH2Tools().connect(ap.getIp(), Integer.valueOf(SSH_CONNECT[2]), SSH_CONNECT[0], SSH_CONNECT[1])) {		
			// 接取文件
			String localPath = System.getProperty("user.dir").toString();
			ssh.scpFileFromRemote(REMOTE_PATH + REMOTE_FILE_NAME, localPath);
			Wini ini = new Wini(new File(localPath + File.separator + REMOTE_FILE_NAME));
			ini.getConfig().setLineSeparator("\n");
			ini.put("esl-working", "ipaddr", url);
			ini.put("esl-working", "port", port);
	        ini.store();
	        // 修改完成上传
	    	ssh.scpFileToRemote(localPath +File.separator + REMOTE_FILE_NAME, REMOTE_PATH);
	    	ssh.shell("reboot");
			System.out.println(ap.getIp() + " is reboot ...........");
			return true;
		} catch (Exception e) {
			throw e;
		}
	}
}
