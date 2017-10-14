package com.may.ple.backend.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.anti_captcha.Api.ImageToText;
import com.anti_captcha.Helper.DebugHelper;

public class HtmlParserUtil {
	private static String link = "https://www.e-studentloan.ktb.co.th";
	private static String captchaImgFilePath = "D:\\DMS_DATA\\upload\\temp\\Captcha.jpg";
	
	public static void mainTest(String[] args) {
		FileInputStream imgInputstream = null;
		
		try {
			//[1]
			String sessionId = loginPage();
			
			//[2]
			String txt = imageToText();
			
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
	
	private static String imageToText() throws Exception {
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

			System.out.println(captchaEl.get(0).attr("src"));

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
			System.out.println("captcha : " + captcha);
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
			
			System.out.println(res.body());
		} catch (Exception e) {
			throw e;
		}
	}
	
}
