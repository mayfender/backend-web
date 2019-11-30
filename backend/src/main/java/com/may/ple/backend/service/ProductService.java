package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.index.IndexInfo;
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
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

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
			
			ProductSetting productSetting = new ProductSetting();
			productSetting.setIsTraceExportExcel(req.getIsTraceExportExcel());
			productSetting.setIsTraceExportTxt(req.getIsTraceExportTxt());
			productSetting.setTraceDateRoundDay(req.getTraceDateRoundDay());
			productSetting.setNoticeFramework(req.getNoticeFramework());
			productSetting.setPocModule(req.getPocModule());
			productSetting.setAutoUpdateBalance(req.getAutoUpdateBalance());
			productSetting.setPaymentRules(req.getPaymentRules());
			productSetting.setCreatedByLog(req.getCreatedByLog());
			productSetting.setUserEditable(req.getUserEditable());
			productSetting.setDiscountColumnName(req.getDiscountColumnName());
			productSetting.setTextLength(req.getTextLength());
			productSetting.setDiscountFields(req.getDiscountFields());
			productSetting.setOpenOfficeHost(req.getOpenOfficeHost());
			productSetting.setOpenOfficePort(req.getOpenOfficePort());
			productSetting.setShowUploadDoc(req.getShowUploadDoc());
			productSetting.setSeizure(req.getSeizure());
			productSetting.setPrivateChatDisabled(req.getPrivateChatDisabled());
			productSetting.setUpdateEmptyReminderDate(req.getUpdateEmptyReminderDate());
			
			productSetting.setUserKYSLaw(req.getUserKYSLaw());
			productSetting.setPassKYSLaw(req.getPassKYSLaw());
			productSetting.setUserKYS(req.getUserKYS());
			productSetting.setPassKYS(req.getPassKYS());
			productSetting.setUserKRO(req.getUserKRO());
			productSetting.setPassKRO(req.getPassKRO());
			
			productSetting.setDsf(req.getDsf());
			productSetting.setReceipt(req.getReceipt());
			
			product.setProductSetting(productSetting);
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
			
			ProductSetting productSetting = product.getProductSetting();
			productSetting.setIsTraceExportExcel(req.getIsTraceExportExcel());
			productSetting.setIsTraceExportTxt(req.getIsTraceExportTxt());
			productSetting.setTraceDateRoundDay(req.getTraceDateRoundDay());
			productSetting.setNoticeFramework(req.getNoticeFramework());
			productSetting.setPocModule(req.getPocModule());
			productSetting.setAutoUpdateBalance(req.getAutoUpdateBalance());
			productSetting.setPaymentRules(req.getPaymentRules());
			productSetting.setCreatedByLog(req.getCreatedByLog());
			productSetting.setUserEditable(req.getUserEditable());
			productSetting.setDiscountColumnName(req.getDiscountColumnName());
			productSetting.setTextLength(req.getTextLength());
			productSetting.setDiscountFields(req.getDiscountFields());
			productSetting.setOpenOfficeHost(req.getOpenOfficeHost());
			productSetting.setOpenOfficePort(req.getOpenOfficePort());
			productSetting.setShowUploadDoc(req.getShowUploadDoc());
			productSetting.setSeizure(req.getSeizure());
			productSetting.setPrivateChatDisabled(req.getPrivateChatDisabled());
			productSetting.setUpdateEmptyReminderDate(req.getUpdateEmptyReminderDate());
			
			productSetting.setSmsMessages(req.getSmsMessages());
			productSetting.setIsSmsEnable(req.getIsSmsEnable());
			productSetting.setSmsUsername(req.getSmsUsername());
			productSetting.setSmsPassword(req.getSmsPassword());
			productSetting.setSmsSenderName(req.getSmsSenderName());
			
			productSetting.setUserKYSLaw(req.getUserKYSLaw());
			productSetting.setPassKYSLaw(req.getPassKYSLaw());
			productSetting.setUserKYS(req.getUserKYS());
			productSetting.setPassKYS(req.getPassKYS());
			productSetting.setUserKRO(req.getUserKRO());
			productSetting.setPassKRO(req.getPassKRO());
			
			productSetting.setDsf(req.getDsf());
			productSetting.setReceipt(req.getReceipt());
			
			productRepository.save(product);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updatePayType(PersistProductCriteriaReq req) throws Exception {
		try {
			Product product = productRepository.findOne(req.getId());			
			ProductSetting productSetting = product.getProductSetting();
			productSetting.setPayTypes(req.getPayTypes());
			productRepository.save(product);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public ProductSetting getProductSetting(String productId) throws Exception {
		try {
			Product product = getProduct(productId);
			return product.getProductSetting();
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
					DBCollection collection = productTemplate.getCollection(isPayment ? NEW_PAYMENT_DETAIL.getName() : NEW_TASK_DETAIL.getName());
					collection.createIndex(new BasicDBObject(req.getColumnName(), 1));
					if(isPayment) {
						collection.createIndex(new BasicDBObject(SYS_CREATED_DATE_TIME.getName(), 1));
					}
				} else {
					String indName = req.getColumnName() + "_1";
					
					List<IndexInfo> indexInfo = productTemplate.indexOps(isPayment ? NEW_PAYMENT_DETAIL.getName() : NEW_TASK_DETAIL.getName()).getIndexInfo();
					for (IndexInfo indInfo : indexInfo) {
						if(!indName.equals(indInfo.getName())) continue;
						
						productTemplate.indexOps(isPayment ? NEW_PAYMENT_DETAIL.getName() : NEW_TASK_DETAIL.getName()).dropIndex(indName);
					}
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
			
			if(isPayment) {
				if(!StringUtils.isBlank(req.getContractNoColumnName())) {
					setting.setContractNoColumnNamePayment(req.getContractNoColumnName());				
				} else if(!StringUtils.isBlank(req.getIdCardNoColumnName())) {
					setting.setIdCardNoColumnNamePayment(req.getIdCardNoColumnName());				
				} else if(!StringUtils.isBlank(req.getSortingColumnName())) {
					setting.setSortingColumnNamePayment(req.getSortingColumnName());				
				} else if(!StringUtils.isBlank(req.getPaidDateColumnName())) {
					setting.setPaidDateColumnNamePayment(req.getPaidDateColumnName());									
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
				} else if(!StringUtils.isBlank(req.getBirthDateColumnName())) {
					setting.setBirthDateColumnName(req.getBirthDateColumnName());					
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
			
			setting.setNormalStartTimeH(req.getNormalStartTimeH());
			setting.setNormalStartTimeM(req.getNormalStartTimeM());
			setting.setNormalEndTimeH(req.getNormalEndTimeH());
			setting.setNormalEndTimeM(req.getNormalEndTimeM());
			
			setting.setSatStartTimeH(req.getSatStartTimeH());
			setting.setSatStartTimeM(req.getSatStartTimeM());
			setting.setSatEndTimeH(req.getSatEndTimeH());
			setting.setSatEndTimeM(req.getSatEndTimeM());
			
			setting.setSunStartTimeH(req.getSunStartTimeH());
			setting.setSunStartTimeM(req.getSunStartTimeM());
			setting.setSunEndTimeH(req.getSunEndTimeH());
			setting.setSunEndTimeM(req.getSunEndTimeM());
			
			setting.setNormalWorkingDayEnable(req.getNormalWorkingDayEnable());
			setting.setSatWorkingDayEnable(req.getSatWorkingDayEnable());
			setting.setSunWorkingDayEnable(req.getSunWorkingDayEnable());
			
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
			
			resp.setNormalStartTimeH(setting.getNormalStartTimeH());
			resp.setNormalStartTimeM(setting.getNormalStartTimeM());
			resp.setNormalEndTimeH(setting.getNormalEndTimeH());
			resp.setNormalEndTimeM(setting.getNormalEndTimeM());
			
			resp.setSatStartTimeH(setting.getSatStartTimeH());
			resp.setSatStartTimeM(setting.getSatStartTimeM());
			resp.setSatEndTimeH(setting.getSatEndTimeH());
			resp.setSatEndTimeM(setting.getSatEndTimeM());
			
			resp.setSunStartTimeH(setting.getSunStartTimeH());
			resp.setSunStartTimeM(setting.getSunStartTimeM());
			resp.setSunEndTimeH(setting.getSunEndTimeH());
			resp.setSunEndTimeM(setting.getSunEndTimeM());
			
			resp.setNormalWorkingDayEnable(setting.getNormalWorkingDayEnable());
			resp.setSatWorkingDayEnable(setting.getSatWorkingDayEnable());
			resp.setSunWorkingDayEnable(setting.getSunWorkingDayEnable());
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateProductSetting(PersistProductCriteriaReq req) throws Exception {
		try {
			Product product = productRepository.findOne(req.getId());
			ProductSetting setting = product.getProductSetting();
			
			if(req.getUpdateType().intValue() == 1) {
				setting.setIsDisableNoticePrint(req.getIsDisableNoticePrint());
			} else if(req.getUpdateType().intValue() == 2) {
				setting.setIsHideComment(req.getIsHideComment());
			} else if(req.getUpdateType().intValue() == 3) {
				setting.setIsHideDashboard(req.getIsHideDashboard());
			} else if(req.getUpdateType().intValue() == 4) {
				setting.setIsHideAlert(req.getIsHideAlert());
			} else if(req.getUpdateType().intValue() == 5) {
				setting.setIsDisableBtnShow(req.getIsDisableBtnShow());
			}
			
			productRepository.save(product);
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
			
			MongoCredential credential = MongoCredential.createCredential(db.getUserName(), db.getDbName(), db.getPassword().toCharArray());
			ServerAddress serverAddress = new ServerAddress(db.getHost(), db.getPort());
			MongoClient mongoClient = new MongoClient(serverAddress, Arrays.asList(credential)); 
			SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongoClient, db.getDbName());
			MongoTemplate newTemplate = new MongoTemplate(factory, mappingMongoConverter);
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
