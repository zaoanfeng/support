package com.hanshow.support.mail;

import java.util.List;

public interface MailReceiveListener {

	void receive(List<MailModel> list);
}
