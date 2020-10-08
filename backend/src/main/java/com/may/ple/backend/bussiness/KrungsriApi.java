package com.may.ple.backend.bussiness;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.security.crypto.codec.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.util.Calendar;
import com.may.ple.backend.entity.DymListDet;
import com.may.ple.backend.service.DymListService;

public class KrungsriApi {
	private static final Logger LOG = Logger.getLogger(KrungsriApi.class.getName());
	private static final KrungsriApi instance = new KrungsriApi();
	private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(10 * 1000).build();

//	private static final String URL_HEALTH_CHK = "https://sandbox.apiauto.krungsri.com/auto/collection/cr/tracking/health";
//	private static final String URL_UPLOAD = "https://sandbox.apiauto.krungsri.com/auto/collection/cr/tracking";
//	private static final String URL_OAUTH = "https://sandbox.apiauto.krungsri.com/auth/oauth/v2/token";

	public String oauthURL;
	public String uploadURL;
	public String healthChkURL;
	public String clientID;
	public String clientSecret;
	public String scope;
	public String apiKey;
	public String signatureKey;
	public String algorithm;

	private KrungsriApi() {}

	public static KrungsriApi getInstance(){
        return instance;
    }

	public void initParams(Map params) {
		this.setOauthURL(params.get("oauthURL").toString());
		this.setUploadURL(params.get("uploadURL").toString());
		this.setHealthChkURL(params.get("healthChkURL").toString());
		this.setClientID(params.get("clientID").toString());
		this.setClientSecret(params.get("clientSecret").toString());
		this.setScope(params.get("scope").toString());
		this.setApiKey(params.get("apiKey").toString());
		this.setSignatureKey(params.get("signatureKey").toString());
		this.setAlgorithm(params.get("algorithm").toString());
	}

/*	public static void main(String[] args) throws Exception {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("entity", "AY");
			data.put("companyCode", "GECAL");
			data.put("product", "MC");
			data.put("branch", "22");
			data.put("contractNumber", "2224416");
			data.put("message", "0800041518   หลานผช.ตต.มาแจ้งชำระแล้วเซเว่นยอด 500 บ.");
			data.put("userName", "sarunnuch");
			data.put("actionCode", "TM");
			data.put("actionDatetime", "15/07/2563 11:58:38");
			data.put("resultCode", "PD");
			data.put("recallCode", "RD");
			data.put("recallDate", "17/07/2563");
			data.put("recallTime", "11:58:38");
			data.put("ppAmount", "0.00");
			data.put("supCode", "A075");
			data.put("transactionDatetime", "15/07/2563 11:58:38");

			KrungsriApi ins = KrungsriApi.getInstance();
			JsonObject jsonObj = ins.healthCheck(data);

			System.out.println(jsonObj.toString());

//			KrungsriApi ins = KrungsriApi.getInstance();
//			String str = ins.getOAuth();
//			System.out.println(str);
		} catch(Exception e) {
			throw e;
		}
	}*/

