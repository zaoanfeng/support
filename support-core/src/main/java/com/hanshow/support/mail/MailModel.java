package com.hanshow.support.mail;

import java.io.Serializable;
import java.util.Date;

public class MailModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private String Id;
	
	private String from;
	
	private Date sendDate;
	
	private String[] recipients;
	
	private String[] ccRecipients;
	
	private String[] bccRecipients;
	
	private String subject;
	
	private String body;
	
	private String attachment;
	
	private boolean read;
	
	private boolean needReply;

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public String[] getRecipients() {
		return recipients;
	}

	public void setRecipients(String[] recipients) {
		this.recipients = recipients;
	}

	public String[] getCcRecipients() {
		return ccRecipients;
	}

	public void setCcRecipients(String[] ccRecipients) {
		this.ccRecipients = ccRecipients;
	}

	public String[] getBccRecipients() {
		return bccRecipients;
	}

	public void setBccRecipients(String[] bccRecipients) {
		this.bccRecipients = bccRecipients;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isNeedReply() {
		return needReply;
	}

	public void setNeedReply(boolean needReply) {
		this.needReply = needReply;
	}
	
}
