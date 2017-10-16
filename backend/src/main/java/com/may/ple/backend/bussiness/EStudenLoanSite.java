package com.may.ple.backend.bussiness;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.anti_captcha.Api.ImageToText;
import com.anti_captcha.Helper.DebugHelper;
import com.fasterxml.uuid.Generators;

public class EStudenLoanSite {
	private static final Logger LOG = Logger.getLogger(EStudenLoanSite.class.getName());
	private enum LOGIN_STATUS {SERVICE_UNAVAILABLE, FAIL, SUCCESS};
	private enum IMG_TO_TXT {ANTI_CAPTCHA, LOCAL};
	private static IMG_TO_TXT service = IMG_TO_TXT.LOCAL;
	private static String LINK = "https://www.e-studentloan.ktb.co.th";
	private static String captchaPath = "D:/DMS_DATA/upload/temp/";
	private static final EStudenLoanSite instance = new EStudenLoanSite();
	
	private EStudenLoanSite() {}
	
	public static EStudenLoanSite getInstance(){
        return instance;
    }
	
	public static void main(String[] args) {
		try {
			EStudenLoanSite estudent = EStudenLoanSite.getInstance();
			
			String cid = "1-8013-00030-41-1";
			String birthdate = "19/04/2528";
			String sessionId = "0";
			
			while(sessionId.equals("0")) {
				sessionId = estudent.login(cid, birthdate);
				LOG.debug("sessionId : " + sessionId);
				
				if(sessionId.equals("-1")) {
					LOG.error("Service Unavailable");
					Thread.sleep(300000);
				} else if(sessionId.equals("0")) {
					LOG.warn("Login fail");
				}
			}
			
			LOG.info("Login Success");
			estudent.getPaymentInfo(sessionId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String login(String cid, String birthdate) throws Exception {
		String captchaFullPath = null;
		
		try {
			//[1]
			Map<String, String> loginResp = getLoginPage();
			
			if(loginResp == null) return "-1";
			
			//[2]
			String sessionId = loginResp.get("JSESSIONID");
			captchaFullPath = loginResp.get("CAPTCHA_FULL_PATH");
			String captcha = getCaptchaText(captchaFullPath);
			
			//[3]
			LOGIN_STATUS status = doLogin(sessionId, captcha, cid, birthdate);
			if(status == LOGIN_STATUS.SUCCESS) {
				return sessionId;				
			} else if (status == LOGIN_STATUS.FAIL) {
				return "0";
			} else {
				return "-1";
			}
		} catch (Exception e) {
			FileUtils.deleteQuietly(new File(captchaFullPath));
			throw e;
		}
	}
	
	private String getCaptchaText(String captchaImg) throws Exception {
		try {
			String txt = "";
			
			if(service == IMG_TO_TXT.LOCAL) {				
				txt = tesseract(captchaImg);
			} else if(service == IMG_TO_TXT.ANTI_CAPTCHA) {
				txt = antiCaptcha(captchaImg);				
			}
			return txt;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private String tesseract(String captchaImg) throws Exception {
		Process process = null;
		BufferedReader reader = null;
		try {
			String path = "D:/python_captcha/";
			String tesseractPath = "C:/Program Files (x86)/Tesseract-OCR/";
			String pythonExePath = "C:\\Users\\sarawuti\\AppData\\Local\\Programs\\Python\\Python36-32\\python";
			
			String[] cmd = { pythonExePath, 
					         "parse_captcha.py", 
					         captchaImg,
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
	
	private String antiCaptcha(String captchaImg) throws Exception {
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
	
	private Map<String, String> getLoginPage() throws Exception {
		try {
			Response res = Jsoup
					.connect(LINK + "/STUDENT/ESLLogin.do")
					.method(Method.GET).execute();
			Map<String, String> cookie = res.cookies();
			Document doc = res.parse();
			Elements captchaEl = doc.select("#capId");
			
			if((captchaEl = doc.select("#capId")) == null) {
				return null;
			}
			
			String captchaImgUrl = LINK + captchaEl.get(0).attr("src");
			
			cookie.put("CAPTCHA_FULL_PATH", getCaptchaImg(cookie, captchaImgUrl));
						
			return cookie;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private String getCaptchaImg(Map<String, String> cookie, String captchaImgUrl) throws Exception {
		try {
			// Fetch the captcha image
			Response res = Jsoup
					.connect(captchaImgUrl) 	// Extract image absolute URL
					.cookies(cookie) 			// Grab cookies
					.ignoreContentType(true) 	// Needed for fetching image
					.execute();
	
			UUID uuid = Generators.timeBasedGenerator().generate();
			String captchaFullPath = captchaPath + uuid + ".jpg";
			
			// Load image from Jsoup response
			ImageIO.write(ImageIO.read(new ByteArrayInputStream(res.bodyAsBytes())), "jpg", new File(captchaFullPath));
			
			return captchaFullPath;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private LOGIN_STATUS doLogin(String sessionId, String captcha, String cid, String birthdate) throws Exception {
		try {
			Response res = Jsoup.connect(LINK + "/STUDENT/ESLLogin.do")
					.method(Method.POST)
					.data("cid", cid)
					.data("stuBirthdate", birthdate)
					.data("captchar", captcha)
					.data("flag", "S")
					.header("Content-Type", "application/x-www-form-urlencoded")
					.cookie("JSESSIONID", sessionId)
					.postDataCharset("UTF-8")
					.execute();
			
			Document doc = res.parse();
			Elements cusName = doc.select("td input[name='stuFullName']");
			LOGIN_STATUS status = LOGIN_STATUS.SUCCESS;
			
			if(cusName != null && StringUtils.isNoneBlank(cusName.val())) {				
				LOG.info("Login success : " + cusName.val());
			} else if(doc.select("#capId") != null) {
				status = LOGIN_STATUS.FAIL;
			} else {
				status = LOGIN_STATUS.SERVICE_UNAVAILABLE;
			}			
			
			return status;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void getPaymentInfo(String sessionId) throws Exception {
		try {
			Response res = Jsoup.connect(LINK + "/STUDENT/ESLMTI001.do")
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
