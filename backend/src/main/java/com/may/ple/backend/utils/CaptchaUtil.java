package com.may.ple.backend.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.log4j.Logger;

import com.anti_captcha.Api.ImageToText;
import com.anti_captcha.Helper.DebugHelper;

public class CaptchaUtil {
	private static final Logger LOG = Logger.getLogger(CaptchaUtil.class.getName());
	
	public static String tesseract(String captchaImg, String baseDir, String tesseractPath, String pythonPath) throws Exception {
		Process process = null;
		BufferedReader reader = null;
		
		try {
			String slash = File.separator;
			String tesseractPathEndSlash = tesseractPath + slash;
			String[] cmd = { 
							pythonPath + slash + "python", 
					        "parse_captcha.py", 
					        captchaImg,
					        tesseractPathEndSlash 
					        };
	    	ProcessBuilder pb = new ProcessBuilder(cmd);
	    	Map<String, String> env = pb.environment();
	    	env.put("TESSDATA_PREFIX", tesseractPathEndSlash + "tessdata");
	    	pb.directory(new File(baseDir));
	    	process = pb.start();
	    	
	    	reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    	String read;
	    	while((read = reader.readLine()) != null) {
	    		if(!read.contains("captcha_txt")) {
	    			LOG.info(read);
	    			continue;
	    		}
	    		
	    		return read.split(":")[1].trim();
	    	}
	    	return "";
		} catch (Exception e) {
			throw e;
		} finally {
			try { if(reader != null) reader.close(); } catch (Exception e2) {}
			try { if(process != null) process.destroy(); } catch (Exception e2) {}
		}
	}
	
	public static String antiCaptcha(String captchaImg) throws Exception {
		try {			
	        DebugHelper.setVerboseMode(true);
	        
	        ImageToText api = new ImageToText();
	        api.setClientKey("253d192c22373024df6180ad1cbe8dc0");
	        api.setFilePath(captchaImg);
	        api.setCase(true);
	        String txt = "";
	        
	        if (!api.createTask()) {
	        	LOG.error("API v2 send failed. " + api.getErrorMessage());
	        } else if (!api.waitForResult()) {
	        	LOG.error("Could not solve the captcha.");
	        } else {
	        	txt = api.getTaskSolution();
	        	LOG.info("Result: " + txt);
	        }
	        
	        return txt;
		} catch (Exception e) {
			throw e;
		}
    }

}