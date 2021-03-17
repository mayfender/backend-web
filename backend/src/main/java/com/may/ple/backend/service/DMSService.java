package com.may.ple.backend.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.DMSCriteriaReq;
import com.may.ple.backend.criteria.DMSCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@Service
public class DMSService {
	private static final Logger LOG = Logger.getLogger(DMSService.class.getName());
	private MongoTemplate template;

	@Autowired
	public DMSService(MongoTemplate template) {
		this.template = template;
	}

	public DMSCriteriaResp getCustomers(DMSCriteriaReq req) {
		try {
			DMSCriteriaResp resp = new DMSCriteriaResp();
			Criteria criteria = new Criteria();

			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}
			if(StringUtils.isNotBlank(req.getName())) {
				criteria.and("name").regex(Pattern.compile(req.getName(), Pattern.CASE_INSENSITIVE));
			}
			if(req.getPackageId() != null) {
				criteria.and("package").is(req.getPackageId());
			}

			long totalItems = template.count(Query.query(criteria), "dms_customer");
			resp.setTotalItems(totalItems);

			if(totalItems > 0) {
				Query query = Query.query(criteria).with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
				resp.setCustomers(template.find(query, Map.class, "dms_customer"));
			}

			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Map editCustomerById(String id) {
		try {
			Criteria criteria = Criteria.where("_id").is(new ObjectId(id));
			return template.findOne(Query.query(criteria), Map.class, "dms_customer");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public String updateCustomer(DMSCriteriaReq req) {
		try {
			if(StringUtils.isBlank(req.getId())) {
				BasicDBObject data = new BasicDBObject(req.getField(), req.getValue());
				data.append("package", 1);
				data.append("enabled", true);
				data.append("createdDateTime", Calendar.getInstance().getTime());
				template.insert(data, "dms_customer");
				return data.get("_id").toString();
			} else {
				Criteria criteria = Criteria.where("_id").is(new ObjectId(req.getId()));
				Update update = new Update();
				update.set(req.getField(), req.getValue());

				if(req.getField().equals("enabled") && !Boolean.valueOf(req.getValue().toString())) {
					update.set("disabledDateTime", Calendar.getInstance().getTime());
				}

				template.updateFirst(Query.query(criteria), update, "dms_customer");
				return null;
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void removeCustomer(String id) {
		try {
			template.remove(Query.query(Criteria.where("_id").is(new ObjectId(id))), "dms_customer");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void updateProduct(DMSCriteriaReq req) {
		try {
			Update update;
			Query query;
			if(StringUtils.isBlank(req.getProductId())) {
				BasicDBObject product = new BasicDBObject("id", ObjectId.get());
				product.append("name", req.getName());
				product.append("package", req.getPackageId());
				product.append("enabled", req.getEnabled());
				product.append("createdDateTime", Calendar.getInstance().getTime());

				update = new Update();
				update.addToSet("products", product);

				query = Query.query(Criteria.where("_id").is(new ObjectId(req.getId())));
				template.updateFirst(query, update, "dms_customer");
			} else {
				update = new Update();
				update.set("products.$.name", req.getName());
				update.set("products.$.package", req.getPackageId());
				update.set("products.$.enabled", req.getEnabled());

				if(!req.getEnabled()) {
					update.set("products.$.disabledDateTime", Calendar.getInstance().getTime());
				}

				query = Query.query(Criteria.where("_id").is(new ObjectId(req.getId())).and("products.id").is(new ObjectId(req.getProductId())));
				template.updateFirst(query, update, "dms_customer");
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void removeProduct(String productId, String id) {
		try {
			Query query = Query.query(Criteria.where("_id").is(new ObjectId(id)));
			Update update = new Update();
			update.pull("products", new BasicDBObject("id", new ObjectId(productId)));
			template.updateFirst(query, update, "dms_customer");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<Map> getProducts(DMSCriteriaReq req) {
		try {
			BasicDBList andCond = new BasicDBList();
			BasicDBList dbList = new BasicDBList();
			dbList.add("$$product.enabled");
			dbList.add(true);
			andCond.add(new BasicDBObject("$eq", dbList));

			if(req.getPackageId() != null) {
				dbList = new BasicDBList();
				dbList.add("$$product.package");
				dbList.add(req.getPackageId());
				andCond.add(new BasicDBObject("$eq", dbList));
			}


			Criteria criteria = Criteria.where("enabled").is(true);
			if(StringUtils.isNotBlank(req.getName())) {
				criteria.and("name").regex(Pattern.compile(req.getName(), Pattern.CASE_INSENSITIVE));
			}

			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(criteria),
					new CustomAggregationOperation(
					        new BasicDBObject("$project",
						            new BasicDBObject("products",
						            	new BasicDBObject("$filter",
						            		new BasicDBObject("input", "$products")
						            		.append("as", "product")
						            		.append("cond", new BasicDBObject("$and", andCond))
						            	)
						            ).append("name", 1)
						        ))
			);

			return template.aggregate(agg, "dms_customer", Map.class).getMappedResults();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void createInvoice(DMSCriteriaReq req) {
		try {
			List<Map> invoiceData = req.getInvoiceData();

			Map<String, Object> invoice = new HashMap<>();
			invoice.put("createdDateTime", Calendar.getInstance().getTime());
			invoice.put("invoiceData", invoiceData);

			template.save(invoice, "dms_invoice");

//			Map<String, Object> invoice = new HashMap<>();
//			List<Map> items;
			/*for (Map iv : invoiceData) {
				invoice.put("_id", new ObjectId(iv.get("id").toString()));
				invoice.put("name", iv.get("name").toString());
				items = (List)iv.get("items");

				for (Map item : items) {
					item.get("name");
					item.get("packageId");
					item.get("perMn");
					item.get("note");
					item.get("price");
				}
			}*/


			/*Query query = Query.query(Criteria.where("_id").is(new ObjectId(id)));
			Update update = new Update();
			update.pull("products", new BasicDBObject("id", new ObjectId(productId)));
			template.updateFirst(query, update, "dms_customer");*/
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
