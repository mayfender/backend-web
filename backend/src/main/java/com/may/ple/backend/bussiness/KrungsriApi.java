package com.may.ple.backend.bussiness;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.security.crypto.codec.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.util.Calendar;

public class KrungsriApi {
	private static final Logger LOG = Logger.getLogger(KrungsriApi.class.getName());
	private static final KrungsriApi instance = new KrungsriApi();
	private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(5 * 1000).build();
	private static final String URL_UPLOAD = "https://sandbox.apiauto.krungsri.com/auto/collection/cr/tracking";
	private static final String URL_OAUTH = "https://sandbox.apiauto.krungsri.com/auth/oauth/v2/token";
	private static final String ALGORITHM = "HmacSHA256";
	private static final String SIGNATURE_KEY = "85EE229A2159F9A84A6627E0EAE14D5D"; //Fixed key provided by API-M
	private static final String API_KEY = ""; //Fixed key provided by API-M
	private String authorization;

	private KrungsriApi() {}

	public static KrungsriApi getInstance(){
        return instance;
    }

	public JsonObject upload(Map<String, Object> data) throws Exception {
		try {
			//---: Do first Login.
			LOG.info("Do first login");
			getOAuth();
			//TODO: To prevent case expires error.

			//---: Prepare parameters.
			Date now = Calendar.getInstance().getTime();
			String body = getBodyJsonString(data);
			String nowAsISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(now);
			String transactionId = UUID.randomUUID().toString();
			String signature = doHmacSha256Base64V2ForEform(body, transactionId, nowAsISO);

			//---: Push data to cloud server.
			return uploadTracking(body, transactionId, nowAsISO, signature, this.authorization);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private JsonObject uploadTracking(String body, String transactionId, String nowAsISO, String signature, String authorization) throws Exception {
		LOG.debug("Start upload");

		try {
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setDefaultRequestConfig(REQUEST_CONFIG);

			CloseableHttpClient httpClient = builder.build();
			HttpPost httpPost = new HttpPost(URL_UPLOAD);
			httpPost.addHeader("Authorization", authorization);
			httpPost.addHeader("API-Key", API_KEY);
			httpPost.addHeader("content-type", "application/json; charset=utf8");
			httpPost.addHeader("X-Client-Transaction-ID", transactionId);
			httpPost.addHeader("X-Client-DateTime", nowAsISO);
			httpPost.addHeader("X-Signature", signature);
			httpPost.setEntity(new StringEntity(body, "utf8"));

			HttpResponse response = httpClient.execute(httpPost);
			return jsonParserUpload(response);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			LOG.debug("End upload");
		}
	}

	private synchronized void getOAuth() throws Exception {
		try {
			if(!StringUtils.isBlank(this.authorization)) {
				LOG.info("Ignore First auth");
				return;
			}

			//---------------
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setDefaultRequestConfig(REQUEST_CONFIG);

			CloseableHttpClient httpClient = builder.build();
			HttpPost httpPost = new HttpPost(URL_OAUTH);

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("client_id", "");     	//---: Fixed key provided by API-M
			jsonObject.addProperty("client_secret", "");	//---: Fixed key provided by API-M
			jsonObject.addProperty("scope", "");			//---: Fixed key provided by API-M
			jsonObject.addProperty("grant_type", "client_credentials");
			String body = jsonObject.toString();
			httpPost.setEntity(new StringEntity(body, "utf8"));

			JsonObject oAuth = jsonParser(httpClient.execute(httpPost));
			String accessToken = oAuth.get("access_token").getAsString();
			String tokenType = oAuth.get("token_type").getAsString();
			this.authorization = tokenType + " " + accessToken;

			LOG.debug("End upload");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private String doHmacSha256Base64V2ForEform(String body, String transactionId, String timestampUtcIso8601) throws Exception {
	    String payload = body + transactionId + timestampUtcIso8601;
	    byte[] hexKey = SIGNATURE_KEY.getBytes("UTF-8");
	    byte[] payloadBytes = payload.getBytes("UTF-8");

	    Mac sha256HMAC = Mac.getInstance(ALGORITHM);
	    SecretKeySpec secretKey = new SecretKeySpec(hexKey, ALGORITHM);
	    sha256HMAC.init(secretKey);

	    //warning: a Mac instance is not thread safe
	    byte[] hmacByte = sha256HMAC.doFinal(payloadBytes);
	    return new String(Base64.decode(hmacByte));
	}

	private JsonObject jsonParser(HttpResponse response) throws Exception {
		try {
			String jsonStr = jsonStr(response);
			JsonElement jsonElement =  new JsonParser().parse(jsonStr);
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

	private String jsonStr(HttpResponse response) throws Exception {
		try {
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output, result = "";

			while ((output = br.readLine()) != null) {
				result = output;
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
				result = output;
			}
			br.close();

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("httpStatus", statusCode);
			resultMap.put("responseJsonStr", result);

			return resultMap;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private String getBodyJsonString(Map<String, Object> data) {
		//---:
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("entity", data.get("entity").toString());
		jsonObject.addProperty("companyCode", data.get("companyCode").toString());
		jsonObject.addProperty("product", data.get("product").toString());
		jsonObject.addProperty("branch", data.get("branch").toString());
		jsonObject.addProperty("contractNumber", data.get("contractNumber").toString());
		jsonObject.addProperty("message", data.get("message").toString());
		jsonObject.addProperty("userName", data.get("userName").toString());
		jsonObject.addProperty("actionCode", data.get("actionCode").toString());
		jsonObject.addProperty("actionDatetime", data.get("actionDatetime").toString());
		jsonObject.addProperty("resultCode", data.get("resultCode").toString());
		jsonObject.addProperty("recallCode", data.get("recallCode").toString());
		jsonObject.addProperty("recallDate", data.get("recallDate").toString());
		jsonObject.addProperty("recallTime", data.get("recallTime").toString());
		jsonObject.addProperty("ppAmount", data.get("ppAmount").toString());
		jsonObject.addProperty("supCode", data.get("supCode").toString());
		jsonObject.addProperty("transactionDatetime", data.get("transactionDatetime").toString());
		return jsonObject.toString();
	}

}
