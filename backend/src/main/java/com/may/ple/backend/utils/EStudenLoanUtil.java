package com.may.ple.backend.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.anti_captcha.Api.ImageToText;
import com.anti_captcha.Helper.DebugHelper;

public class EStudenLoanUtil {
	private static String link = "https://www.e-studentloan.ktb.co.th";
	private static String captchaImgFilePath = "D:/DMS_DATA/upload/captcha/Captcha.jpg";
	
	public static void main(String[] args) {
		try {
			String sessionId = login();
			System.out.println("sessionId : " + sessionId);
			
			if(StringUtils.isNoneBlank(sessionId)) {				
				getPaymentInfo(sessionId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String login() throws Exception {
		try {
			//[1]
			String sessionId = getLoginPage();
			
			//[2] 
			String captcha = getCaptchaText();
			
			//[3]
			if(doLogin(sessionId, captcha)) {
				return sessionId;				
			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	private static String getCaptchaText() throws Exception {
		try {
			String txt = "";
			if("local".equals("local")) {				
				txt = tesseract();
			} else {
				txt = antiCaptcha();				
			}
			return txt;
		} catch (Exception e) {
			throw e;
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
	
	private static String getLoginPage() throws Exception {
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
	
	private static boolean doLogin(String sessionId, String captcha) throws Exception {
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
			
			Document doc = res.parse();
			Elements cusName = doc.select("td input[name='stuFullName']");
			boolean status = false;
			
			if(cusName != null && StringUtils.isNoneBlank(cusName.val())) {				
				System.out.println("Login success : " + cusName.val());
				status = true;
			} else if(doc.select("#capId") != null) {				
				System.out.println("Login fail");
			} else {
				System.out.println("Service unavailable");
				System.out.println(doc);
			}			
			
			return status;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private static void getPaymentInfo(String sessionId) throws Exception {
		try {
			Response res = Jsoup.connect(link + "/STUDENT/ESLMTI001.do")
						.method(Method.POST)
					   .data("loanType", "F101")
					   .data("accNo", "1006277854")
					   .data("cif", "")
					   .data("browser", "Fire Fox Or Other")
					   .header("Content-Type", "application/x-www-form-urlencoded")
					   .cookie("JSESSIONID", sessionId)
					   .postDataCharset("UTF-8")
					   .execute();
			
			Document doc = res.parse();
			Elements table = doc.select("#tab4 table table");
			Elements rows = table.select("tr");
			Elements cols;
			boolean isFirstRow = true;
			
			for (Element row : rows) {
				cols = row.select("td");
				
				for (Element col : cols) {
					if(isFirstRow) {
						System.out.print(String.format("%25s", col.select("div").text() + "| "));
						isFirstRow = false;
					} else {
						System.out.print(String.format("%25s", col.text() + "| "));												
					}
				}
				System.out.println();
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
}
