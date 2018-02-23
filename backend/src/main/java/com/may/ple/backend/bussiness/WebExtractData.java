package com.may.ple.backend.bussiness;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

public class WebExtractData {
	private static final Logger LOG = Logger.getLogger(WebExtractData.class.getName());
	private final int CONN_TIMEOUT = 30000;
	
	public void getWebData(Integer site, List<Map<String, String>> requestData) throws Exception {
		try {
			if(site == 1) {
				getWebData1(requestData);
			} else if(site == 2) {
				getWebData2(requestData);
			} else if(site == 3) {
				getWebData3(requestData);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void getWebData1(List<Map<String, String>> requestData) throws Exception {
		try {
			Response res = Jsoup.connect("http://erm.nhso.go.th/ermsearch/faces/login.jsf")
					.timeout(CONN_TIMEOUT)
					.method(Method.GET)
					.postDataCharset("UTF-8")
					.execute();
			
			res = Jsoup.connect("http://erm.nhso.go.th/ermsearch/faces/login.jsf")
					.timeout(CONN_TIMEOUT)
					.method(Method.POST)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.cookies(res.cookies())
					.data("main_frm_login:username", "chk8161")
					.data("main_frm_login:password", "14042525")
					.data("javax.faces.partial.ajax", "true")
					.postDataCharset("UTF-8")
					.execute();
			
			Document doc = res.parse();
			System.out.println(doc.html());
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void getWebData2(List<Map<String, String>> requestData) {
		
	}
	
	private void getWebData3(List<Map<String, String>> requestData) {
		
	}
	
	public static void main(String[] args) {
		try {
			new WebExtractData().getWebData1(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
