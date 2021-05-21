package com.may.ple.backend.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.model.DbFactory;

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
		if (isJson(ctx)) {
			try {
                String json = IOUtils.toString(ctx.getEntityStream(), "utf-8");
                JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();

                if(jsonObj.get("productId") != null) {
                	String productId = jsonObj.get("productId").getAsString();
                	Query query = Query.query(Criteria.where("id").is(productId));
                	query.fields().include("productName");
                	Product product = templateCenter.findOne(query, Product.class);
                	MDC.put("PTID", product.getProductName());

                	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        			MDC.put("UN", authentication.getName());
                }

                ctx.setEntityStream(IOUtils.toInputStream(json, "utf-8"));
            } catch (IOException ex) {
            	LOG.error(ex.toString(), ex);
            }
		}
	}

	boolean isJson(ContainerRequestContext request) {
        return request.getMediaType().toString().contains("application/json");
    }

}
