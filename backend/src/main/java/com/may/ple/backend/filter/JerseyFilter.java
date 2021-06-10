package com.may.ple.backend.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.may.ple.backend.constant.RolesConstant;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Provider
public class JerseyFilter implements ContainerRequestFilter {
	private static final Logger LOG = Logger.getLogger(JerseyFilter.class.getName());
	@Autowired
	private MongoTemplate templateCenter;
	@Autowired
	private DbFactory dbFactory;
	@Context
	private HttpServletRequest request;

	public JerseyFilter() {
		LOG.info("Init JerseyFilter");
	}

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		try {
			LOG.info("Start");
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String userName = auth.getName();
			List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>)auth.getAuthorities();
		    RolesConstant rolesConstant = RolesConstant.valueOf(authorities.get(0).getAuthority());
		    int userGroup = rolesConstant.getId();

			MDC.put("UN", userName);

			Query queryUser = Query.query(Criteria.where("username").is(userName));
	    	queryUser.fields().include("_id");
	    	Users user = templateCenter.findOne(queryUser, Users.class);
	    	int mediaType = getMediaType(ctx);

	    	Map<String, Object> payLoad = new HashMap<>();
			payLoad.put("userId", new ObjectId(user.getId()));
			payLoad.put("userGroup", userGroup);

			if (mediaType == 1) {
				//---: Json
                String json = IOUtils.toString(ctx.getEntityStream(), "utf-8");
                JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
                boolean isLog = jsonObj.get("isLog") != null ? jsonObj.get("isLog").getAsBoolean() : false;
                JsonElement productIdEl = jsonObj.get("productId");
                String productId = "";

                if(productIdEl != null) {
                	productId = productIdEl.getAsString();
                } else {
                	productIdEl = jsonObj.get("productIds");
                	if(productIdEl != null) {
                		productId = productIdEl.getAsJsonArray().get(0).getAsString();
                	}
                }

                if(isLog && StringUtils.isNotBlank(productId)) {
                	String actionName = getActionName(ctx.getUriInfo());
                	if(actionName.equals("taskDetail:find")) {
        				String fromPage = jsonObj.get("fromPage") != null ? jsonObj.get("fromPage").getAsString() : null;
        				if(fromPage != null && fromPage.equals("assign")) {
        					String actionType = jsonObj.get("actionType") != null ? jsonObj.get("actionType").getAsString() : null;
        					if(actionType != null) {
        						actionName += ":" + fromPage + ":" + actionType;
        					} else {
        						actionName += ":" + fromPage;
        					}
        				}
        			}

        			//---: Persist Log Data.
        			MongoTemplate template = dbFactory.getTemplates().get(productId);

        			payLoad.put("requestPayload", jsonObj.toString());
        			payLoad.put("actionName", actionName);
        			payLoad.put("remoteIP", getRemoteIP());

        			saveLog(template, ctx, payLoad, productId);
                } else {
                	LOG.info("productId is empty or isLog is false.");
                }

                ctx.setEntityStream(IOUtils.toInputStream(json, "utf-8"));
			} else if (mediaType == 2) {
				//---: Upload file
				if (ctx instanceof ContainerRequest) {
		            ContainerRequest request = (ContainerRequest) ctx;
		            InputStream in = request.getEntityStream();
		            if (in.getClass() != ByteArrayInputStream.class) {
		            	//---: Buffer input
		            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		            	ReaderWriter.writeTo(in, baos);
		            	in = new ByteArrayInputStream(baos.toByteArray());
		            	request.setEntityStream(in);
		            }

		            //---: Read entity
		            FormDataMultiPart form = request.readEntity(FormDataMultiPart.class);
		            String productId = form.getFields("productId") != null ? form.getFields("productId").get(0).getValue() : null;
		            boolean isLog = form.getFields("isLog") != null ? Boolean.valueOf(form.getFields("isLog").get(0).getValue()) : false;

		            if(isLog && productId != null) {
		            	String actionName = getActionName(ctx.getUriInfo());
		            	HashMap<Object, Object> requestPayload = new HashMap<>();
		            	requestPayload.put("productId", productId);

		            	payLoad.put("requestPayload", new Gson().toJson(requestPayload));
		            	payLoad.put("actionName", actionName);
		            	payLoad.put("remoteIP", getRemoteIP());

		            	MongoTemplate template = dbFactory.getTemplates().get(productId);
		            	saveLog(template, ctx, payLoad, productId);
		            }

		            //---: Reset buffer
		            ByteArrayInputStream bais = (ByteArrayInputStream)in;
		            bais.reset();
		        }
			} else {
				MultivaluedMap<String, String> queryParameters = ctx.getUriInfo().getQueryParameters();
				boolean isLog = queryParameters.get("isLog") != null ? Boolean.valueOf(queryParameters.get("isLog").get(0)) : false;

				if(isLog && queryParameters.get("productId") != null) {
					String actionName = getActionName(ctx.getUriInfo());
					String productId = queryParameters.get("productId").get(0);
					HashMap<Object, Object> requestPayload = new HashMap<>();

					for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
						System.out.println(entry.getKey() + ":" + entry.getValue().get(0));
						requestPayload.put(entry.getKey(), entry.getValue().get(0));
					}

					MongoTemplate template = dbFactory.getTemplates().get(productId);
	            	payLoad.put("requestPayload", new Gson().toJson(requestPayload));
	            	payLoad.put("actionName", actionName);
	            	payLoad.put("remoteIP", getRemoteIP());

					saveLog(template, ctx, payLoad, productId);
				}

