package com.may.ple.backend.bussiness.kys;

import java.net.Proxy;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class KYSApi {
	private static final Logger LOG = Logger.getLogger(KYSApi.class.getName());
	public static final String LINK = "https://www.e-studentloan.ktb.co.th";
	private static final KYSApi instance = new KYSApi();
	private static final int CONN_TIMEOUT = 30000;
	
	private KYSApi(){}
	
	public static KYSApi getInstance(){
        return instance;
    }
	
	public LoginRespModel login(Proxy proxy, String cid, String birthdate, int round) throws Exception {		
		try {
			LOG.info("Start login");
			
			//[1]
			LoginRespModel loginResp = getLoginPage(proxy);
			
			if(loginResp == null) {
				loginResp = new LoginRespModel();
				loginResp.setStatus(StatusConstant.SERVICE_UNAVAILABLE);
				return loginResp;
			}
			
			//[2]
			String text = new Tess4jCaptcha().solve(loginResp.getImageContent());
			
			//[3]
			doLogin(proxy, loginResp, text, cid, birthdate);
			LOG.info((proxy != null ? proxy.toString() : "No Proxy") + " " + loginResp.getStatus() + " for " + text + " round: " + round);
			
			return loginResp;
		} catch (Exception e) {
			LOG.error((proxy != null ? proxy.toString() : "No Proxy") + " " + e.toString());
			throw e;
		}
	}
	

	public LoginRespModel getLoginPage(Proxy proxy) throws Exception {
		try {
			LOG.debug("Start getLoginPage");
			
			Response res = Jsoup
					.connect(LINK + "/STUDENT/ESLLogin.do")
					.proxy(proxy)
					.timeout(CONN_TIMEOUT)
					.method(Method.GET).execute();
			Map<String, String> cookie = res.cookies();
			Document doc = res.parse();
			Elements captchaEl;
			
			if((captchaEl = doc.select("#capId")) == null || captchaEl.size() == 0) {
				return null;
			}
			
			String captchaImgUrl = LINK + captchaEl.get(0).attr("src");
						
			LoginRespModel resp = new LoginRespModel();
			resp.setSessionId(cookie.get("JSESSIONID"));
			resp.setImageContent(getCaptchaImg(proxy, cookie, captchaImgUrl));
						
			return resp;
		} catch (Exception e) {
			LOG.error((proxy != null ? proxy.toString() : "No Proxy") + " " + e.toString());
			throw e;
		}
	}

	private byte[] getCaptchaImg(Proxy proxy, Map<String, String> cookie, String captchaImgUrl) throws Exception {
		try {
			LOG.debug("Start getCaptchaImg");
			
			Response res = Jsoup
					.connect(captchaImgUrl) 	// Extract image absolute URL
					.proxy(proxy)
					.timeout(CONN_TIMEOUT)
					.cookies(cookie) 			// Grab cookies
					.ignoreContentType(true) 	// Needed for fetching image
					.execute();
		
			return res.bodyAsBytes();
		} catch (Exception e) {
			LOG.error((proxy != null ? proxy.toString() : "No Proxy") + " " + e.toString());
			throw e;
		}
	}
	
	private void doLogin(Proxy proxy, LoginRespModel loginResp, String captcha, String cid, String birthdate) throws Exception {
		try {
			LOG.debug("Start doLogin");
			
			Response res = Jsoup.connect(LINK + "/STUDENT/ESLLogin.do")
					.timeout(CONN_TIMEOUT)
					.proxy(proxy)
					.method(Method.POST)
					.data("cid", cid)
					.data("stuBirthdate", birthdate)
					.data("captchar", captcha)
					.data("flag", "S")
					.header("Content-Type", "application/x-www-form-urlencoded")
					.cookie("JSESSIONID", loginResp.getSessionId())
					.postDataCharset("UTF-8")
					.execute();
			
			Document doc = res.parse();
			Elements cifEl = doc.select("td input[name='cif']");
			StatusConstant status;
			String cif = null;
			
			if(cifEl != null && StringUtils.isNoneBlank((cif = cifEl.val()))) {				
				status = StatusConstant.LOGIN_SUCCESS;
			} else if(doc.select("#capId") != null) {
				status = StatusConstant.LOGIN_FAIL;
			} else {
				status = StatusConstant.SERVICE_UNAVAILABLE;
			}
			
			loginResp.setStatus(status);
			loginResp.setCif(cif);
		} catch (Exception e) {
			LOG.error((proxy != null ? proxy.toString() : "No Proxy") + " " + e.toString());
			throw e;
		}
	}
	
}