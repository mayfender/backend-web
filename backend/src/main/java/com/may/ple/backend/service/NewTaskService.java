package com.may.ple.backend.service;

import java.io.InputStream;
import java.util.Date;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.NewTaskFile;
import com.may.ple.backend.model.DbFactory;

@Service
public class NewTaskService {
	private static final Logger LOG = Logger.getLogger(NewTaskService.class.getName());
	private DbFactory dbFactory;
	
	@Autowired
	public NewTaskService(DbFactory dbFactory) {
		this.dbFactory = dbFactory;
	}
	
	/*public UserSearchCriteriaResp findAllUser(UserSearchCriteriaReq req) throws Exception {
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
			
			long totalItems = template.count(new Query(criteria), Users.class);
			
			Query query = new Query(criteria)
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
	}*/
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get("krungsi_debt_db");
			template.insert(new NewTaskFile(fileDetail.getFileName(), new Date()));
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
