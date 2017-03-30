package com.may.ple.backend.utils;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

public class EmailUtil {
	
	public static void sendSimple(String subject, String msg) {
		try {
		    Email email = new SimpleEmail();
		    email.setHostName("smtp.gmail.com");
		    email.setSmtpPort(587);
		    email.setAuthenticator(new DefaultAuthenticator("mayfender.debt@gmail.com", "19042528"));
		    email.setStartTLSEnabled(true);
		    email.setFrom("mayfender.debt@gmail.com");
		    email.setSubject(subject);
		    email.setMsg(msg);
		    email.addTo("mayfender.debt@gmail.com");
		    email.send();
		} catch (Exception e) {
			e.printStackTrace();
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
