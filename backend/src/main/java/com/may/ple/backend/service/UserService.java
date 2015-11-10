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
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.repository.UserRepository;

@Service
public class UserService {
	private static final Logger LOG = Logger.getLogger(UserService.class.getName());
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private DataSource dataSource;
	
	@Autowired	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, DataSource dataSource) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.dataSource = dataSource;
	}
	
	public UserSearchCriteriaResp findAllUser(UserSearchCriteriaReq req) throws Exception {
		UserSearchCriteriaResp resp = new UserSearchCriteriaResp();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {		
			StringBuilder sql = new StringBuilder();
			sql.append(" select * ");
			sql.append(" from users where 1=1 ");
			
			if(req != null) {
				if(req.getUserName() != null) {
					sql.append(" and username like '%" + req.getUserName() + "%' ");
				}
				if(req.getRole() != null) {
					sql.append(" and username like '%" + req.getUserName() + "%' ");
				}
				if(req.getStatus() != null) {
					sql.append(" and enabled = " + req.getStatus());
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
			
			sql.append(" order by created_date_time asc ");
			sql.append(" limit " + (req.getCurrentPage() - 1) * req.getItemsPerPage() + ", " + req.getItemsPerPage());
			
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			List<Users> users = new ArrayList<Users>();
			List<Roles> roles;
			Users user;
			Roles role;
			
			while(rst.next()) {
				role = new Roles("", "", "");
				roles = new ArrayList<Roles>();
				roles.add(role);
				
				user = new Users(rst.getString(""), null, rst.getTimestamp("out_date_time"), null, rst.getInt(""), roles);
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
	
	public void saveUser(PersistUserCriteriaReq req) throws Exception {
		try {
			Users u = userRepository.findByUserName(req.getUserName());
			if(u != null) {
				throw new CustomerException(2000, "This username is existing");
			}
			
			String password = passwordEncoder.encode(new String(Base64.decode(req.getPassword().getBytes())));
			List<Roles> roles = getRole(req.getUserName(), req.getAuthority());
			Date currentDate = new Date();
			
			Users user = new Users(req.getUserName(), password, currentDate, currentDate, req.getStatus(), roles);
			userRepository.save(user);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateUser(PersistUserCriteriaReq req) throws Exception {
		try {
			Users user = userRepository.findOne(req.getId());
			
			if(!user.getUserName().equals(req.getUserName())) {
				Users u = userRepository.findByUserName(req.getUserName());
				if(u != null)
					throw new CustomerException(2000, "This username is existing");
			}
			
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
			if(!req.getNewUserName().equals(req.getOldUserName())) {
				Users u = userRepository.findByUserName(req.getNewUserName());
				if(u != null)
					throw new CustomerException(2000, "This username is existing");	
			}
			
			Users user = userRepository.findByUserName(req.getOldUserName());
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
