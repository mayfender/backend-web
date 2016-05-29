package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.DbFactory;
import com.may.ple.backend.criteria.PersistProductCriteriaReq;
import com.may.ple.backend.criteria.ProductSearchCriteriaReq;
import com.may.ple.backend.criteria.ProductSearchCriteriaResp;
import com.may.ple.backend.entity.Database;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.repository.ProductRepository;
import com.mongodb.MongoClient;

@Service
public class ProductService {
	private static final Logger LOG = Logger.getLogger(ProductService.class.getName());
	private ProductRepository productRepository;
	private MongoTemplate template;
	private DbFactory dbFactory;
	private MappingMongoConverter mappingMongoConverter;
	
	@Autowired	
	public ProductService(ProductRepository productRepository, MongoTemplate template, DbFactory dbFactory, MappingMongoConverter mappingMongoConverter) {
		this.productRepository = productRepository;
		this.template = template;
		this.dbFactory = dbFactory;
		this.mappingMongoConverter = mappingMongoConverter;
	}
	
	public ProductSearchCriteriaResp findProduct(ProductSearchCriteriaReq req) throws Exception {
		ProductSearchCriteriaResp resp = new ProductSearchCriteriaResp();
		
		try {
			Criteria criteria = Criteria.where("productName").regex(Pattern.compile(req.getProductName() == null ? "" : req.getProductName(), Pattern.CASE_INSENSITIVE));
			
			if(req.getEnabled() != null) {
				criteria = criteria.and("enabled").is(req.getEnabled());
			}
			
			long totalItems = template.count(new Query(criteria == null ? criteria = new Criteria() : criteria), Product.class);
			
			Query query = new Query(criteria == null ? criteria = new Criteria() : criteria)
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort("productName"));
			query.fields().exclude("updatedDateTime");
			
			List<Product> prods = template.find(query, Product.class);			
			
			resp.setTotalItems(totalItems);
			resp.setProducts(prods);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void saveProduct(PersistProductCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			Product product = new Product(req.getProductName(), req.getEnabled(), date, date, req.getDatabase());
			
			productRepository.save(product);
			
			LOG.debug("Call addDbConn");
			addDbConn(product);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateProduct(PersistProductCriteriaReq req) throws Exception {
		try {
			Product product = productRepository.findOne(req.getId());
			product.setProductName(req.getProductName());
			product.setUpdatedDateTime(new Date());
			
			if(!product.getDatabase().equals(req.getDatabase())) {
				LOG.debug("Database config had changed");
				
				product.setDatabase(req.getDatabase());
				
				LOG.debug("Call removeDbConn");
				removeDbConn(req.getId());
				
				LOG.debug("Call addDbConn");
				addDbConn(product);
			}
			
			if(req.getEnabled().intValue() != product.getEnabled().intValue()) {
				product.setEnabled(req.getEnabled());
				
				if(product.getEnabled().intValue() == 1) {
					LOG.debug("Call addDbConn");
					addDbConn(product);
				} else {
					LOG.debug("Call removeDbConn");
					removeDbConn(req.getId());
				}
			}
			
			productRepository.save(product);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteProduct(String id) throws Exception {
		try {
			productRepository.delete(id);
			removeDbConn(id);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private synchronized void addDbConn(Product product) throws Exception {
		try {
			LOG.debug("Add new Database connection");
			Database db = product.getDatabase();
			SimpleMongoDbFactory simFact = new SimpleMongoDbFactory(new MongoClient(db.getHost(), db.getPort()), db.getDbName());
			MongoTemplate newTemplate = new MongoTemplate(simFact, mappingMongoConverter);
			dbFactory.getTemplates().put(product.getId(), newTemplate);
			LOG.debug("All databsae : " + dbFactory.getTemplates().size());			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private synchronized void removeDbConn(String id) {
		LOG.debug("Remove Database connection");
		Map<String, MongoTemplate> templates = dbFactory.getTemplates();
		templates.get(id).getDb().getMongo().close();
		templates.remove(id);
		LOG.debug("All databsae : " + dbFactory.getTemplates().size());
	}
	
}
