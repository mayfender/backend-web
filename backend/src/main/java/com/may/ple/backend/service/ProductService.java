package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.may.ple.backend.constant.RolesConstant;
import com.may.ple.backend.criteria.ColumnFormatDetActiveUpdateCriteriaReq;
import com.may.ple.backend.criteria.ColumnFormatDetUpdatreCriteriaReq;
import com.may.ple.backend.criteria.GetColumnFormatsDetCriteriaResp;
import com.may.ple.backend.criteria.GroupDataUpdateCriteriaReq;
import com.may.ple.backend.criteria.PersistProductCriteriaReq;
import com.may.ple.backend.criteria.ProductNoticeUpdateCriteriaReq;
import com.may.ple.backend.criteria.ProductSearchCriteriaReq;
import com.may.ple.backend.criteria.ProductSearchCriteriaResp;
import com.may.ple.backend.criteria.UpdateProductSettingCriteriaReq;
import com.may.ple.backend.criteria.WorkingTimeCriteriaResp;
import com.may.ple.backend.criteria.WorkingTimeUpdateCriteriaReq;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Database;
import com.may.ple.backend.entity.GroupData;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.ColumnFormatGroup;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.repository.ProductRepository;
import com.may.ple.backend.repository.UserRepository;
import com.mongodb.MongoClient;

@Service
public class ProductService {
	private static final Logger LOG = Logger.getLogger(ProductService.class.getName());
	private MappingMongoConverter mappingMongoConverter;
	private ProductRepository productRepository;
	private UserRepository userRepository;
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public ProductService(ProductRepository productRepository, MongoTemplate template, DbFactory dbFactory, MappingMongoConverter mappingMongoConverter, UserRepository userRepository) {
		this.template = template;
		this.dbFactory = dbFactory;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.mappingMongoConverter = mappingMongoConverter;
	}
	
	public ProductSearchCriteriaResp findProduct(ProductSearchCriteriaReq req) throws Exception {
		ProductSearchCriteriaResp resp = new ProductSearchCriteriaResp();
		
		try {
			
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>)authentication.getAuthorities();
			RolesConstant rolesConstant = RolesConstant.valueOf(authorities.get(0).getAuthority());
			List<String> prodIds = null;
			
			if(rolesConstant == RolesConstant.ROLE_ADMIN) {
				LOG.debug("Find PRODUCTS underly admin");
				Users admin = userRepository.findByUsername(authentication.getName());
				prodIds = admin.getProducts();
			}
			
			Criteria criteria = Criteria.where("productName").regex(Pattern.compile(req.getProductName() == null ? "" : req.getProductName(), Pattern.CASE_INSENSITIVE));
			
			if(req.getEnabled() != null) {
				criteria = criteria.and("enabled").is(req.getEnabled());
			}
			if(prodIds != null) {
				criteria = criteria.and("_id").in(prodIds);
			}
			
			long totalItems = template.count(Query.query(criteria == null ? criteria = new Criteria() : criteria), Product.class);
			
			Query query = Query.query(criteria == null ? criteria = new Criteria() : criteria)
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort("productName"));
			query.fields().exclude("updatedDateTime").exclude("columnFormats");
			
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
			Product product = new Product(req.getProductName(), req.getEnabled(), date, date, null);
			
			productRepository.save(product);
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
	
