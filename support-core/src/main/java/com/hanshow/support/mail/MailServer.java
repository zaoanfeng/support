package com.hanshow.support.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hanshow.support.util.Config;

public class MailServer {

	private Logger logger = LoggerFactory.getLogger(MailClient.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private MailReceiveListener mailReceiveListener;
	private static Date lastActiveTime = new Date();
	private static MailServer mailServer;
	private static final int INIT = 10 * 1000;

	private MailServer() {

	}

	public static MailServer getInstance() {
		if (mailServer == null) {
			mailServer = new MailServer();
		}
		return mailServer;
	}

	/**
	 * 接收邮件
	 * 
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	public MailServer start() {

		Timer timer = new Timer();
		int DELAY = Config.getInstance().getInt("mail.server.delay") * 1000;
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				Store store = null;
				List<MailModel> list = new ArrayList<>();
				try {
					Properties p = new Properties();
					if (Config.getInstance().getString("mail.server.protocol").equals("pop3")) {
						// pop3的配置
						p.setProperty("mail.pop3.socketFactory.class", Config.getInstance().getString("mail.server.class"));
						p.setProperty("mail.pop3.socketFactory.fallback", Config.getInstance().getString("mail.server.fallback"));
						p.setProperty("mail.pop3.host", Config.getInstance().getString("mail.server.host"));
						p.setProperty("mail.pop3.port", Config.getInstance().getString("mail.server.port"));
						p.setProperty("mail.pop3.auth", Config.getInstance().getString("mail.server.auth"));
						// 创建连接
						Session session = Session.getDefaultInstance(p, null);
						store = session.getStore(Config.getInstance().getString("mail.server.protocol"));
						store.connect(Config.getInstance().getString("mail.server.recipient.address"), Config.getInstance().getString("mail.server.recipient.password"));
						Date start = new Date();
						Folder[] folders = store.getDefaultFolder().list();
						for (Folder folder : folders) {
							folder.open(Folder.READ_ONLY);
							
							//获取所有邮件信息
							Message messages[] = folder.getMessages();
							int messageCount = messages.length;
							//筛选未读邮件
							for (int i = messageCount; i > 0; i--) {
								Message message = messages[i - 1];
								if (message.getSentDate().getTime() <= lastActiveTime.getTime()) {
									break;
								}
								MailModel model = receiveMail(message);
								list.add(model);	
							}
						}
						lastActiveTime = start;
						logger.debug("Received new mail size =" + list.size());
						logger.debug("Process spend time is " + (new Date().getTime() - start.getTime()) + "ms");
					}
					if (list.size() > 0) {
						Collections.reverse(list);
						mailReceiveListener.receive(list);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					//关流
					if (store != null) {
						try {
							store.close();
						} catch (MessagingException e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
			}
		}, INIT, DELAY);
		return this;
	}

	/**
	 * 回谳线程
	 * @param listener
	 */
	public void setReceiveListener(MailReceiveListener listener) {
		this.mailReceiveListener = listener;
	}

	private MailModel receiveMail(Message message) throws MessagingException, IOException {
		MailModel model = new MailModel();
		model.setSubject(getSubject(message));
		model.setSendDate(message.getSentDate());
		model.setRead(isNew(message));
		model.setFrom(message.getFrom()[0].toString());
		model.setRecipients(getRecipients(message.getRecipients(Message.RecipientType.TO)));
		model.setCcRecipients(getRecipients(message.getRecipients(Message.RecipientType.CC)));
		model.setBccRecipients(getRecipients(message.getRecipients(Message.RecipientType.BCC)));
		// 获得邮件内容===============
		StringBuffer bodyText = new StringBuffer();
		getBody(bodyText, (Part) message);
		model.setBody(bodyText.toString());
		// 判断获取附件
		File attachFile = new File(System.getProperty("user.dir") + "/attachment/");
		if (!attachFile.exists()) {
			attachFile.mkdir();
		}
		boolean isAttachMent = saveAttachMent(attachFile.getPath(), (Part) message);
		if (isAttachMent) {
			model.setAttachment(attachFile.getPath());
		}
		return model;
	}

	/**
	 *  * 判断此邮件是否已读，如果未读返回false,反之返回true
	 */
	private boolean isNew(Message message) throws MessagingException {
		boolean isNew = false;
		Flags.Flag[] flags = message.getFlags().getSystemFlags();
		for (int i = 0; i < flags.length; i++) {
			if (flags[i] == Flags.Flag.SEEN) {
				isNew = true;
			}
		}
		return isNew;
	}

	/**
	 * 获取联系人
	 * 
	 * @param allRecipients
	 * @param type
	 * @return
	 */
	private String[] getRecipients(Address[] allRecipients) {
		// TODO Auto-generated method stub
		List<String> recipients = new ArrayList<>();
		if (allRecipients == null) {
			return null;
		}
		for (int i = 0; i < allRecipients.length; i++) {
			recipients.add(allRecipients[i].toString());
		}
		return recipients.toArray(new String[] {});
	}

	/**
	 * 保存符件
	 * 
	 * @param path
	 * @param part
	 * @throws IOException
	 * @throws MessagingException
	 */
	private boolean saveAttachMent(String path, Part part) throws IOException, MessagingException {
		String fileName = "";
		boolean isAttach = false;
		if (part.isMimeType("multipart/*")) {
			Object obj = part.getContent();
			if (!(obj instanceof Multipart)) {
				return false;
			}
			Multipart multipart = (Multipart) obj;
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				String disposition = bodyPart.getDisposition();
				if (disposition != null && (disposition.equals(Part.ATTACHMENT) || disposition.equals(Part.INLINE))) {
					fileName = bodyPart.getFileName();
					if (fileName == null) {
						return false;
					}
					if (fileName.toLowerCase().indexOf("gb2312") != -1) {
						fileName = MimeUtility.decodeText(fileName);
					}
					String suffix = "";
					if (fileName.lastIndexOf(".") != -1) {
						suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
					}

					File file = new File(path + File.separator + sdf.format(new Date()) + suffix);
					if (!file.exists()) {
						file.createNewFile();
					}
					try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
							BufferedInputStream inputStream = new BufferedInputStream(bodyPart.getInputStream())) {
						int c;
						while ((c = inputStream.read()) != -1) {
							outputStream.write(c);
						}
					} catch (IOException e) {
						throw e;
					}
					isAttach = true;
				}
			}
		}
		return isAttach;
	}

	/**
	 * 获取邮件主题
	 * 
	 * @param message
	 * @return
	 * @throws MessagingException
	 */
	private String getSubject(Message message) throws MessagingException {
		return message.getSubject();
	}

	/**
	 * 获取邮件内容
	 * 
	 * @param bodyText
	 * @param part
	 * @throws MessagingException
	 * @throws IOException
	 */
	private void getBody(StringBuffer bodyText, Part part) throws MessagingException, IOException {

		String contentType = part.getContentType();
		int nameIndex = contentType.indexOf("name");
		boolean conName = false;
		if (nameIndex != -1) {
			conName = true;
		}

		if (part.isMimeType("text/plain") && conName == false) {
			bodyText.append((String) part.getContent());
		} else if (part.isMimeType("text/html") && conName == false) {
			bodyText.append((String) part.getContent());
		} else if (part.isMimeType("multipart/*")) {
			Object obj = part.getContent();
			if (obj instanceof Multipart) {
				Multipart multipart = (Multipart) obj;
				int counts = multipart.getCount();
				for (int i = 0; i < counts; i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					getBody(bodyText, bodyPart);
				}
			}
		} else if (part.isMimeType("message/rfc822")) {
			getBody(bodyText, (Part) part.getContent());
		} else {

		}

	}
}
