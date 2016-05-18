package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.PersistUserCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaResp;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.repository.UserRepository;

@Service
public class UserService {
	private static final Logger LOG = Logger.getLogger(UserService.class.getName());
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	
	@Autowired	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	public UserSearchCriteriaResp findAllUser(UserSearchCriteriaReq req) throws Exception {
		UserSearchCriteriaResp resp = new UserSearchCriteriaResp();
		
		try {		
			List<Users> users = userRepository.findAll();
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
			
			Users user = new Users(req.getShowname(), req.getUsername(), password, currentDate, currentDate, req.getEnabled(), authorities);
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
			
			user.setShowname(req.getShowname());
			user.setUsername(req.getUsername());
			user.setEnabled(req.getEnabled());
			user.setUpdatedDateTime(new Date());
			
			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority(req.getAuthority()));
			
			user.setAuthorities(authorities);
			
			userRepository.save(user);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	
	
	
	/*public UserSearchCriteriaResp findAllUser(UserSearchCriteriaReq req) throws Exception {
		UserSearchCriteriaResp resp = new UserSearchCriteriaResp();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {		
			StringBuilder sql = new StringBuilder();
			sql.append(" select u.id as id, u.username_show as username_show, u.username as username, u.enabled as enabled, ");
			sql.append(" u.created_date_time as created_date_time, r.authority as authority, r.name as name ");
			sql.append(" from users u join roles r on u.username = r.username where 1=1 ");
			
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
	}*/
	
	/*
	
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
	}*/
	
}
