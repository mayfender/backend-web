package com.may.ple.backend.license;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import net.nicholaswilliams.java.licensing.encryption.PublicKeyDataProvider;
import net.nicholaswilliams.java.licensing.exception.KeyNotFoundException;

import org.apache.log4j.Logger;

public class MyPublicKeyProvider implements PublicKeyDataProvider {
	private static final Logger LOG = Logger.getLogger(MyPublicKeyProvider.class.getName());
	private ServletContext servletContext;
	
	public MyPublicKeyProvider(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	@Override
	public byte[] getEncryptedPublicKeyData() throws KeyNotFoundException {
		try {			
			String publickey = servletContext.getRealPath("/app/key/public.key");
			return Files.readAllBytes(Paths.get(publickey));
		} catch (Exception e) {
			LOG.error(e.toString());
			return null;
		}
	}

}
