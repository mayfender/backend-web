package com.may.ple.backend.bussiness;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebExtractData {
	private static final Logger LOG = Logger.getLogger(WebExtractData.class.getName());
	private final int CONN_TIMEOUT = 60000;
	
	public List<Map<String, String>> getWebData1(String username, String password, List<Map<String, String>> requestData) throws Exception {
		try {
			if(StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
				throw new Exception("username or password is null");
			}
			
			String date = String.format(new Locale("th", "TH"), "%1$td/%1$tm/%1$tY", new Date());
			String idNo;
			Document doc;
			Elements viewEls, commonEls, findEls, trs, tds;
			Element table, commonEl;
			
			Response res = Jsoup
					.connect("http://erm.nhso.go.th/ermsearch/faces/login.jsf")
					.method(Method.GET).execute();
			
			LOG.info("Get page");
			Map<String, String> cookies = res.cookies();
			doc = res.parse();
			viewEls = doc.select("input[name='javax.faces.ViewState']");
			String buttonId = doc.select("button").get(0).attr("id");
			
			res = Jsoup.connect("http://erm.nhso.go.th/ermsearch/faces/login.jsf")
					.method(Method.POST)
					.timeout(CONN_TIMEOUT)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.cookies(cookies)
					.postDataCharset("UTF-8")
					.data("javax.faces.partial.ajax", "true")
					.data("javax.faces.source", buttonId)
					.data("javax.faces.partial.execute", "@all")
					.data("javax.faces.partial.render", "main_frm_login:loginCriteria")
					.data(buttonId, buttonId)
					.data("main_frm_login", "main_frm_login")
					.data("main_frm_login:username", username)
					.data("main_frm_login:password", password)
					.data("javax.faces.ViewState", viewEls.get(0).val())
					.execute();
			
			doc = res.parse();
			doc = Jsoup.parse(doc.select("[id='javax.faces.ViewRoot']").text());
			
			String linkId = doc.select("form").get(2).select("a").get(0).attr("id");
			LOG.info("Login");
			res = Jsoup.connect("http://erm.nhso.go.th/ermsearch/faces/user/nextProcess.jsf")
					.method(Method.POST)
					.timeout(CONN_TIMEOUT)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.cookies(cookies)
					.postDataCharset("UTF-8")
					.data("j_idt47", "j_idt47")
					.data("javax.faces.ViewState", viewEls.get(0).val())
					.data("groupId", "01")
					.data(linkId, linkId)
					.execute();
			
			doc = res.parse();
			viewEls = doc.select("input[name='javax.faces.ViewState']");
			LOG.info("--------------------: ready to search :-------------------------");
			
			List<Map<String, String>> result = new ArrayList<>();
			Map<String, String> data;
			
			for (Map<String, String> map : requestData) {
				idNo = map.get("ID Number");
				data = new HashMap<>();
				data.put("ID Number", idNo);
				result.add(data);
				String birthDate="", name="", gender="", status="", dataDate="", right="", issuedDate="", expiredDate="", province="", hospital="";
				
				res = Jsoup.connect("http://erm.nhso.go.th/ermsearch/faces/registration/regInquiryAuthentication.jsf")
						.method(Method.POST)
						.timeout(CONN_TIMEOUT)
						.header("Content-Type", "application/x-www-form-urlencoded")
						.cookies(cookies)
						.postDataCharset("UTF-8")
						.data("javax.faces.partial.ajax", "true")
						.data("javax.faces.source", "regMainForm:btnSearch")
						.data("javax.faces.partial.execute", "@all")
						.data("javax.faces.partial.render", "regMainForm")
						.data("regMainForm:btnSearch", "regMainForm:btnSearch")
						.data("skipvalidation", "1")
						.data("regMainForm", "regMainForm")
						.data("regMainForm:msgCode", "")
						.data("regMainForm:severity", "")
						.data("regMainForm:searchFrom", "NHSO")
						.data("regMainForm:personalId", idNo)
						.data("regMainForm:criteriaBackDate_input", date)
						.data("regMainForm:custom_collapsed", "false")
						.data("regMainForm:tabId1_activeIndex", "0")
						.data("regMainForm:healthTab_activeIndex", "0")
						.data("javax.faces.ViewState", viewEls.get(0).val())
						.execute();
				
				LOG.info("Fetch data " + idNo);
				doc = res.parse();
				commonEls = doc.select("#regMainForm");
				doc = Jsoup.parse(commonEls.text());
				
				//-- Find data table at index 9
				commonEls = doc.select("table");
				if(commonEls.size() < 10) {
					LOG.info("Data not found on " + idNo);
					continue;					
				}
				
				table = commonEls.get(9);
				trs = table.select("tr");
				
				if(trs.size() > 0) {
					tds = trs.get(0).select("td");
					if(tds.size() > 3) {
						birthDate = tds.get(3).select("label").text();						
					}
				}
				if(trs.size() > 1) {
					tds = trs.get(1).select("td");
					if(tds.size() > 1) {
						name = tds.get(1).select("label").text();
					}
					if(tds.size() > 3) {
						gender = tds.get(3).select("label").text();
					}
				}
				
				if(trs.size() > 2) {
					tds = trs.get(2).select("td");
					if(tds.size() > 1) {
						status = tds.get(1).select("label").text();
					}
					if(tds.size() > 3) {
						dataDate = tds.get(3).select("label").text();
					}
				}
				
				//-- Find data table at index 13
				if(commonEls.size() > 13) {
					table = commonEls.get(13);
					trs = table.select("tr");
					
					findEls = trs.select("label:matches(^สิทธิที่ใช้เบิก :)");
					if(findEls.size() > 0) {
						commonEl = findEls.first().parent();
						right = commonEl.nextElementSibling().select("label").text();
					}
					
					findEls = trs.select("label:matches(^วันที่ออกบัตร :)");
					if(findEls.size() > 0) {
						commonEl = findEls.first().parent();
						issuedDate = commonEl.nextElementSibling().select("label").text();
					}
					
					findEls = trs.select("label:matches(^วันบัตรหมดอายุ :)");
					if(findEls.size() > 0) {
						commonEl = findEls.first().parent();
						expiredDate = commonEl.nextElementSibling().select("label").text();
					}
					
					findEls = trs.select("label:matches(^จังหวัดที่ลงทะเบียนรักษา :)");
					if(findEls.size() > 0) {
						commonEl = findEls.first().parent();
						province = commonEl.nextElementSibling().select("label").text();
					}
					
					findEls = trs.select("label:matches(^รพ. รักษา)");
					if(findEls.size() > 0) {
						commonEl = findEls.first().parent();
						hospital = commonEl.nextElementSibling().select("label").text();
					}
					
					data.put("birthDate", birthDate);
					data.put("name", name);
					data.put("gender", gender);
					data.put("status", status);
					data.put("dataDate", dataDate);
					data.put("right", right);
					data.put("issuedDate", issuedDate);
					data.put("expiredDate", expiredDate);
					data.put("province", province);
					data.put("hospital", hospital);
				}
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public List<Map<String, String>> getWebData2(String username, String password, List<Map<String, String>> requestData) throws Exception {
		try {
			Response res = Jsoup
					.connect("http://welcgd.cgd.go.th/cgd_tax/search_psl_dept.jsp")
					.method(Method.GET).execute();
			
			LOG.info("Get page");
			List<Map<String, String>> result = new ArrayList<>();
			Map<String, String> cookies = res.cookies();
			Map<String, String> data;
			Elements tableEl;
			Document doc;
			String idNo;
			Element table;
			Element el;
			
			for (Map<String, String> map : requestData) {
				idNo = map.get("ID Number");
				data = new HashMap<>();
				data.put("ID Number", idNo);
				result.add(data);
				
				res = Jsoup.connect("http://welcgd.cgd.go.th/cgd_tax/search_psl_dept_result.jsp")
						.method(Method.POST)
						.timeout(CONN_TIMEOUT)
						.header("Content-Type", "application/x-www-form-urlencoded")
						.cookies(cookies)
						.postDataCharset("UTF-8")
						.data("PER_ID", idNo)
						.execute();
				
				LOG.info("Fetch data " + idNo);
				doc = res.parse();
				tableEl = doc.select("table table table table table");
				
				if(tableEl.size() == 0) {
					LOG.info("Data not found + " + idNo);
					continue;
				}
				
				table = tableEl.get(0);
				el = table.select("tr").get(0);
				el = el.select("td").get(1);
				data.put("data_1_1", el.text());
				
				el = table.select("tr").get(1);
				el = el.select("td").get(1);
				data.put("data_1_2", el.text());
				
				el = table.select("tr").get(2);
				el = el.select("td").get(1);
				data.put("data_1_3", el.text());
				
				//--------------------------------------
				table = tableEl.get(1);
				el = table.select("tr").get(0);
				el = el.select("td").get(1);
				data.put("data_2_1", el.text());
				
				el = table.select("tr").get(1);
				el = el.select("td").get(1);
				data.put("data_2_2", el.text());
				
				el = table.select("tr").get(2);
				el = el.select("td").get(1);
				data.put("data_2_3", el.text());
				
				//--------------------------------------
				table = tableEl.get(2);
				el = table.select("tr").get(0);
				el = el.select("td").get(1);
				data.put("data_3_1", el.text());
				
				el = table.select("tr").get(1);
				el = el.select("td").get(1);
				data.put("data_3_2", el.text());
				
				el = table.select("tr").get(2);
				el = el.select("td").get(1);
				data.put("data_3_3", el.text());				
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public List<Map<String, String>> getWebData3(String username, String password, List<Map<String, String>> requestData) throws Exception {
		try {
			if(StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
				throw new Exception("username or password is null");
			}
			
			Response res = Jsoup
					.connect("http://tvgcc.truevisionstv.com/TVGWEB/login_revamp.aspx")
					.method(Method.GET).execute();
			
			LOG.info("Get page");
			Map<String, String> cookies = res.cookies();
			String userid = cookies.get("ASP.NET_SessionId");
			Document doc = res.parse();
			Elements viewstate = doc.select("input[name='__VIEWSTATE']");
			Elements eventvalidation = doc.select("input[name='__EVENTVALIDATION']");
			
			res = Jsoup.connect("http://tvgcc.truevisionstv.com/TVGWEB/login_revamp.aspx")
					.method(Method.POST)
					.timeout(CONN_TIMEOUT)
					.cookies(cookies)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.postDataCharset("UTF-8")
					.data("__EVENTTARGET", "lnk")
					.data("__EVENTARGUMENT", "")
					.data("__VIEWSTATE", viewstate.get(0).val())
					.data("__EVENTVALIDATION", eventvalidation.get(0).val())
					.data("txtUsername", username)
					.data("txtPassword", password)
					.data("__ASYNCPOST", "true")
					.execute();
			
			LOG.info("Login");
			
			/*res = Jsoup.connect("http://tvgcc.truevisionstv.com/TVGWEB/Subbase/stock_daily_workorder_revamp.aspx?userid=" + userid)
					.method(Method.GET)
					.timeout(CONN_TIMEOUT)
					.cookies(cookies)
					.execute();*/
			
			res = Jsoup.connect("http://tvgcc.truevisionstv.com/TVGWEB/customerinfo.aspx?userid=" + userid)
					.method(Method.GET)
					.timeout(CONN_TIMEOUT)
					.cookies(cookies)
					.execute();
			
			LOG.info("Get customerinfo page");
			doc = res.parse();
			Elements viewstateForSearch = doc.select("input[name='__VIEWSTATE']");
			Elements eventvalidationForSearch = doc.select("input[name='__EVENTVALIDATION']");
			
			res = Jsoup.connect("http://tvgcc.truevisionstv.com/TVGWEB/agent_customer_search.aspx?userid=" + userid)
					.method(Method.GET)
					.timeout(CONN_TIMEOUT)
					.cookies(cookies)
					.execute();
			
			LOG.info("Get agent_customer_search page popup");
			doc = res.parse();
			viewstate = doc.select("input[name='__VIEWSTATE']");
			eventvalidation = doc.select("input[name='__EVENTVALIDATION']");
			
			//---------------------------------------------------------------
			List<Map<String, String>> result = new ArrayList<>();
			String find = "hideAndSubmit(";
			Map<String, String> data;
			String customerNumber;
			String popupResp;
			Elements els;
			String idNo;
			int start, end;
			
			for (Map<String, String> map : requestData) {
				idNo = map.get("ID Number");
				data = new HashMap<>();
				data.put("ID Number", idNo);
				result.add(data);	
				
				res = Jsoup.connect("http://tvgcc.truevisionstv.com/TVGWEB/agent_customer_search.aspx?userid=" + userid)
						.method(Method.POST)
						.timeout(CONN_TIMEOUT)
						.cookies(cookies)
						.header("Content-Type", "application/x-www-form-urlencoded")
						.postDataCharset("UTF-8")
						.data("ScriptManager1", "ScriptManager1|ibtSearch")
						.data("msgStatus$hdfLevelError", "")
						.data("msgStatus$hdfStatus", "")
						.data("msgStatus$hdfEnableInsertLog", "")
						.data("msgStatus$hdfSetMapError", "true")
						.data("msgStatus$hdfEnableControlMessage", "")
						.data("msgStatus$hdfParam", "")
						.data("txtCustomerNo", "")
						.data("txtBAN", "")
						.data("txtCustomerName", "")
						.data("txtSurname", "")
						.data("txtPhone", "")
						.data("txtIDCard", idNo)
						.data("txtPNumber", "")
						.data("txtApplyRefNo", "")
						.data("txtSerial", "")
						.data("txtEmailAddress", "")
						.data("txtCreditCardNo", "")
						.data("txtValidAddress", "")
						.data("txtUID", "")
						.data("txtTrueMoveNo", "")
						.data("txtTrueLifeNo", "")
						.data("txtFixedLineNo", "")
						.data("__EVENTTARGET", "")
						.data("__EVENTARGUMENT", "")
						.data("__VIEWSTATE", viewstate.get(0).val())
						.data("__EVENTVALIDATION", eventvalidation.get(0).val())
						.data("__ASYNCPOST", "true")
						.data("ibtSearch.x", "0")
						.data("ibtSearch.y", "0")
						.execute();
				
				LOG.info("Submit agent_customer_search page popup");
				doc = res.parse();
				popupResp = doc.text();
				
				if(!popupResp.contains(find)) {
					LOG.info("Data not found on " + idNo);
					continue;
				}
				
				start = popupResp.indexOf(find) + find.length();
				end = popupResp.indexOf(")", start);
				customerNumber = popupResp.substring(start, end);
				//-------------------------------------------------------
				
				res = Jsoup.connect("http://tvgcc.truevisionstv.com/TVGWEB/customerinfo.aspx?userid=" + userid)
						.method(Method.POST)
						.timeout(CONN_TIMEOUT)
						.cookies(cookies)
						.header("Content-Type", "application/x-www-form-urlencoded")
						.postDataCharset("UTF-8")
						.data("ctl00$scmMaster", "tl00$MainContent$udpnCustomer|ctl00$MainContent$ibtSearch")
						.data("__EVENTTARGET", "")
						.data("__EVENTARGUMENT", "")
						.data("__LASTFOCUS", "")
						.data("__VIEWSTATE", viewstateForSearch.get(0).val())
						.data("__SCROLLPOSITIONX", "0")
						.data("__SCROLLPOSITIONY", "0")
						.data("__VIEWSTATEENCRYPTED", "")
						.data("__EVENTVALIDATION", eventvalidationForSearch.get(0).val())
						.data("ctl00$MainContent$hdfAlertMessage", "")
						.data("ctl00$MainContent$txtCustomerNumberCustTab", customerNumber)
						.data("ctl00$MainContent$vdcCustTab_ClientState", "")
						.data("ctl00$MainContent$hdaddressid", "")
						.data("ctl00$MainContent$hdfcustnr", "")
						.data("ctl00$MainContent$hdaddressbillid", "")
						.data("ctl00$MainContent$hdaddressCCBSbillid", "")
						.data("ctl00$MainContent$hdaddressmagid", "")
						.data("ctl00$MainContent$hdaddressdeliverid", "")
						.data("ctl00$MainContent$txt_BouquetName", "")
						.data("ctl00$MainContent$hdfProductSelected", "")
						.data("ctl00$MainContent$hdfWorkOrderSelected", "")
						.data("ctl00$MainContent$hdfOrderSelected", "")
						.data("ctl00$MainContent$hdfTVSFTSelected", "")
						.data("ctl00$MainContent$hdfATB2FTSelected", "")
						.data("ctl00$MainContent$hdfDepositSelected", "")
						.data("ctl00$MainContent$hdfDecoderSelected", "")
						.data("ctl00$MainContent$hdfNoteSelected", "")
						.data("ctl00$MainContent$hdfContactSelected", "")
						.data("ctl00$MainContent$hdfScheduleSelected", "")
						.data("ctl00$MainContent$txtRefresh", "")
						.data("ctl00$MainContent$hdfRefresh", "")
						.data("ctl00$MainContent$TextBox1", "")
						.data("ctl00$MainContent$hdfGetDecoder", "0")
						.data("ctl00$MainContent$hdfNote", "0")
						.data("ctl00$MainContent$hdfContract", "0")
						.data("ctl00$MainContent$hdfKeyword", "0")
						.data("ctl00$MainContent$hdfSchdule", "0")
						.data("ctl00$MainContent$hdfHP", "0")
						.data("ctl00$MainContent$hdfRelation", "0")
						.data("ctl00$MainContent$hdfGetTab", "0")
						.data("ctl00$MainContent$hdfGetTab", "0")
						.data("ctl00$MainContent$hdfATB", "0")
						.data("ctl00$MainContent$hdfccbsoffer", "0")
						.data("ctl00$MainContent$hdfinvhis", "0")
						.data("ctl00$MainContent$hdfpayhis", "0")
						.data("ctl00$MainContent$hdfdeposit", "0")
						.data("ctl00$MainContent$hdfaccountstatement", "0")
						.data("ctl00$MainContent$hdfccbsbill", "0")
						.data("ctl00$MainContent$hdfban", "0")
						.data("ctl00$MainContent$hdfgvCCBSInvoiceHistorySelected", "")
						.data("ctl00$MainContent$hdfSuspentionCount", "0")
						.data("ctl00$MainContent$hdfSuspentionMessage", "")
						.data("__ASYNCPOST", "true")
						.data("ctl00$MainContent$ibtSearch.x", "0")
						.data("ctl00$MainContent$ibtSearch.y", "0")
						.execute();
				
				LOG.info("Submit customerinfo page");
				doc = res.parse();
				
				els = doc.select("[id='MainContent_lblNameDefaultTab']");
				data.put("name", els.size() == 0 ? "" : els.get(0).text());
				
				els = doc.select("[id='MainContent_lblAddressDefaultTab']");
				data.put("addr", els.size() == 0 ? "" : els.get(0).text());
				
				els = doc.select("[id='MainContent_lblPhoneNoDefaultTab']");
				data.put("phone", els.size() == 0 ? "" : els.get(0).text());
				
				els = doc.select("[id='MainContent_lblOfficePhoneDefaultTab']");
				data.put("phoneOffice", els.size() == 0 ? "" : els.get(0).text());
				
				els = doc.select("[id='MainContent_lblFaxMobileDefaultTab']");
				data.put("faxMobile", els.size() == 0 ? "" : els.get(0).text());
			}
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	/*public static void main(String[] args) {
		try {
			List<Map<String, String>> reqData = new ArrayList<>();
			
			HashMap<String, String> map = new HashMap<>();
			map.put("ID Number", "3180400401235");
			reqData.add(map);
						
//			List<Map<String, String>> result = new WebExtractData().getWebData3(reqData);
//			LOG.info(result);
					    
			FileOutputStream fileOut = new FileOutputStream("C:\\Users\\LENOVO\\Desktop\\test\\test.xlsx");		    
		    ExcelGenerator gen = new WebReport3Impl("3");
		    gen.createReport(null, fileOut);
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

}