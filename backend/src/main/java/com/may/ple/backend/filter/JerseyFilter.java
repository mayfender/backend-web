package com.may.ple.backend.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
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
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

	public JerseyFilter() {
		LOG.info("Init JerseyFilter");
	}

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		try {
			LOG.info("Start");
			String userName = SecurityContextHolder.getContext().getAuthentication().getName();
			MDC.put("UN", userName);

			Query queryUser = Query.query(Criteria.where("username").is(userName));
	    	queryUser.fields().include("_id");
	    	Users user = templateCenter.findOne(queryUser, Users.class);
	    	int mediaType = getMediaType(ctx);

			if (mediaType == 1) {
				//---: Json
                String json = IOUtils.toString(ctx.getEntityStream(), "utf-8");
                JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
                JsonElement productIdEl;
                boolean isLog = jsonObj.get("isLog") != null ? jsonObj.get("isLog").getAsBoolean() : false;

                if(isLog && (productIdEl = jsonObj.get("productId")) != null) {
                	String productId = productIdEl.getAsString();

        			//---: Persist Log Data.
        			MongoTemplate template = dbFactory.getTemplates().get(productId);

        			Map<String, Object> payLoad = new HashMap<>();
        			payLoad.put("userId", new ObjectId(user.getId()));
        			payLoad.put("requestPayload", jsonObj.toString());
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
		            	HashMap<Object, Object> requestPayload = new HashMap<>();
		            	requestPayload.put("productId", productId);

		            	Map<String, Object> payLoad = new HashMap<>();
		            	payLoad.put("userId", new ObjectId(user.getId()));
		            	payLoad.put("requestPayload", new Gson().toJson(requestPayload));

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
					String productId = queryParameters.get("productId").get(0);
					HashMap<Object, Object> requestPayload = new HashMap<>();

					for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
						System.out.println(entry.getKey() + ":" + entry.getValue().get(0));
						requestPayload.put(entry.getKey(), entry.getValue().get(0));
					}

					MongoTemplate template = dbFactory.getTemplates().get(productId);
					Map<String, Object> payLoad = new HashMap<>();
	            	payLoad.put("userId", new ObjectId(user.getId()));
	            	payLoad.put("requestPayload", new Gson().toJson(requestPayload));

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
		UriInfo uriInfo = ctx.getUriInfo();
		Calendar cal = Calendar.getInstance();
		BasicBSONObject userLog = new BasicBSONObject();
		userLog.put("createdDateTime", cal.getTime());
		payLoad.put("actionName", uriInfo.getPathSegments().get(0).getPath() + ":" + uriInfo.getPathSegments().get(1).getPath());
		payLoad.put("httpMethod", ctx.getMethod());

		for (Map.Entry<String, Object> entry : payLoad.entrySet()) {
	        userLog.put(entry.getKey(), entry.getValue());
	    }

		template.insert(userLog, userLogDbName);
		//---: Create DB Index.
		DBCollection collection = template.getCollection(userLogDbName);
		collection.createIndex(new BasicDBObject("createdDateTime", 1));
		collection.createIndex(new BasicDBObject("userId", 1));
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

}
