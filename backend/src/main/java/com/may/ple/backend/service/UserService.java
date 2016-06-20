package com.may.ple.backend.service;

import java.util.ArrayList;
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
import com.may.ple.backend.criteria.ReOrderCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaResp;
import com.may.ple.backend.criteria.UserSettingCriteriaReq;
import com.may.ple.backend.entity.UserSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.repository.UserRepository;

@Service
public class UserService {
	private static final Logger LOG = Logger.getLogger(UserService.class.getName());
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private MongoTemplate template;
	private DbFactory dbFactory;
	
	@Autowired	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MongoTemplate template, DbFactory dbFactory) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.template = template;
		this.dbFactory = dbFactory;
	}
	
	public UserSearchCriteriaResp findAllUser(UserSearchCriteriaReq req) throws Exception {
		UserSearchCriteriaResp resp = new UserSearchCriteriaResp();
		
		try {
			
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>)authentication.getAuthorities();
			RolesConstant rolesConstant = RolesConstant.valueOf(authorities.get(0).getAuthority());
			boolean isAdminRole = false;
			
			if(rolesConstant == RolesConstant.ROLE_ADMIN) {
				LOG.debug("Find PRODUCTS underly admin");
				isAdminRole = true;
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
			if(req.getProduct() != null) {
				criteria.and("products").in(req.getProduct());
			} else if(req.getCurrentProduct() != null) {
				criteria.and("products").in(req.getCurrentProduct());
			}
			
			long totalItems = template.count(Query.query(criteria), Users.class);
			
			Query query = Query.query(criteria)
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort("authorities.role", "username", "showname"));
			query.fields().exclude("updatedDateTime").exclude("setting");
			
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
			userRepository.save(user);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateUser(PersistUserCriteriaReq req) throws Exception {
		try {
			Users user = userRepository.findOne(req.getId());
			boolean isChangedShowname = false;
			boolean isChangedUsername = false;
			
			if(!user.getShowname().equals(req.getShowname())) {
				Users u = userRepository.findByShowname(req.getShowname());
				if(u != null)
					throw new CustomerException(2001, "This username_show is existing");
				
				LOG.debug("ChangedShowname");
				isChangedShowname = true;
			}
			
			if(!user.getUsername().equals(req.getUsername())) {
				Users u = userRepository.findByUsername(req.getUsername());
				if(u != null)
					throw new CustomerException(2000, "This username is existing");
				
				LOG.debug("ChangedUsername");
				isChangedUsername = true;
			}
			
			if(!StringUtils.isBlank(req.getPassword())) {
				String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));				
				user.setPassword(password);
			}
			
			if(isChangedUsername || isChangedShowname) {
				updateAllRelatedTask(user, req.getUsername(), req.getShowname());				
			}
			
			user.setShowname(req.getShowname());
			user.setUsername(req.getUsername());
			user.setEnabled(req.getEnabled());
			user.setUpdatedDateTime(new Date());
			user.setProducts(req.getProductIds());
			
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
			userRepository.delete(id);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateProfile(ProfileUpdateCriteriaReq req) throws Exception {
		try {
			Users u;
			boolean isChangedShowname = false;
			boolean isChangedUsername = false;
			
			if(!req.getNewUserNameShow().equals(req.getOldUserNameShow())) {
				u = userRepository.findByShowname(req.getNewUserNameShow());
				if(u != null)
					throw new CustomerException(2001, "This username_show is existing");	
				
				isChangedShowname = true;
			}
			
			if(!req.getNewUserName().equals(req.getOldUserName())) {
				u = userRepository.findByUsername(req.getNewUserName());
				if(u != null)
					throw new CustomerException(2000, "This username is existing");	
				
				isChangedUsername = true;
			}
			
			
			Users user = userRepository.findByUsername(req.getOldUserName());
			user.setShowname(req.getNewUserNameShow());
			user.setUsername(req.getNewUserName());
			user.setUpdatedDateTime(new Date());
			
			if(isChangedUsername || isChangedShowname) {
				updateAllRelatedTask(user, req.getNewUserName(), req.getNewUserNameShow());
			}
			
			if(!StringUtils.isBlank(req.getPassword())) {
				String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));		
				user.setPassword(password);
			}
						
			userRepository.save(user);
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
			query.fields().include("username").include("showname").include("authorities");
		
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
	
	private void updateAllRelatedTask(Users user, String username, String showname) {
		LOG.debug("Start update user all-task and all-product");
		
		List<String> products = user.getProducts();
		Criteria criteria = Criteria.where("owner.username").in(user.getUsername());
		Query query = Query.query(criteria);
		MongoTemplate template;
		List<Map<String, String>> owers;
		List<Map> taskLst;
		Map<String, String> newOwner;
		
		for (String prodId : products) {
			template = dbFactory.getTemplates().get(prodId);			
			taskLst = template.find(query, Map.class, "newTaskDetail");
			
			for (Map map : taskLst) {
				owers = (List<Map<String, String>>)map.get("owner");
				
				for (int i = 0; i < owers.size(); i++) {
					if(user.getUsername().equals(owers.get(i).get("username"))) {
						owers.remove(i);
						newOwner = new HashMap<String, String>();
						newOwner.put("username", username);
						newOwner.put("showname", showname);
						owers.add(i, newOwner);
					}
				}
				template.save(map, "newTaskDetail");
			}
		}
		
		LOG.debug("End update user all-task and all-product");
	}
	
}