	public void updateDatabaseConf(PersistProductCriteriaReq req) throws Exception {
		try {
			Product product = productRepository.findOne(req.getId());
			product.setUpdatedDateTime(new Date());
			product.setDatabase(req.getDatabase());
			
			LOG.debug("Call removeDbConn");
			removeDbConn(req.getId());
			
			LOG.debug("Call addDbConn");
			addDbConn(product);
			
			productRepository.save(product);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateColumnFormat(PersistProductCriteriaReq req) throws Exception {
		try {
			Product product = productRepository.findOne(req.getId());
			product.setUpdatedDateTime(new Date());
			boolean isPayment = req.getIsPayment() == null ? false : req.getIsPayment();
			
			if(isPayment) {				
				product.setColumnFormatsPayment(req.getColumnFormats());				
			} else {
				product.setColumnFormats(req.getColumnFormats());				
			}
			
			productRepository.save(product);
			
			if(req.getIsActive() != null) {
				LOG.debug("Update index");
				MongoTemplate productTemplate = dbFactory.getTemplates().get(req.getId());
				
				if(req.getColumnName().equals(SYS_OWNER.getName())) {
					req.setColumnName(SYS_OWNER_ID.getName());
				}
				
				if(req.getIsActive()) {
					productTemplate.indexOps(isPayment ? "paymentDetail" : NEW_TASK_DETAIL.getName()).ensureIndex(new Index().on(req.getColumnName(), Direction.ASC));										
				} else {
					productTemplate.indexOps(isPayment ? "paymentDetail" : NEW_TASK_DETAIL.getName()).dropIndex(req.getColumnName() + "_1");
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateColumnName(UpdateProductSettingCriteriaReq req) throws Exception {
		try {
			Product product = productRepository.findOne(req.getProductId());
			product.setUpdatedDateTime(new Date());
			ProductSetting setting = product.getProductSetting();
			boolean isPayment = req.getIsPayment() == null ? false : req.getIsPayment();
			
			if(setting == null) {
				LOG.debug("Create new ProductSetting");
				setting = new ProductSetting();
				product.setProductSetting(setting);
			}
			
			if(isPayment) {
				if(!StringUtils.isBlank(req.getContractNoColumnName())) {
					setting.setContractNoColumnNamePayment(req.getContractNoColumnName());				
				} else if(!StringUtils.isBlank(req.getIdCardNoColumnName())) {
					setting.setIdCardNoColumnNamePayment(req.getIdCardNoColumnName());				
				}
			} else {
				if(!StringUtils.isBlank(req.getContractNoColumnName())) {
					setting.setContractNoColumnName(req.getContractNoColumnName());				
				} else if(!StringUtils.isBlank(req.getIdCardNoColumnName())) {
					setting.setIdCardNoColumnName(req.getIdCardNoColumnName());				
				} else if(!StringUtils.isBlank(req.getBalanceColumnName())) {
					setting.setBalanceColumnName(req.getBalanceColumnName());
				} else if(!StringUtils.isBlank(req.getExpirationDateColumnName())) {
					setting.setExpirationDateColumnName(req.getExpirationDateColumnName());
				}				
			}
			
			productRepository.save(product);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<ColumnFormat> getColumnFormat(String id) throws Exception {
		try {
			Product product = productRepository.findOne(id);
			return product.getColumnFormats();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Product getProduct(String id) throws Exception {
		try {
			Product product = productRepository.findOne(id);
			return product;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public GetColumnFormatsDetCriteriaResp getColumnFormatDet(String id) throws Exception {
		try {
			GetColumnFormatsDetCriteriaResp resp = new GetColumnFormatsDetCriteriaResp();
			Product product = productRepository.findOne(id);
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			List<GroupData> groupDatas = product.getGroupDatas();
			
			if(columnFormats == null) return null;
			
			Map<Integer, List<ColumnFormat>> map = new HashMap<>();
			List<ColumnFormat> colFormLst;
			
			for (ColumnFormat colForm : columnFormats) {
				if(map.containsKey(colForm.getDetGroupId())) {					
					colFormLst = map.get(colForm.getDetGroupId());
					colFormLst.add(colForm);
				} else {
					colFormLst = new ArrayList<>();
					colFormLst.add(colForm);
					map.put(colForm.getDetGroupId(), colFormLst);
				}
			}
			
			resp.setGroupDatas(groupDatas);
			resp.setColFormMap(map);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateGroupDatas(GroupDataUpdateCriteriaReq req) throws Exception {
		try {
			Product product = productRepository.findOne(req.getProductId());
			product.setUpdatedDateTime(new Date());
			product.setGroupDatas(req.getGroupDatas());
			
			productRepository.save(product);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateColumnFormatDet(ColumnFormatDetUpdatreCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start");
			Product product = productRepository.findOne(req.getProductId());
			List<ColumnFormat> colForm = product.getColumnFormats();
			
			List<ColumnFormatGroup> colFormGroups = req.getColFormGroups();
			List<ColumnFormat> columnFormats;
			Integer groupId;
			
			for (ColumnFormatGroup columnFormatGroup : colFormGroups) {
				groupId = columnFormatGroup.getId();
				columnFormats = columnFormatGroup.getColumnFormats();
				
				for (ColumnFormat col : colForm) {		
					for (ColumnFormat columnFormat : columnFormats) {
						if(col.getColumnName().equals(columnFormat.getColumnName())) {
							col.setDetGroupId(groupId);
							col.setDetOrder(columnFormat.getDetOrder());
							break;
						}
					}
				}
			}
			
			product.setUpdatedDateTime(new Date());
			productRepository.save(product);
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateColumnFormatDetActive(ColumnFormatDetActiveUpdateCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start");
			Product product = productRepository.findOne(req.getProductId());
			List<ColumnFormat> colForm = product.getColumnFormats();
			ColumnFormat columnFormat = req.getColumnFormat();
			
			for (ColumnFormat col : colForm) {
				if(col.getColumnName().equals(columnFormat.getColumnName())) {
					col.setDetIsActive(columnFormat.getDetIsActive());
					break;
				}				
			}
			
			product.setUpdatedDateTime(new Date());
			productRepository.save(product);
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateNotice(ProductNoticeUpdateCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start");
			Product product = productRepository.findOne(req.getId());
			List<ColumnFormat> colForm = product.getColumnFormats();
			
			for (ColumnFormat col : colForm) {		
				if(col.getColumnName().equals(req.getColumnName())) {
					if(col.getIsNotice() == null || !col.getIsNotice()) {
						col.setIsNotice(true);						
					} else {
						col.setIsNotice(false);
					}
					break;
				}				
			}
			
			product.setUpdatedDateTime(new Date());
			productRepository.save(product);
			LOG.debug("End");
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
	
	public void updateWorkingTime(WorkingTimeUpdateCriteriaReq req) throws Exception {
		try {
			Product product = productRepository.findOne(req.getProductId());
			ProductSetting setting = product.getProductSetting();
			
			if(setting == null) {
				setting = new ProductSetting();
			}
			
			setting.setStartTimeH(req.getStartTimeH());
			setting.setStartTimeM(req.getStartTimeM());
			setting.setEndTimeH(req.getEndTimeH());
			setting.setEndTimeM(req.getEndTimeM());
			
			productRepository.save(product);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public WorkingTimeCriteriaResp getWorkingTime(String productId) throws Exception {
		try {
			WorkingTimeCriteriaResp resp = new WorkingTimeCriteriaResp();
			
			Product product = productRepository.findOne(productId);
			ProductSetting setting = product.getProductSetting();
			
			if(setting == null) {
				return resp;
			}
			
			resp.setStartTimeH(setting.getStartTimeH());
			resp.setStartTimeM(setting.getStartTimeM());
			resp.setEndTimeH(setting.getEndTimeH());
			resp.setEndTimeM(setting.getEndTimeM());
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private synchronized void addDbConn(Product product) throws Exception {
		try {
			LOG.debug("Add new Database connection");
			Database db = product.getDatabase();
			
			if(db == null || StringUtils.isBlank(db.getHost())) return;
			
			UserCredentials credential = new UserCredentials(db.getUserName(), db.getPassword());
			SimpleMongoDbFactory fatory = new SimpleMongoDbFactory(new MongoClient(db.getHost(), db.getPort()), db.getDbName(), credential);
			MongoTemplate newTemplate = new MongoTemplate(fatory, mappingMongoConverter);
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
		
		if(templates.containsKey(id)) {
			try {
				templates.get(id).getDb().getMongo().close();
			} catch (Exception e) {
				LOG.error(e.toString());
			}
			
			templates.remove(id);
			LOG.debug("All databsae : " + dbFactory.getTemplates().size());			
			
			//--: Remove others relationship
			List<Users> users = template.find(Query.query(Criteria.where("setting.currentProduct").is(id)), Users.class);
			for (Users u : users) {
				u.getSetting().setCurrentProduct(null);
				template.save(u);
			}
		} else {
			LOG.debug("Nothing to remove");
		}
	}
	
}
