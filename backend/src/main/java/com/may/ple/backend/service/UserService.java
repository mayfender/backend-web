package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
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
import com.may.ple.backend.entity.ImgData;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.repository.UserRepository;
import com.may.ple.backend.utils.ImageUtil;

@Service
public class UserService {
	private static final Logger LOG = Logger.getLogger(UserService.class.getName());
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private ServletContext servletContext;
	private MongoTemplate template;

	@Autowired
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MongoTemplate template, ServletContext servletContext) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.servletContext = servletContext;
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
			boolean isSuperAdminRole = false;

			if(rolesConstant == RolesConstant.ROLE_ADMIN) {
				LOG.debug("Role is Admin");
				isAdminRole = true;
			} else if(rolesConstant == RolesConstant.ROLE_MANAGER) {
				LOG.debug("Role is Manager");
				isManagerRole = true;
			} else if(rolesConstant == RolesConstant.ROLE_SUPERADMIN) {
				LOG.debug("Role is Super Admin");
				isSuperAdminRole = true;
			}

			Criteria criteria = Criteria.where("showname").regex(Pattern.compile(req.getUserNameShow() == null ? "" : req.getUserNameShow(), Pattern.CASE_INSENSITIVE))
					            .and("username").regex(Pattern.compile(req.getUserName() == null ? "" : req.getUserName(), Pattern.CASE_INSENSITIVE));

			if(!StringUtils.isBlank(req.getRole())) {
				criteria.and("authorities.role").is(req.getRole());
			}
			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}
			/*if(isAdminRole) {
				List<SimpleGrantedAuthority> excludeAuthorities = new ArrayList<>();
				excludeAuthorities.add(new SimpleGrantedAuthority(RolesConstant.ROLE_ADMIN.toString()));
				criteria.and("authorities").ne(excludeAuthorities);
			}*/

			if(isSuperAdminRole) {
				/*List<SimpleGrantedAuthority> includeAuthorities = new ArrayList<>();
				includeAuthorities.add(new SimpleGrantedAuthority(RolesConstant.ROLE_SUPERADMIN.toString()));
				includeAuthorities.add(new SimpleGrantedAuthority(RolesConstant.ROLE_MANAGER.toString()));
				criteria.orOperator(Criteria.where("authorities").in(includeAuthorities), Criteria.where("products").in(req.getCurrentProduct()));*/
				criteria.and("dealerId").in(req.getDealerId());
			} else if(isManagerRole) {
				criteria.and("dealerId").in(req.getDealerId()).
				orOperator(
						Criteria.where("authorities").in(new SimpleGrantedAuthority(RolesConstant.ROLE_MANAGER.toString())),
						Criteria.where("authorities").in(new SimpleGrantedAuthority(RolesConstant.ROLE_ADMIN.toString())),
						Criteria.where("authorities").in(new SimpleGrantedAuthority(RolesConstant.ROLE_SUPERVISOR.toString())),
						Criteria.where("authorities").in(new SimpleGrantedAuthority(RolesConstant.ROLE_AGENT.toString()))
				);
				/*criteria.orOperator(
						Criteria.where("authorities").in(new SimpleGrantedAuthority(RolesConstant.ROLE_ADMIN.toString())).and("products").in(req.getCurrentProduct()),
						Criteria.where("authorities").in(new SimpleGrantedAuthority(RolesConstant.ROLE_LPS.toString())),
						Criteria.where("authorities").in(new SimpleGrantedAuthority(RolesConstant.ROLE_MANAGER.toString()))
						);*/
			} else {
				criteria.and("dealerId").in(req.getDealerId());
			}

			long totalItems = template.count(Query.query(criteria), Users.class);

			Query query = Query.query(criteria)
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort("authorities.role", "username", "showname"));
			query.fields()
			.include("showname")
			.include("username")
			.include("authorities")
			.include("enabled")
			.include("createdDateTime");

			List<Users> users = template.find(query, Users.class);

			resp.setTotalItems(totalItems);
			resp.setUsers(users);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<Users> getUsers(UserSearchCriteriaReq req) {
		try {
			Query query = Query.query(Criteria.where("dealerId").is(req.getDealerId()));
			query.fields().include("showname").include("username").include("authorities");
			List<Users> users = template.find(query, Users.class);
			RolesConstant rolesConstant;

			for (Users u : users) {
				rolesConstant = RolesConstant.valueOf(u.getAuthorities().get(0).getAuthority());
				u.setRoleId(rolesConstant.getId());
			}

			return users;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void saveUser(PersistUserCriteriaReq req) throws Exception {
		try {
			Users u;
			if(req.getDealerId() != null) {
				u = userRepository.findByShownameAndDealerId(req.getShowname(), req.getDealerId());
			} else {
				u = userRepository.findByShownameAndDealerIdIsNull(req.getShowname());
			}

			if(u != null) {
				throw new CustomerException(2001, "This username_show is existing");
			}

			u = userRepository.findByUsername(req.getUsername());

			if(u != null) {
				throw new CustomerException(2000, "This username is existing");
			}

			if(StringUtils.isNotBlank(req.getLineUserId())) {
				u = userRepository.findByLineUserId(req.getLineUserId());

				if(u != null) {
					throw new CustomerException(2002, "This Line User is existing");
				}
			}

			String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));

			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority(req.getAuthority()));

			Date currentDate = new Date();

			Users user = new Users(req.getShowname(), req.getUsername(), password, currentDate, currentDate, req.getEnabled(), authorities, 1);
			user.setFirstName(req.getFirstName());
			user.setLastName(req.getLastName());
			user.setTitle(req.getTitle());
			user.setLineUserId(req.getLineUserId());

			if(req.getDealerId() != null) {
				user.setDealerId(req.getDealerId());
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
			.include("lineUserId")
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
				Users u;
				if(req.getDealerId() != null) {
					u = userRepository.findByShownameAndDealerId(req.getShowname(), req.getDealerId());
				} else {
					u = userRepository.findByShownameAndDealerIdIsNull(req.getShowname());
				}

				if(u != null)
					throw new CustomerException(2001, "This username_show is existing");
			}

			if(!user.getUsername().equals(req.getUsername())) {
				Users u = userRepository.findByUsername(req.getUsername());
				if(u != null)
					throw new CustomerException(2000, "This username is existing");
			}

			if(StringUtils.isNotBlank(req.getLineUserId())) {
				if(StringUtils.isBlank(user.getLineUserId()) || !user.getLineUserId().equals(req.getLineUserId())) {
					Users u = userRepository.findByLineUserId(req.getLineUserId());
					if(u != null)
						throw new CustomerException(2002, "This Line User is existing");
				}
			}

			if(!StringUtils.isBlank(req.getPassword())) {
				String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));
				user.setPassword(password);
			}

			user.setShowname(req.getShowname());
			user.setUsername(req.getUsername());
			user.setEnabled(req.getEnabled());
			user.setUpdatedDateTime(new Date());

			user.setFirstName(req.getFirstName());
			user.setLastName(req.getLastName());
			user.setTitle(req.getTitle());
			user.setLineUserId(req.getLineUserId());

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
				if(req.getDealerId() != null) {
					u = userRepository.findByShownameAndDealerId(req.getNewUserNameShow(), req.getDealerId());
				} else {
					u = userRepository.findByShownameAndDealerIdIsNull(req.getNewUserNameShow());
				}

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

	public List<Users> getUser(String productId, List<String> roles) throws Exception {
		try {
			Criteria criteria = Criteria.where("enabled").is(true);

			if(!StringUtils.isBlank(productId)) {
				criteria.and("products").in(productId);
			}
			if(roles != null) {
				criteria.and("authorities.role").in(roles);
			}

			Query query = Query.query(criteria).with(new Sort("order", "showname"));
			query.fields()
			.include("username")
			.include("showname")
			.include("firstName")
			.include("lastName")
			.include("phoneNumber")
			.include("phoneExt")
			.include("authorities");

			List<Users> users = template.find(query, Users.class);
			return users;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<Users> getChatFriends(List<String> productId, Integer currentPage, Integer itemsPerPage, String keyword, String ownId, boolean ignoreSuperadmin) throws Exception {
		try {
			Criteria criteriaMaster = Criteria.where("enabled").is(true);
			if(!StringUtils.isBlank(ownId)) {
				criteriaMaster = Criteria.where("_id").not().in(new ObjectId(ownId));
			}
			if(ignoreSuperadmin) {
				criteriaMaster.and("authorities.role").nin("ROLE_SUPERADMIN");
			}

			if(!StringUtils.isBlank(keyword)) {
				List<Criteria> multiOr = new ArrayList<>();
				multiOr.add(Criteria.where("showname").regex(Pattern.compile(keyword, Pattern.CASE_INSENSITIVE)));
				multiOr.add(Criteria.where("firstName").regex(Pattern.compile(keyword, Pattern.CASE_INSENSITIVE)));
				multiOr.add(Criteria.where("lastName").regex(Pattern.compile(keyword, Pattern.CASE_INSENSITIVE)));
				Criteria[] multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
				criteriaMaster.orOperator(multiOrArr);
			} else {
				Criteria criteria = new Criteria();
				if(productId != null) {
					criteria.and("products").in(productId);
				}
				criteriaMaster.orOperator(criteria, Criteria.where("products").exists(false));
			}

			Query query = Query.query(criteriaMaster)
					  .with(new PageRequest(currentPage - 1, itemsPerPage))
		 			  .with(new Sort("showname"));
			query.fields()
			.include("showname")
			.include("username")
			.include("firstName")
			.include("lastName")
			.include("imgData");

			List<Users> users = template.find(query, Users.class);
			return users;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<Users> getUser(String productId, List<String> roles, List<String> userIds) throws Exception {
		try {
			Criteria criteria = Criteria.where("enabled").is(true);

			if(!StringUtils.isBlank(productId)) {
				criteria.and("products").in(productId);
			}
			if(roles != null) {
				criteria.and("authorities.role").in(roles);
			}
			if(userIds != null) {
				criteria.and("id").in(userIds);
			}

			Query query = Query.query(criteria).with(new Sort("order", "showname"));
			query.fields()
			.include("username")
			.include("showname")
			.include("firstName")
			.include("lastName")
			.include("phoneNumber")
			.include("authorities")
			.include("products");

			List<Users> users = template.find(query, Users.class);
			return users;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	@Deprecated
	public Users getUserById(String id) throws Exception {
		try {
			Query query = Query.query(Criteria.where("id").is(id));
			query.fields()
			.include("username")
			.include("showname")
			.include("firstName")
			.include("lastName")
			.include("phoneNumber")
			.include("authorities");

			Users users = template.findOne(query, Users.class);
			return users;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Users getUserByName(String username) throws Exception {
		try {
			Query query = Query.query(Criteria.where("username").is(username));
			query.fields()
			.include("username")
			.include("showname")
			.include("firstName")
			.include("lastName")
			.include("phoneNumber")
			.include("authorities");

			Users users = template.findOne(query, Users.class);
			return users;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Users getUserById(String id, String ...fields) throws Exception {
		try {
			Query query = Query.query(Criteria.where("id").is(id));
			Field field = query.fields();

			for (String f : fields) {
				field.include(f);
			}

			Users users = template.findOne(query, Users.class);
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
