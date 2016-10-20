package com.may.ple.backend.action;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.may.ple.backend.constant.RolesConstant;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.AuthenticationRequest;
import com.may.ple.backend.model.AuthenticationResponse;
import com.may.ple.backend.repository.UserRepository;
import com.may.ple.backend.security.CerberusUser;
import com.may.ple.backend.security.TokenUtils;
import com.may.ple.backend.utils.ImageUtil;

@RestController
public class LoginAction {
	private static final Logger LOG = Logger.getLogger(LoginAction.class.getName());
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private TokenUtils tokenUtils;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MongoTemplate template;
	@Autowired
    ServletContext servletContext;
	
	@RequestMapping(value="/login", method = RequestMethod.POST)
	public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest, Device device) throws Exception {
		try {			
			LOG.debug("Start Login");
		    Authentication authentication = authenticationManager.authenticate(
		    		new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), new String(Base64.decode(authenticationRequest.getPassword().getBytes())))
		    );
		    
		    SecurityContextHolder.getContext().setAuthentication(authentication);
		    
		    UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken)authentication;
		    CerberusUser cerberusUser = (CerberusUser)authToken.getPrincipal();

		    String token = tokenUtils.generateToken(cerberusUser, device);		    
		    
		    List<Product> products = prePareProduct(cerberusUser.getProducts());
		    LOG.debug("End Login");
		    
		    if(cerberusUser.getPhoto() == null) {
		    	LOG.debug("Use default thumbnail");
		    	cerberusUser.setPhoto(ImageUtil.getDefaultThumbnail(servletContext));
		    }
		    
		    AuthenticationResponse resp = new AuthenticationResponse(token, cerberusUser.getId(), cerberusUser.getShowname(), cerberusUser.getUsername(), cerberusUser.getAuthorities(), products, cerberusUser.getSetting(), cerberusUser.getPhoto());
		    
		    String companyName = getCompanyName();
		    
		    if(cerberusUser.getSetting() != null) {
		    	Integer workingTime = workingTimeCalculation(cerberusUser.getSetting().getCurrentProduct(), resp, authentication);
		    	
		    	if(workingTime != null) {
					if(workingTime < 0) {		
						LOG.warn("The time out of working time.");
						return ResponseEntity.status(410).build();
					}
			    	resp.setWorkingTime(workingTime);
		    	}
		    }
		    
		    resp.setServerDateTime(new Date());
		    resp.setFirstName(cerberusUser.getFirstName());
		    resp.setLastName(cerberusUser.getLastName());
		    resp.setPhoneNumber(cerberusUser.getPhoneNumber());
		    resp.setTitle(cerberusUser.getTitle());
		    resp.setCompanyName(companyName);
		    
		    return ResponseEntity.ok(resp);
		} catch (BadCredentialsException e) {
			LOG.error(e.toString(), e);
			throw e;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@RequestMapping(value="/refreshToken", method = RequestMethod.POST)
	public ResponseEntity<?> refreshToken(@RequestBody AuthenticationRequest authenticationRequest, Device device) throws Exception {
		try {			
			LOG.debug("Start refreshToken");
			
			String token = tokenUtils.refreshToken(authenticationRequest.getToken());
			
			if(token == null) {
				return ResponseEntity.status(401).build();
			}
			
			String username = tokenUtils.getUsernameFromToken(token);			
			Users user = userRepository.findByUsername(username);
			
			if(!user.getEnabled()) {
				LOG.debug("User is inactive");
				return ResponseEntity.status(410).build();
			}
			
			byte[] photo = null;
			if(user.getImgData() != null && user.getImgData().getImgContent() != null) {
				photo = user.getImgData().getImgContent();
			} else {
				LOG.debug("Use default thumbnail");
				photo = ImageUtil.getDefaultThumbnail(servletContext);
			}
			
			List<Product> products = prePareProduct(user.getProducts());
			LOG.debug("End refreshToken");
			
			AuthenticationResponse resp = new AuthenticationResponse(token, user.getId(), user.getShowname(), user.getUsername(), user.getAuthorities(), products, user.getSetting(), photo);
			
			String companyName = getCompanyName();
			
			if(user.getSetting() != null) {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				Integer workingTime = workingTimeCalculation(user.getSetting().getCurrentProduct(), resp, authentication);
				
				if(workingTime != null) {
					if(workingTime <= 0) {		
						LOG.warn("The time out of working time.");
						return ResponseEntity.status(400).build();
					}
			    	resp.setWorkingTime(workingTime);
				}
		    }
			
			resp.setServerDateTime(new Date());
			resp.setFirstName(user.getFirstName());
		    resp.setLastName(user.getLastName());
		    resp.setPhoneNumber(user.getPhoneNumber());
		    resp.setTitle(user.getTitle());
		    resp.setCompanyName(companyName);
		    
		    return ResponseEntity.ok(resp);
		} catch (BadCredentialsException e) {
			LOG.error(e.toString(), e);
			throw e;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	private List<Product> prePareProduct(List<String> products) {
		List<Product> allProds;
		Criteria criteria = Criteria.where("enabled").is(1);
		Query query = Query.query(criteria);
		query.fields().include("productName");
		query.with(new Sort("productName"));
		
	    if(products == null) {
	    	allProds = template.find(query, Product.class);
	    } else {
	    	criteria.and("id").in(products);
	    	allProds = template.find(query, Product.class);
	    }
	    
	    return allProds;
	}
	
	private String getCompanyName() {
		ApplicationSetting find = template.findOne(new Query(), ApplicationSetting.class);
		return find == null ? null : find.getCompanyName();
	}
	
	private Integer workingTimeCalculation(String productId, AuthenticationResponse resp, Authentication authentication) {
		if(StringUtils.isBlank(productId)) return null;
		
		List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>)authentication.getAuthorities();
	    RolesConstant rolesConstant = RolesConstant.valueOf(authorities.get(0).getAuthority());
	    
	    if(rolesConstant != RolesConstant.ROLE_USER) {
	    	return null;
	    }
		
		Product product = template.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
		ProductSetting setting = product.getProductSetting();
		
		if(setting == null) {
			return null;
		}
		
		LocalTime startTime, endTime;
		LocalTime nowTime = new LocalTime();
		int seconds;
		
		if(setting.getStartTimeH() != null && setting.getStartTimeM() != null) {
			startTime = new LocalTime(setting.getStartTimeH(), setting.getStartTimeM());	
			seconds = Seconds.secondsBetween(startTime, nowTime).getSeconds();
			
			if(seconds <= 0) return seconds;
		}
		
		if(setting.getEndTimeH() != null && setting.getEndTimeM() != null) {
			endTime = new LocalTime(setting.getEndTimeH(), setting.getEndTimeM());	
			seconds = Seconds.secondsBetween(nowTime, endTime).getSeconds();	
			return seconds;
		}
		
		return 0;
	}
	
}
