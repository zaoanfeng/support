package com.hanshow.support.install;

public class StopService extends Base {

	public void exec() {
		getFileDir();
		stopService();
	}
}
