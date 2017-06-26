package com.may.ple.backend.utils;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

public class EmailUtil {
	
	public static void sendSimple(String subject, String msg) throws Exception {
		try {
		    Email email = new SimpleEmail();
		    email.setHostName("smtp.gmail.com");
		    email.setSmtpPort(587);
		    email.setAuthenticator(new DefaultAuthenticator("meta.no.reply@gmail.com", "19042528"));
		    email.setStartTLSEnabled(true);
		    email.setFrom("meta.no.reply@gmail.com");
		    email.setSubject(subject);
		    email.setMsg(msg);
		    email.addTo("meta.no.reply@gmail.com");
		    email.send();
		} catch (Exception e) {
			throw e;
		}
	}
	
	/*public static void main(String[] args) {
		try {
			sendSimple("test", "test");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

}