				LOG.info("Not Json");
			}
		} catch (IOException ex) {
        	LOG.error(ex.toString(), ex);
        }
		LOG.info("End");
	}

	private void saveLog(MongoTemplate template, ContainerRequestContext ctx, Map<String, Object> payLoad, String productId) {
		String userLogDbName = "userLog";
		Calendar cal = Calendar.getInstance();
		BasicBSONObject userLog = new BasicBSONObject();
		userLog.put("createdDateTime", cal.getTime());
		payLoad.put("httpMethod", ctx.getMethod());

		for (Map.Entry<String, Object> entry : payLoad.entrySet()) {
	        userLog.put(entry.getKey(), entry.getValue());
	    }

		ObjectId objectId = ObjectId.get();
		userLog.put("_id", objectId);
		template.insert(userLog, userLogDbName);
		ctx.getHeaders().add("userLogId", objectId.toString());

		//---: Create DB Index.
		DBCollection collection = template.getCollection(userLogDbName);
		collection.createIndex(new BasicDBObject("createdDateTime", 1));
		collection.createIndex(new BasicDBObject("userId", 1));
		collection.createIndex(new BasicDBObject("userGroup", 1));
		collection.createIndex(new BasicDBObject("actionName", 1));

		Query queryProd = Query.query(Criteria.where("id").is(productId));
    	queryProd.fields().include("productName");
    	Product product = templateCenter.findOne(queryProd, Product.class);
		MDC.put("PTID", product.getProductName());
	}

	private int getMediaType(ContainerRequestContext request) {
		if(request != null) {
			if(request.getMediaType() != null) {
				String mediaType = request.getMediaType().toString();
				return mediaType.contains("application/json") ? 1 :
					  (mediaType.contains("multipart/form-data") ? 2 : -1);
			} else {
				return -1;
			}
		} else {
			return -1;
		}
    }

	private String getActionName(UriInfo uriInfo) {
		return uriInfo.getPathSegments().get(0).getPath() + ":" + uriInfo.getPathSegments().get(1).getPath();
	}

	private String getRemoteIP() {
		String remoteAddr = null;
		if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (StringUtils.isBlank(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
		return remoteAddr;
	}

}
