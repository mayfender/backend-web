package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.may.ple.backend.constant.RolesConstant;
import com.may.ple.backend.criteria.PersistUserCriteriaReq;
import com.may.ple.backend.criteria.ProfileUpdateCriteriaReq;
import com.may.ple.backend.criteria.ProfileUpdateCriteriaResp;
import com.may.ple.backend.criteria.ReOrderCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaResp;
import com.may.ple.backend.criteria.UserSettingCriteriaReq;
import com.may.ple.backend.entity.ImgData;
import com.may.ple.backend.entity.UserSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.repository.UserRepository;
import com.may.ple.backend.utils.ImageUtil;

@Service
public class UserService {
	private static final Logger LOG = Logger.getLogger(UserService.class.getName());
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private ServletContext servletContext;
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MongoTemplate template, ServletContext servletContext, DbFactory dbFactory) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.servletContext = servletContext;
		this.dbFactory = dbFactory;
		this.template = template;
	}
	
	public UserSearchCriteriaResp findAllUser(UserSearchCriteriaReq req) throws Exception {
		UserSearchCriteriaResp resp = new UserSearchCriteriaResp();
		
		try {
			
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>)authentication.getAuthorities();
			RolesConstant rolesConstant = RolesConstant.valueOf(authorities.get(0).getAuthority());
			boolean isManagerRole = false;
			boolean isAdminRole = false;
			
			if(rolesConstant == RolesConstant.ROLE_ADMIN) {
				LOG.debug("Role is Admin");
				isAdminRole = true;
			}
			if(rolesConstant == RolesConstant.ROLE_MANAGER) {
				LOG.debug("Role is Manager");
				isManagerRole = true;
			}
			
			Criteria criteria = Criteria.where("showname").regex(Pattern.compile(req.getUserNameShow() == null ? "" : req.getUserNameShow(), Pattern.CASE_INSENSITIVE))
					            .and("username").regex(Pattern.compile(req.getUserName() == null ? "" : req.getUserName(), Pattern.CASE_INSENSITIVE));
			
			if(!StringUtils.isBlank(req.getRole())) {
				criteria.and("authorities.role").is(req.getRole());				
			}
			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}
			if(isAdminRole) {
				List<SimpleGrantedAuthority> excludeAuthorities = new ArrayList<>();
				excludeAuthorities.add(new SimpleGrantedAuthority(RolesConstant.ROLE_ADMIN.toString()));
				criteria.and("authorities").ne(excludeAuthorities);
			}
			if(isManagerRole) {
				List<SimpleGrantedAuthority> includeAuthorities = new ArrayList<>();
				includeAuthorities.add(new SimpleGrantedAuthority(RolesConstant.ROLE_ADMIN.toString()));
				includeAuthorities.add(new SimpleGrantedAuthority(RolesConstant.ROLE_MANAGER.toString()));
				criteria.and("authorities").in(includeAuthorities);
			}
			if(req.getProduct() != null) {
				criteria.and("products").in(req.getProduct());
			} else if(req.getCurrentProduct() != null) {
				criteria.and("products").in(req.getCurrentProduct());
			}
			
			long totalItems = template.count(Query.query(criteria), Users.class);
			
			Query query = Query.query(criteria)
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort("authorities.role", "username", "showname"));
			query.fields().include("showname").include("username").include("authorities").include("enabled").include("createdDateTime");
			
			List<Users> users = template.find(query, Users.class);			
			
			resp.setTotalItems(totalItems);
			resp.setUsers(users);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void saveUser(PersistUserCriteriaReq req) throws Exception {
		try {
			Users u = userRepository.findByShowname(req.getShowname());
			
			if(u != null) {
				throw new CustomerException(2001, "This username_show is existing");
			}
			
			u = userRepository.findByUsername(req.getUsername());
			
			if(u != null) {
				throw new CustomerException(2000, "This username is existing");
			}
			
			String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));
			
			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority(req.getAuthority()));
			
			Date currentDate = new Date();
			
			Users user = new Users(req.getShowname(), req.getUsername(), password, currentDate, currentDate, req.getEnabled(), authorities, req.getProductIds(), 1);
			user.setFirstName(req.getFirstName());
			user.setLastName(req.getLastName());
			user.setPhoneNumber(req.getPhoneNumber());
			user.setPhoneExt(req.getPhoneExt());
			user.setTitle(req.getTitle());
			
			if(!CollectionUtils.isEmpty(user.getProducts())) {
				UserSetting userSetting = new UserSetting();
				userSetting.setCurrentProduct(user.getProducts().get(0));
				user.setSetting(userSetting);
			}
			
			if(req.getImgContent() != null) {
				ImgData imgData = new ImgData(req.getImgName(), Base64.decode(req.getImgContent().getBytes()));
				user.setImgData(imgData);
				LOG.debug("Save image");
			}
			
			userRepository.save(user);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Users editUser(String id) throws Exception {
		try {
			Query query = Query.query(Criteria.where("id").is(id));
			query.fields()
			.include("imgData.imgContent")
			.include("products")
			.include("firstName")
			.include("lastName")
			.include("title")
			.include("phoneNumber")
			.include("phoneExt");
			
			Users user = template.findOne(query, Users.class);
			return user;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateUser(PersistUserCriteriaReq req) throws Exception {
		try {
			Users user = userRepository.findOne(req.getId());
			
			if(!user.getShowname().equals(req.getShowname())) {
				Users u = userRepository.findByShowname(req.getShowname());
				if(u != null)
					throw new CustomerException(2001, "This username_show is existing");
			}
			
			if(!user.getUsername().equals(req.getUsername())) {
				Users u = userRepository.findByUsername(req.getUsername());
				if(u != null)
					throw new CustomerException(2000, "This username is existing");
			}
			
			if(!StringUtils.isBlank(req.getPassword())) {
				String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));				
				user.setPassword(password);
			}
			
			user.setShowname(req.getShowname());
			user.setUsername(req.getUsername());
			user.setEnabled(req.getEnabled());
			user.setUpdatedDateTime(new Date());
			user.setProducts(req.getProductIds());

			user.setFirstName(req.getFirstName());
			user.setLastName(req.getLastName());
			user.setPhoneNumber(req.getPhoneNumber());
			user.setPhoneExt(req.getPhoneExt());
			user.setTitle(req.getTitle());
			
			if(req.getIsChangedImg()) {
				ImgData imgData;
				
				if(req.getImgContent() != null) {
					imgData = new ImgData(req.getImgName(), Base64.decode(req.getImgContent().getBytes()));					
				} else {
					imgData = new ImgData();
				}
				user.setImgData(imgData);
				LOG.debug("Save image");
			}
			
			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority(req.getAuthority()));
			
			user.setAuthorities(authorities);
			
			userRepository.save(user);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteUser(String id) throws Exception {
		try {
			Users user = template.findOne(Query.query(Criteria.where("id").is(id)), Users.class);
			List<String> userPros = user.getProducts();
			MongoTemplate temp;
			Update update;
			
			for (String prodId : userPros) {
				temp = dbFactory.getTemplates().get(prodId);
				
				update = new Update();
				update.set(SYS_OWNER_ID.getName(), null);
				
				temp.updateMulti(Query.query(Criteria.where(SYS_OWNER_ID.getName() + ".0").is(id)), update, NEW_TASK_DETAIL.getName());
			}
			
			userRepository.delete(id);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Users getProfile(String username) throws Exception {
		try {
			Query query = Query.query(Criteria.where("username").is(username));
			query.fields()
			.include("firstName")
			.include("lastName")
			.include("phoneNumber")
			.include("title")
			.include("imgData.imgContent");
			
			Users user = template.findOne(query, Users.class);
			return user;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public ProfileUpdateCriteriaResp updateProfile(ProfileUpdateCriteriaReq req) throws Exception {
		try {
			ProfileUpdateCriteriaResp resp = new ProfileUpdateCriteriaResp();
			Users u;
			
			if(!req.getNewUserNameShow().equals(req.getOldUserNameShow())) {
				u = userRepository.findByShowname(req.getNewUserNameShow());
				if(u != null)
					throw new CustomerException(2001, "This username_show is existing");	
			}
			
			if(!req.getNewUserName().equals(req.getOldUserName())) {
				u = userRepository.findByUsername(req.getNewUserName());
				if(u != null)
					throw new CustomerException(2000, "This username is existing");	
			}
			
			Users user = userRepository.findByUsername(req.getOldUserName());
			user.setShowname(req.getNewUserNameShow());
			user.setUsername(req.getNewUserName());
			user.setUpdatedDateTime(new Date());
			user.setFirstName(req.getFirstName());
			user.setLastName(req.getLastName());
			user.setPhoneNumber(req.getPhoneNumber());
			user.setPhoneExt(req.getPhoneExt());
			user.setTitle(req.getTitle());
			
			if(req.getIsChangedImg()) {
				ImgData imgData;
				
				if(req.getImgContent() != null) {
					imgData = new ImgData(req.getImgName(), Base64.decode(req.getImgContent().getBytes()));					
				} else {
					imgData = new ImgData();
					resp.setDefaultThumbnail(ImageUtil.getDefaultThumbnail(servletContext));
				}
				user.setImgData(imgData);
				LOG.debug("Save image");
			}
			
			if(!StringUtils.isBlank(req.getPassword())) {
				String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));		
				user.setPassword(password);
			}
						
			userRepository.save(user);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateUserSetting(UserSettingCriteriaReq req) throws Exception {
		try {
			Users user = userRepository.findByUsername(req.getUsername());
			UserSetting setting = user.getSetting();
			
			if(setting == null) {				
				setting = new UserSetting();
				user.setSetting(setting);
			}
			
			if(!StringUtils.isBlank(req.getCurrentProduct())) {
				setting.setCurrentProduct(req.getCurrentProduct());
			}
						
			userRepository.save(user);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Users> getUserByProduct(String productId, List<String> roles) throws Exception {
		try {
			Criteria criteria = Criteria.where("enabled").is(true).and("products").in(productId).and("authorities.role").in(roles);
			Query query = Query.query(criteria).with(new Sort("order", "username"));
			query.fields()
			.include("username")
			.include("showname")
			.include("firstName")
			.include("lastName")
			.include("authorities");
		
			List<Users> users = template.find(query, Users.class);				
			return users;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void reOrder(ReOrderCriteriaReq req) throws Exception {
		try {
			Query query;
			
			for (int i = 0; i < req.getIds().size(); i++) {
				query = Query.query(Criteria.where("id").is(req.getIds().get(i)));
				template.updateFirst(query, Update.update("order", i + 1), Users.class);
			}			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
