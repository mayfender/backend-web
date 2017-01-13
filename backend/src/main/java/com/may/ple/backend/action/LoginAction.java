package com.may.ple.backend.action;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import net.nicholaswilliams.java.licensing.License;
import net.nicholaswilliams.java.licensing.LicenseManager;
import net.nicholaswilliams.java.licensing.exception.ExpiredLicenseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	@Value("${backend.version}")
	String version;
	
	@RequestMapping(value="/login", method = RequestMethod.POST)
	public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest, Device device) throws Exception {
		AuthenticationResponse resp;
		
		try {			
			LOG.debug("Call getAppSetting");
			ApplicationSetting appSetting = getAppSetting();
			
			LOG.debug("Check License");
			License license = checkLicense(appSetting.getProductKey());
			long expiredDate = license.getGoodBeforeDate();
			
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
		    
		    resp = new AuthenticationResponse(token, cerberusUser.getId(), cerberusUser.getShowname(), cerberusUser.getUsername(), cerberusUser.getAuthorities(), products, cerberusUser.getSetting(), cerberusUser.getPhoto());
		    
		    String companyName = getCompanyName();
		    
		    if(cerberusUser.getSetting() != null) {
		    	ProductSetting setting;
		    	
		    	if(!StringUtils.isBlank(cerberusUser.getSetting().getCurrentProduct())) {
		    		setting = getProdSetting(cerberusUser.getSetting().getCurrentProduct(), products.get(0).getId());
		    	} else {
		    		setting = getProdSetting(products.get(0).getId(), null);
		    	}
		    	
		    	Integer workingTime = workingTimeCalculation(setting, resp, authentication);
		    	
		    	boolean isValid = checkWorkingTime(workingTime, resp);
		    	if(!isValid) {
		    		return ResponseEntity.status(410).build();
		    	}
		    }
		    
		    resp.setServerDateTime(new Date());
		    resp.setFirstName(cerberusUser.getFirstName());
		    resp.setLastName(cerberusUser.getLastName());
		    resp.setPhoneNumber(cerberusUser.getPhoneNumber());
		    resp.setPhoneExt(cerberusUser.getPhoneExt());
		    resp.setTitle(cerberusUser.getTitle());
		    resp.setCompanyName(companyName);
		    resp.setVersion(version);
		    resp.setPhoneWsServer(appSetting.getPhoneWsServer());
		    resp.setPhoneRealm(appSetting.getPhoneRealm());
		    resp.setPhonePass(appSetting.getPhoneDefaultPass());
		    
		    return ResponseEntity.ok(resp);
		} catch (BadCredentialsException e) {
			LOG.error(e.toString(), e);
			throw e;
		} catch (ExpiredLicenseException e) {
			LOG.error(e.toString());
			resp = new AuthenticationResponse();
			resp.setIsLicenseNotValid(true);
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@RequestMapping(value="/refreshToken", method = RequestMethod.POST)
	public ResponseEntity<?> refreshToken(@RequestBody AuthenticationRequest authenticationRequest, Device device) throws Exception {
		AuthenticationResponse resp;
		
		try {			
			LOG.debug("Call getAppSetting");
			ApplicationSetting appSetting = getAppSetting();
			
			LOG.debug("Check License");
			checkLicense(appSetting.getProductKey());
			
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
			
			resp = new AuthenticationResponse(token, user.getId(), user.getShowname(), user.getUsername(), user.getAuthorities(), products, user.getSetting(), photo);
			
			String companyName = getCompanyName();
			
			if(user.getSetting() != null) {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();				
				ProductSetting setting;
				
		    	if(!StringUtils.isBlank(user.getSetting().getCurrentProduct())) {
		    		setting = getProdSetting(user.getSetting().getCurrentProduct(), products.get(0).getId());
				} else {
		    		setting = getProdSetting(products.get(0).getId(), null);
		    	}
		    	
		    	Integer workingTime = workingTimeCalculation(setting, resp, authentication);
		    	
				boolean isValid = checkWorkingTime(workingTime, resp);
		    	if(!isValid) {
		    		return ResponseEntity.status(410).build();
		    	}
		    }
						
			resp.setServerDateTime(new Date());
			resp.setFirstName(user.getFirstName());
		    resp.setLastName(user.getLastName());
		    resp.setPhoneNumber(user.getPhoneNumber());
		    resp.setPhoneExt(user.getPhoneExt());
		    resp.setTitle(user.getTitle());
		    resp.setCompanyName(companyName);
		    resp.setVersion(version);
		    resp.setPhoneWsServer(appSetting.getPhoneWsServer());
		    resp.setPhoneRealm(appSetting.getPhoneRealm());
		    resp.setPhonePass(appSetting.getPhoneDefaultPass());
		    
		    return ResponseEntity.ok(resp);
		} catch (BadCredentialsException e) {
			LOG.error(e.toString(), e);
			throw e;
		} catch (ExpiredLicenseException e) {
			LOG.error(e.toString());
			resp = new AuthenticationResponse();
			resp.setIsLicenseNotValid(true);
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@RequestMapping(value="/refreshClock", method = RequestMethod.POST)
	public ResponseEntity<?> refreshClock(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
		try {			
			LOG.debug("Start refreshClock");
			
			String token = tokenUtils.refreshToken(authenticationRequest.getToken());
			
			if(token == null) {
				return ResponseEntity.status(401).build();
			}
			
			String username = tokenUtils.getUsernameFromToken(token);			
			Users user = userRepository.findByUsername(username);
						
			AuthenticationResponse resp = new AuthenticationResponse();
			
			if(user.getSetting() != null) {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();				
				Integer workingTime = null;
				
		    	if(!StringUtils.isBlank(user.getSetting().getCurrentProduct())) {
		    		ProductSetting setting = getProdSetting(user.getSetting().getCurrentProduct(), null);
		    		workingTime = workingTimeCalculation(setting, resp, authentication);
		    	}
				
				boolean isValid = checkWorkingTime(workingTime, resp);
		    	if(!isValid) {
		    		return ResponseEntity.status(410).build();
		    	}
		    }
			
			Calendar now = Calendar.getInstance();
			resp.setServerDateTime(now.getTime());
		    
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
	
	private ProductSetting getProdSetting(String productId, String productIdReserve) {
		Product product = template.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
		
		if(product == null) {
			product = template.findOne(Query.query(Criteria.where("id").is(productIdReserve)), Product.class);
		}
		
		return product == null ? null : product.getProductSetting();
	}
	
	private ApplicationSetting getAppSetting() {
		return template.findOne(new Query(), ApplicationSetting.class);
	}
	
	private Integer workingTimeCalculation(ProductSetting setting, AuthenticationResponse resp, Authentication authentication) {
		
		List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>)authentication.getAuthorities();
	    RolesConstant rolesConstant = RolesConstant.valueOf(authorities.get(0).getAuthority());
	    
	    if(rolesConstant != RolesConstant.ROLE_USER) {
	    	return null;
	    }
		
		LocalTime startTime, endTime;
		LocalDate newDate = new LocalDate();
		LocalTime nowTime = new LocalTime();
		int dayOfWeek = newDate.getDayOfWeek();
		Integer startTimeH, startTimeM, endTimeH, endTimeM;
		Boolean isEnable;
		int seconds;
		
		if(DateTimeConstants.SATURDAY == dayOfWeek) {
			isEnable = setting.getSatWorkingDayEnable();
			startTimeH = setting.getSatStartTimeH();
			startTimeM = setting.getSatStartTimeM();
			endTimeH = setting.getSatEndTimeH();
			endTimeM = setting.getSatEndTimeM();
		} else if(DateTimeConstants.SUNDAY == dayOfWeek) {
			isEnable = setting.getSunWorkingDayEnable();
			startTimeH = setting.getSunStartTimeH();
			startTimeM = setting.getSunStartTimeM();
			endTimeH = setting.getSunEndTimeH();
			endTimeM = setting.getSunEndTimeM();
		} else {
			isEnable = setting.getNormalWorkingDayEnable();
			startTimeH = setting.getNormalStartTimeH();
			startTimeM = setting.getNormalStartTimeM();
			endTimeH = setting.getNormalEndTimeH();
			endTimeM = setting.getNormalEndTimeM();
		}
		
		if(isEnable != null && !isEnable) {
			LOG.info("Working time is disabled");
			return null;
		}
		
		if(startTimeH != null && startTimeM != null) {
			startTime = new LocalTime(startTimeH, startTimeM);	
			seconds = Seconds.secondsBetween(startTime, nowTime).getSeconds();
			
			if(seconds <= 0) return seconds;
		}
		
		if(endTimeH != null && endTimeM != null) {
			endTime = new LocalTime(endTimeH, endTimeM);	
			seconds = Seconds.secondsBetween(nowTime, endTime).getSeconds();	
			return seconds;
		}
		
		return null;
	}
	
	private boolean checkWorkingTime(Integer workingTime, AuthenticationResponse resp) {
		if(workingTime != null) {
			if(workingTime <= 0) {		
				LOG.warn("The time out of working time.");
				return false;
			}
	    	resp.setWorkingTime(workingTime);
    	}
		return true;
	}
	
	private License checkLicense(String productKey) {
		try {
			if(StringUtils.isBlank(productKey)) throw new ExpiredLicenseException("Product Key is empty"); 
			
			LicenseManager manager = LicenseManager.getInstance();
			License license = manager.getLicense("");
			
			if(!license.getProductKey().equals(productKey)) throw new ExpiredLicenseException("Product Key is not match."); 
			
			manager.validateLicense(license);
			
			return license;
		} catch (ExpiredLicenseException e) {
			throw e;
		} catch (Exception e) {
			throw new ExpiredLicenseException("Not really expired have some thing error");
		}
	}
	
}
