package com.may.ple.backend.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

import com.may.ple.backend.model.FileDetail;

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

	public static void sendAttach(String subject, String msg, InputStream in, FileDetail fd) throws Exception {
		InputStream is = null;
		
		try {
			MultiPartEmail email = new MultiPartEmail();
		    email.setHostName("smtp.gmail.com");
		    email.setSmtpPort(587);
		    email.setAuthenticator(new DefaultAuthenticator("meta.no.reply@gmail.com", "19042528"));
		    email.setStartTLSEnabled(true);
		    email.setFrom("meta.no.reply@gmail.com");
		    email.setSubject(subject);
		    email.setMsg(msg);
		    email.addTo("meta.no.reply@gmail.com");
		    
		    MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		    String mimeType = mimeTypesMap.getContentType(fd.fileName);
		    
		    is = new BufferedInputStream(in);  
		    DataSource source = new ByteArrayDataSource(is, mimeType);  
		    email.attach(source, fd.fileName, "");
		    
		    email.send();
		} catch (Exception e) {
			throw e;
		} finally {
			if(is != null) is.close();
		}
	}

	/*
	 * public static void main(String[] args) { try { sendSimple("test",
	 * "test"); } catch (Exception e) { e.printStackTrace(); } }
	 */

}
