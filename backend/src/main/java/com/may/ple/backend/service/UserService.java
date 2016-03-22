package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.may.ple.backend.constant.RolesConstant;
import com.may.ple.backend.criteria.PersistUserCriteriaReq;
import com.may.ple.backend.criteria.ProfileUpdateCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaResp;
import com.may.ple.backend.entity.Roles;
import com.may.ple.backend.entity.SptMasterNamingDet;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.repository.SptMasterNamingDetRepository;
import com.may.ple.backend.repository.UserRepository;

@Service
public class UserService {
	private static final Logger LOG = Logger.getLogger(UserService.class.getName());
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private DataSource dataSource;
	private SptMasterNamingDetRepository sptMasterNamingDetRepository;
	
	@Autowired	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, DataSource dataSource, SptMasterNamingDetRepository sptMasterNamingDetRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.dataSource = dataSource;
		this.sptMasterNamingDetRepository = sptMasterNamingDetRepository;
	}
	
	public UserSearchCriteriaResp findAllUser(UserSearchCriteriaReq req) throws Exception {
		UserSearchCriteriaResp resp = new UserSearchCriteriaResp();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {		
			StringBuilder sql = new StringBuilder();
			sql.append(" select u.id as id, u.username_show as username_show, u.username as username, u.enabled as enabled, ");
			sql.append(" u.created_date_time as created_date_time, u.work_position_id, r.authority as authority, r.name as name, n.display_value ");
			sql.append(" from users u join roles r on u.username = r.username join spt_master_naming_det n on u.work_position_id = n.naming_det_id ");
			sql.append(" where u.work_position_id is not null ");
			
			if(req != null) {
				if(!StringUtils.isBlank(req.getUserNameShow())) {
					sql.append(" and u.username_show like '%" + req.getUserNameShow() + "%' ");
				}
				if(!StringUtils.isBlank(req.getUserName())) {
					sql.append(" and u.username like '%" + req.getUserName() + "%' ");
				}
				if(!StringUtils.isBlank(req.getRole())) {
					sql.append(" and r.authority = '" + req.getRole() + "' ");
				}
				if(req.getStatus() != null) {
					sql.append(" and u.enabled = " + req.getStatus());
				}
			}
			
			try {			
				StringBuilder sqlCount = new StringBuilder();
				sqlCount.append("select count(id) as size from ( " + sql.toString()+ " ) sub");
				
				conn = dataSource.getConnection();
				pstmt = conn.prepareStatement(sqlCount.toString());
				rst = pstmt.executeQuery();
				
				if(rst.next()) {
					resp.setTotalItems(rst.getLong("size"));
				}
			} catch (Exception e) {
				LOG.error(e.toString());
				throw e;
			} finally {
				try { if(rst != null) rst.close(); } catch (Exception e2) {}
				try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			}
			
			sql.append(" order by u.created_date_time desc ");
			sql.append(" limit " + (req.getCurrentPage() - 1) * req.getItemsPerPage() + ", " + req.getItemsPerPage());
			
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			List<Users> users = new ArrayList<Users>();
			List<Roles> roles;
			Users user;
			Roles role;
			
			while(rst.next()) {
				role = new Roles(null, rst.getString("authority"), rst.getString("name"));
				roles = new ArrayList<Roles>();
				roles.add(role);
				
				user = new Users(rst.getString("username_show"), rst.getString("username"), null, rst.getTimestamp("created_date_time"), null, rst.getInt("enabled"), roles);
				user.setWorkPositionId(rst.getLong("work_position_id"));
				user.setWorkPositionName(rst.getString("display_value"));
				user.setId(rst.getLong("id"));
				
				users.add(user);
			}
			resp.setUsers(users);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	public Long saveUser(PersistUserCriteriaReq req) throws Exception {
		try {
			/*Users u = userRepository.findByUserNameShow(req.getUserNameShow());
			
			if(u != null) {
				throw new CustomerException(2001, "This username_show is existing");
			}*/
			
			Users u = userRepository.findByUserName(req.getUserName());
			if(u != null) {
				throw new CustomerException(2000, "This username is existing");
			}
			
			String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));
			List<Roles> roles = getRole(req.getUserName(), req.getAuthority());
			Date currentDate = new Date();
			
			
			Users user = new Users(req.getUserNameShow(), req.getUserName(), password, currentDate, currentDate, req.getStatus(), roles);
			
			if(req.getWorkPositionId() != null) {
				SptMasterNamingDet sptMasterNamingDet = sptMasterNamingDetRepository.findOne(req.getWorkPositionId());
				user.setSptMasterNamingDet(sptMasterNamingDet);				
			}
			
			userRepository.save(user);
			
			return user.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateUser(PersistUserCriteriaReq req) throws Exception {
		try {
			Users user = userRepository.findOne(req.getId());
			
			/*if(!user.getUserNameShow().equals(req.getUserNameShow())) {
				Users u = userRepository.findByUserNameShow(req.getUserNameShow());
				if(u != null)
					throw new CustomerException(2001, "This username_show is existing");
			}*/
			
			if(!user.getUserName().equals(req.getUserName())) {
				Users u = userRepository.findByUserName(req.getUserName());
				if(u != null)
					throw new CustomerException(2000, "This username is existing");
			}
			
			if(req.getPassword() != null) {
				String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));				
				user.setPassword(password);
			}
			
			if(req.getWorkPositionId() != null) {
				SptMasterNamingDet sptMasterNamingDet = sptMasterNamingDetRepository.findOne(req.getWorkPositionId());
				user.setSptMasterNamingDet(sptMasterNamingDet);				
			}
			
			user.setUserNameShow(req.getUserNameShow());
			user.setUserName(req.getUserName());
			user.setEnabled(req.getStatus());
			user.setUpdatedDateTime(new Date());
			
			List<Roles> roles = getRole(req.getUserName(), req.getAuthority());
			Roles r = roles.get(0);
			
			Roles role = user.getRoles().get(0);
			role.setUserName(req.getUserName());
			role.setAuthority(r.getAuthority());
			role.setName(r.getName());
			
			userRepository.save(user);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteUser(long userId) throws Exception {
		try {
			userRepository.delete(userId);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateProfile(ProfileUpdateCriteriaReq req) throws Exception {
		try {
			Users u;
			
			if(!req.getNewUserNameShow().equals(req.getOldUserNameShow())) {
				u = userRepository.findByUserNameShow(req.getNewUserNameShow());
				if(u != null)
					throw new CustomerException(2001, "This username_show is existing");	
			}
			
			if(!req.getNewUserName().equals(req.getOldUserName())) {
				u = userRepository.findByUserName(req.getNewUserName());
				if(u != null)
					throw new CustomerException(2000, "This username is existing");	
			}
			
			Users user = userRepository.findByUserName(req.getOldUserName());
			user.setUserNameShow(req.getNewUserNameShow());
			user.setUserName(req.getNewUserName());
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
	
	public Users loadProfile(String userName) throws Exception {
		try {
			Users user = userRepository.findByUserName(userName);
			return user;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private List<Roles> getRole(String userName, String authority) throws Exception {
		RolesConstant roleConstant = RolesConstant.valueOf(authority);
		
		if(roleConstant == null)
			throw new Exception("Not found Role from authority : " + authority);
		
		List<Roles> roles = new ArrayList<Roles>();
		Roles role = new Roles(userName, roleConstant.toString(), roleConstant.getName());
		roles.add(role);
		return roles;
	}
	
}
