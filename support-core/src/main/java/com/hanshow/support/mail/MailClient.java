package com.hanshow.support.mail;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.hanshow.support.util.Config;

public class MailClient {

	private MailClient() throws EmailException {
	}

	/**
	 * 发送邮件
	 * 
	 * @param subject
	 * @param content
	 * @param recipients
	 * @throws EmailException
	 */
	public static void send(String subject, String content, String... recipients) throws EmailException {
		Email email = new SimpleEmail();
		email.setMsg(content);
		send(email, subject, recipients);
	}
	
	/**
	 * 发送邮件
	 * 
	 * @param subject
	 * @param content
	 * @param recipients
	 * @throws EmailException
	 */
	public static void send(String subject, String content, File attachment, String... recipients) throws EmailException {
		if (!attachment.exists()) {
			send(subject, subject, recipients);
		} else {
			MultiPartEmail email = new MultiPartEmail();
			email.attach(attachment);
			send(email, subject, recipients);
		}	
	}

	/**
	 * 发送带html模板的邮件
	 * 
	 * @param subject
	 * @param map
	 * @param template
	 * @param translateMap
	 * @param recipients
	 * @throws EmailException
	 */
	public static void sendHtml(String subject, Map<String, Object> map, Template template, Map<String, Object> translateMap, String... recipients) throws EmailException {
		VelocityContext vc = new VelocityContext();
		vc.put("result", map);
		vc.put("translate", translateMap);
		StringWriter sw = new StringWriter();
		template.merge(vc, sw);
		HtmlEmail email = new HtmlEmail();
		email.setHtmlMsg(sw.toString());
		send(email, subject, recipients);
	}

	/**
	 * 发邮件
	 * 
	 * @param email
	 * @param subject
	 * @param recipients
	 * @throws EmailException
	 */
	private static void send(Email email, String subject, String... recipients) throws EmailException {
		Config config = Config.getInstance();
		email.setCharset("utf-8");
		email.setHostName(config.getProperties("mail.smtp.host").toString());
		email.setSmtpPort(Integer.valueOf(config.getProperties("mail.smtp.port").toString()).intValue());
		email.setAuthenticator(new DefaultAuthenticator(config.getProperties("mailbox.address").toString(), config.getProperties("mailbox.password").toString()));
		email.setSSLOnConnect(Boolean.valueOf(config.getProperties("mail.smtp.starttls.enable").toString()).booleanValue());
		email.setFrom(config.getProperties("mailbox.address").toString());
		email.setSubject(subject);
		email.addTo(recipients);
		email.send();
	}

	/**
	 * 加截模板信息属性
	 * 
	 * @param path
	 * @return
	 */
	public static Template loadTemplate(String path) {
		Properties props = new Properties();
		props.setProperty("input.encoding", "utf8");
		props.setProperty("resource.loader", "class");
		props.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
		props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		props.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
		props.put("runtime.log.logsystem.log4j.category", "velocity");
		props.put("runtime.log.logsystem.log4j.logger", "velocity");
		Velocity.init(props);

		return Velocity.getTemplate(path);
	}

	
}
