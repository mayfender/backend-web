package com.may.ple.backend.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

public class ImageUtil {
	private static final Logger LOG = Logger.getLogger(ImageUtil.class.getName());
	
	public static byte[] getDefaultThumbnail(ServletContext servletContext) throws Exception {
		try {
			String defaultThumnailPath = servletContext.getRealPath("/app/images/default-thumbnail.png");
			
			Path path = Paths.get(defaultThumnailPath);
			byte[] data = Files.readAllBytes(path);
			
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
