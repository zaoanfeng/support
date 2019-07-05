package com.hanshow.support.mail;

import java.util.List;

public class MailServerTest {

	public static void main(String[] args) {
		MailServer.getInstance().start().setReceiveListener(new MailReceiveListener() {		
			@Override
			public void receive(List<MailModel> list) {
				// TODO Auto-generated method stub
				list.forEach(l -> {
					System.out.println(l.getSubject());
				});
			}
		});
	}
}