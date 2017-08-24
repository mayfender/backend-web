package com.may.ple.backend.utils;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class LogUtil {
	private static final Logger LOG = Logger.getLogger(LogUtil.class.getName());
	
	public static String getLogFilePath() {
		try {
			InputStream configStream = LogUtil.class.getResourceAsStream( "/log4j.properties");
			
			Properties props = new Properties(); 
			props.load(configStream); 
			
			String path = props.getProperty("log4j.appender.com.may.ple.backend.file");
			LOG.info("Log file path : " + path);
			
			return path;
		} catch (Exception e) {
			LOG.error(e.toString());
			return null;
		}
	}

}
