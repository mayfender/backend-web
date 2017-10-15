package com.may.ple.backend.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.anti_captcha.Api.ImageToText;
import com.anti_captcha.Helper.DebugHelper;

public class EStudenLoanUtil {
	private static String link = "https://www.e-studentloan.ktb.co.th";
	private static String captchaImgFilePath = "D:/DMS_DATA/upload/captcha/Captcha.jpg";
	
	public static void main(String[] args) {
		FileInputStream imgInputstream = null;
		
		try {
			//[1]
			String sessionId = loginPage();
			
			//[2]
			String txt = "";
			if("local".equals("local")) {				
				txt = tesseract();
			} else {
				txt = antiCaptcha();				
			}
			
			//[3]
			doLogin(sessionId, txt);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(imgInputstream != null) imgInputstream.close();				
			} catch (Exception e2) {}
		}
	}
	
	private static String tesseract() throws Exception {
		Process process = null;
		BufferedReader reader = null;
		try {
			String path = "D:/DMS_DATA/upload/captcha/";
			String tesseractPath = "C:/Program Files (x86)/Tesseract-OCR/";
			String pythonExePath = "C:\\Users\\mayfender\\AppData\\Local\\Programs\\Python\\Python36-32\\python";
			
			String[] cmd = { pythonExePath, 
					         "parse_captcha.py", 
					         "Captcha.jpg",
					         tesseractPath };
	    	ProcessBuilder pb = new ProcessBuilder(cmd);
	    	Map<String, String> env = pb.environment();
	    	env.put("TESSDATA_PREFIX", tesseractPath + "tessdata");
	    	pb.directory(new File(path));
	    	process = pb.start();
	    	
	    	reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    	String read;
	    	while((read = reader.readLine()) != null) {
	    		if(!read.contains("captcha_txt")) {
	    			System.out.println(read);
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
	
	private static String antiCaptcha() throws Exception {
		try {			
	        DebugHelper.setVerboseMode(true);
	
	        ImageToText api = new ImageToText();
	        api.setClientKey("253d192c22373024df6180ad1cbe8dc0");
	        api.setFilePath(captchaImgFilePath);
	        api.setCase(true);
	        String txt = "";
	        
	        if (!api.createTask()) {
	            DebugHelper.out(
	                    "API v2 send failed. " + api.getErrorMessage(),
	                    DebugHelper.Type.ERROR
	            );
	        } else if (!api.waitForResult()) {
	            DebugHelper.out("Could not solve the captcha.", DebugHelper.Type.ERROR);
	        } else {
	        	txt = api.getTaskSolution();
	            DebugHelper.out("Result: " + txt, DebugHelper.Type.SUCCESS);
	        }
	        
	        return txt;
		} catch (Exception e) {
			throw e;
		}
    }
	
	private static String loginPage() throws Exception {
		try {
			Response res = Jsoup.connect(link + "/STUDENT/ESLLogin.do").method(Method.GET).execute();
			Map<String, String> cookie = res.cookies();
			Document doc = res.parse();
			Elements captchaEl = doc.select("#capId");
			String captchaImg = link + captchaEl.get(0).attr("src");

//			System.out.println(captchaEl.get(0).attr("src"));

			// Fetch the captcha image
			res = Jsoup //
					.connect(captchaImg) // Extract image absolute URL
					.cookies(cookie) // Grab cookies
					.ignoreContentType(true) // Needed for fetching image
					.execute();

			// Load image from Jsoup response
			ImageIO.write(ImageIO.read(new ByteArrayInputStream(res.bodyAsBytes())), "jpg",
					new File(captchaImgFilePath));
			
			return cookie.get("JSESSIONID");
		} catch (Exception e) {
			throw e;
		}
	}
	
	private static void doLogin(String sessionId, String captcha) throws Exception {
		try {
			Response res = Jsoup.connect(link + "/STUDENT/ESLLogin.do")
						.method(Method.POST)
					   .data("cid", "1-8013-00030-41-1")
					   .data("stuBirthdate", "19/04/2528")
					   .data("captchar", captcha)
					   .data("flag", "S")
					   .header("Content-Type", "application/x-www-form-urlencoded")
					   .cookie("JSESSIONID", sessionId)
					   .postDataCharset("UTF-8")
					   .execute();
			
			Document document = res.parse();
			Elements cusName = document.select("td input[name='stuFullName']");
			
			if(cusName != null) {				
				System.out.println("Login success : " + cusName.val());
			} else {
				System.out.println("Login fail");
			}			
		} catch (Exception e) {
			throw e;
		}
	}
	
}
