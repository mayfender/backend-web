package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.may.ple.backend.constant.RolesConstant;
import com.may.ple.backend.criteria.PersistUserCriteriaReq;
import com.may.ple.backend.criteria.ProfileUpdateCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaResp;
import com.may.ple.backend.criteria.UserSettingCriteriaReq;
import com.may.ple.backend.entity.UserSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.repository.UserRepository;

@Service
public class UserService {
	private static final Logger LOG = Logger.getLogger(UserService.class.getName());
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private MongoTemplate template;
	
	@Autowired	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MongoTemplate template) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.template = template;
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
			
			Users u = userRepository.findByUsername(req.getUsername());
			
			if(u != null) {
				throw new CustomerException(2000, "This username is existing");
			}
			
			String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));
			
			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority(req.getAuthority()));
			
			Date currentDate = new Date();
			
			Users user = new Users(req.getShowname(), req.getUsername(), password, currentDate, currentDate, req.getEnabled(), authorities, req.getProductIds());
			userRepository.save(user);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateUser(PersistUserCriteriaReq req) throws Exception {
		try {
			Users user = userRepository.findOne(req.getId());
			
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
			
			if(!req.getNewUserName().equals(req.getOldUserName())) {
				u = userRepository.findByUsername(req.getNewUserName());
				if(u != null)
					throw new CustomerException(2000, "This username is existing");	
			}
			
			Users user = userRepository.findByUsername(req.getOldUserName());
			user.setShowname(req.getNewUserNameShow());
			user.setUsername(req.getNewUserName());
			user.setUpdatedDateTime(new Date());
			
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
			Query query = Query.query(criteria).with(new Sort("username"));
			query.fields().include("username");
		
			List<Users> users = template.find(query, Users.class);				
			return users;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