	public JsonObject uploadJson(Map<String, Object> data) throws Exception {
		try {
			//---: Do first Login.
			LOG.info("Do first login");
			String authorization = getOAuth();
			LOG.info(authorization);

			//---: Prepare parameters.
			LOG.info(data);
			Date now = Calendar.getInstance().getTime();
			String body = getBodyJsonReq(data);
			LOG.info(body);

			String nowAsISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(now);
			String transactionId = UUID.randomUUID().toString();
			String signature = doHmacSha256Base64V2ForEform(body, transactionId, nowAsISO);

			//---: Push data to cloud server.
			return uploadTrackingJson(body, transactionId, nowAsISO, signature, authorization);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public JsonObject uploadSoap(Map<String, Object> data) throws Exception {
		try {
			//---: Do first Login.
			LOG.info("Do first login");
			String authorization = getOAuth();
			LOG.info(authorization);

			//---: Prepare parameters.
			LOG.info(data);
			Date now = Calendar.getInstance().getTime();
			String body = getBodySoapReq(data);
			LOG.info(body);

			String nowAsISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(now);
			String transactionId = UUID.randomUUID().toString();
			String signature = doHmacSha256Base64V2ForEform(body, transactionId, nowAsISO);

			//---: Push data to cloud server.
			return uploadTrackingJson(body, transactionId, nowAsISO, signature, authorization);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<String> getTaskDetailFields(String dataFormat) {
		String[] fields = dataFormat.replaceAll("\\r|\\n", "").split(",");
		List<String> taskDetailFields = new ArrayList<>();
		String[] format, subValue;
		String value, child;

		for (String field : fields) {
			format = field.split("=");
			value = format[1];
			if(value.contains("${")) {
				subValue = value.replace("${", "").replace("}", "").split("\\.");

				if(subValue.length > 1) {
					child = subValue[1];
					taskDetailFields.add(child);
				}
			}
		}
		return taskDetailFields;
	}

	public Map prepareData(Map<String, Object> traceWork, String dataFormat, String productId, Map<String, String> user, DymListService dymService) throws Exception {
		try {
			Map<String, Object> data = new HashMap<>();
			String[] fields = dataFormat.replaceAll("\\r|\\n", "").split(",");
			String header, value, parent, child;
			String[] format, subValue;
			DymListDet dymListDet;
			Object dymDeId, valueObj;
			Date createdDateTime = (Date)traceWork.get("createdDateTime");

			for (String field : fields) {
				format = field.split("=");
				header = format[0]; value = format[1];
				if(value.contains("${")) {
					subValue = value.replace("${", "").replace("}", "").split("\\.");
					parent = subValue[0];

					if(subValue.length > 1) {
						child = subValue[1];

						valueObj = ((Map)traceWork.get(parent)).get(child);
						if(valueObj != null) {
							value = valueObj.toString();
						} else {
							value = "";
						}
					} else {
						if(parent.equals("user")) {
							valueObj = user.get("firstName");
						} else {
							valueObj = traceWork.get(parent);
						}

						if(valueObj != null) {
							value = valueObj.toString();
						} else {
							value = "";
						}
					}

					if(header.equals("entity")) {
						data.put(header, "AY");
					} else {
						data.put(header, value);
					}
				} else {
					if(value.startsWith("link_")) {
						dymDeId = traceWork.get(value.replace("link_", ""));
						if(dymDeId != null) {
							dymListDet = dymService.findListDetById(productId, dymDeId.toString());
							data.put(header, StringUtils.isNotBlank(dymListDet.getCode()) ? dymListDet.getCode() : dymListDet.getMeaning());
						}
					} else {
						if(header.equals("actionDatetime")) {
							value = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("th", "TH")).format(createdDateTime);
						} else if(header.equals("recallDate")) {
							valueObj = traceWork.get("appointDate");
							if(valueObj != null) {
								value = new SimpleDateFormat("dd/MM/yyyy", new Locale("th", "TH")).format(valueObj);
							} else {
								value = "";
							}
						} else if(header.equals("recallTime")) {
							valueObj = traceWork.get("appointDate");
							if(valueObj != null) {
								value = new SimpleDateFormat("HH:mm:ss", new Locale("th", "TH")).format(valueObj);
							} else {
								value = "";
							}
						} else if(header.equals("ppAmount")) {
							valueObj = traceWork.get("appointAmount");
							if(valueObj != null) {
								value = String.format("%.2f", valueObj);
							} else {
								value = String.format("%.2f", 0.0);
							}
						} else if(header.equals("transactionDatetime")) {
							value = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("th", "TH")).format(createdDateTime);
						}
						data.put(header, value);
					}
				}
			}

			/*if(user != null) {
				LOG.info("Set userName");
				data.put("userName", user.get("firstName"));
			}*/

			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private JsonObject healthCheck(Map<String, Object> data, String authorization) throws Exception {
		try {
			//---: Prepare parameters.
			Date now = Calendar.getInstance().getTime();
			String body = getBodyJsonReq(data);
			String nowAsISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(now);
			String transactionId = UUID.randomUUID().toString();
			String signature = doHmacSha256Base64V2ForEform(body, transactionId, nowAsISO);

			//---: Push data to cloud server.
			return healthCheckSend(body, transactionId, nowAsISO, signature, authorization);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private JsonObject uploadTrackingJson(String body, String transactionId, String nowAsISO, String signature, String authorization) throws Exception {
		LOG.info("Start upload");

		try {
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setDefaultRequestConfig(REQUEST_CONFIG);

			CloseableHttpClient httpClient = builder.build();
			HttpPost httpPost = new HttpPost(uploadURL);
			httpPost.addHeader("Authorization", authorization);
			httpPost.addHeader("API-Key", apiKey);
			httpPost.addHeader("content-type", "application/json; charset=utf8");
			httpPost.addHeader("X-Client-Transaction-ID", transactionId);
			httpPost.addHeader("X-Client-DateTime", nowAsISO);
			httpPost.addHeader("X-Signature", signature);

			httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			HttpResponse response = httpClient.execute(httpPost);

			return jsonParserUpload(response);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			LOG.info("End upload");
		}
	}

	private JsonObject uploadTrackingSoap(String body, String transactionId, String nowAsISO, String signature, String authorization) throws Exception {
		LOG.info("Start upload");

		try {
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setDefaultRequestConfig(REQUEST_CONFIG);

			CloseableHttpClient httpClient = builder.build();
			HttpPost httpPost = new HttpPost(uploadURL);
			httpPost.addHeader("Authorization", authorization);
			httpPost.addHeader("API-Key", apiKey);
			httpPost.addHeader("content-type", "application/soap+xml; charset=utf8");
			httpPost.addHeader("X-Client-Transaction-ID", transactionId);
			httpPost.addHeader("X-Client-DateTime", nowAsISO);
			httpPost.addHeader("X-Signature", signature);

			httpPost.setEntity(new StringEntity(body, "text/xml"));
			HttpResponse response = httpClient.execute(httpPost);

			return jsonParserUpload(response);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			LOG.info("End upload");
		}
	}

	private JsonObject healthCheckSend(String body, String transactionId, String nowAsISO, String signature, String authorization) throws Exception {
		LOG.debug("Start upload");

		try {
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setDefaultRequestConfig(REQUEST_CONFIG);

			CloseableHttpClient httpClient = builder.build();
			HttpPost httpPost = new HttpPost(healthChkURL);
			httpPost.addHeader("Authorization", authorization);
			httpPost.addHeader("API-Key", apiKey);
			httpPost.addHeader("content-type", "application/json; charset=utf8");
			httpPost.addHeader("X-Client-Transaction-ID", transactionId);
			httpPost.addHeader("X-Client-DateTime", nowAsISO);
			httpPost.addHeader("X-Signature", signature);

			httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			HttpResponse response = httpClient.execute(httpPost);

			return jsonParserUpload(response);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			LOG.debug("End upload");
		}
	}

	private synchronized String getOAuth() throws Exception {
		try {
			/*if(!StringUtils.isBlank(this.authorization)) {
				LOG.info("Ignore First auth");
				return;
			}*/

			//---------------
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setDefaultRequestConfig(REQUEST_CONFIG);

			CloseableHttpClient httpClient = builder.build();
			HttpPost httpPost = new HttpPost(oauthURL);

			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("client_id", clientID));	//---: Fixed key provided by API-M
			nvps.add(new BasicNameValuePair("client_secret", clientSecret));	//---: Fixed key provided by API-M
			nvps.add(new BasicNameValuePair("scope", scope));						//---: Fixed key provided by API-M
			nvps.add(new BasicNameValuePair("grant_type", "client_credentials"));					//---: Fixed key provided by API-M

			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
			JsonObject oAuth = jsonParser(httpClient.execute(httpPost));

			String accessToken = oAuth.get("access_token").getAsString();
			String tokenType = oAuth.get("token_type").getAsString();

			LOG.debug("End upload");
			return tokenType + " " + accessToken;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private String doHmacSha256Base64V2ForEform(String body, String transactionId, String timestampUtcIso8601) throws Exception {
	    String payload = body + transactionId + timestampUtcIso8601;
	    byte[] hexKey = signatureKey.getBytes("UTF-8");
	    byte[] payloadBytes = payload.getBytes("UTF-8");

	    Mac sha256HMAC = Mac.getInstance(algorithm);
	    SecretKeySpec secretKey = new SecretKeySpec(hexKey, algorithm);
	    sha256HMAC.init(secretKey);

	    //warning: a Mac instance is not thread safe
	    byte[] hmacByte = sha256HMAC.doFinal(payloadBytes);
	    return new String(Base64.encode(hmacByte));
	}

	private JsonObject jsonParser(HttpResponse response) throws Exception {
		try {
			String resultStr = getResultStr(response);
			JsonElement jsonElement =  new JsonParser().parse(resultStr);
			return jsonElement.getAsJsonObject();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private JsonObject jsonParserUpload(HttpResponse response) throws Exception {
		try {
			Map<String, Object> resultMap = jsonStrUpload(response);

			int httpStatus = (int)resultMap.get("httpStatus");
			LOG.info("httpStatus : " + httpStatus);

			JsonElement jsonElement =  new JsonParser().parse(resultMap.get("responseJsonStr").toString());
			return jsonElement.getAsJsonObject();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private String getResultStr(HttpResponse response) throws Exception {
		try {
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output, result = "";

			while ((output = br.readLine()) != null) {
				result += output;
			}
			br.close();

			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private Map<String, Object> jsonStrUpload(HttpResponse response) throws Exception {
		try {
			int statusCode = response.getStatusLine().getStatusCode();
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output, result = "";

			while ((output = br.readLine()) != null) {
				result += output;
			}
			br.close();

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("httpStatus", statusCode);
			resultMap.put("responseJsonStr", result);

			LOG.info(resultMap);

			return resultMap;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private String getBodyJsonReq(Map<String, Object> data) {
		JsonObject jsonObject = new JsonObject();
		Object val;

		for (Map.Entry<String, Object> keyVal : data.entrySet()) {
			val = keyVal.getValue();
			jsonObject.addProperty(keyVal.getKey(), val == null ? "" : val.toString());
		}

		return jsonObject.toString();
	}

	private String getBodySoapReq(Map<String, Object> data) {
		JsonObject jsonObject = new JsonObject();
		Object val;

		for (Map.Entry<String, Object> keyVal : data.entrySet()) {
			val = keyVal.getValue();
			jsonObject.addProperty(keyVal.getKey(), val == null ? "" : val.toString());
		}

		return jsonObject.toString();
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getSignatureKey() {
		return signatureKey;
	}

	public void setSignatureKey(String signatureKey) {
		this.signatureKey = signatureKey;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getOauthURL() {
		return oauthURL;
	}

	public void setOauthURL(String oauthURL) {
		this.oauthURL = oauthURL;
	}

	public String getUploadURL() {
		return uploadURL;
	}

	public void setUploadURL(String uploadURL) {
		this.uploadURL = uploadURL;
	}

	public String getHealthChkURL() {
		return healthChkURL;
	}

	public void setHealthChkURL(String healthChkURL) {
		this.healthChkURL = healthChkURL;
	}

}
