package com.may.ple.backend.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

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
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

import net.nicholaswilliams.java.licensing.License;
import net.nicholaswilliams.java.licensing.LicenseManager;
import net.nicholaswilliams.java.licensing.exception.InvalidLicenseException;

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
			LOG.debug("Check License");
			checkLicense();
			
			LOG.debug("Start Login");
		    Authentication authentication = authenticationManager.authenticate(
		    		new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), new String(Base64.decode(authenticationRequest.getPassword().getBytes())))
		    );
		    
		    SecurityContextHolder.getContext().setAuthentication(authentication);
		    List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>)authentication.getAuthorities();
		    RolesConstant rolesConstant = RolesConstant.valueOf(authorities.get(0).getAuthority());
		    
		    UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken)authentication;
		    CerberusUser cerberusUser = (CerberusUser)authToken.getPrincipal();

		    String token = tokenUtils.generateToken(cerberusUser, device);
		    
		    if(cerberusUser.getPhoto() == null) {
		    	LOG.debug("Use default thumbnail");
		    	cerberusUser.setPhoto(ImageUtil.getDefaultThumbnail(servletContext));
		    }
		    
		    resp = new AuthenticationResponse(token, cerberusUser.getId(), cerberusUser.getShowname(), cerberusUser.getUsername(), cerberusUser.getAuthorities(), cerberusUser.getSetting(), cerberusUser.getPhoto());
		    String companyName = getCompanyName();
		    
		    LOG.debug("Call getAppSetting");
			ApplicationSetting appSetting = getAppSetting();
			
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
		    resp.setProductKey(appSetting.getProductKey());
		    resp.setWebExtractIsEnabled(appSetting.getWebExtractIsEnabled());
		    resp.setWarning(appSetting.getWarning());
		    
		    LOG.debug("End Login");
		    return ResponseEntity.ok(resp);
		} catch (BadCredentialsException e) {
			LOG.error(e.toString());
			throw e;
		} catch (InvalidLicenseException e) {
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
			LOG.debug("Check License");
			checkLicense();
			
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
			if(products == null || products.size() == 0) {
		    	throw new UsernameNotFoundException(String.format("No user found with username '%s'.", user.getUsername()));
		    }
			
			resp = new AuthenticationResponse(token, user.getId(), user.getShowname(), user.getUsername(), user.getAuthorities(), products, user.getSetting(), photo);
			
			String companyName = getCompanyName();
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();		
			List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>)authentication.getAuthorities();
			RolesConstant rolesConstant = RolesConstant.valueOf(authorities.get(0).getAuthority());
			
			if(user.getSetting() != null) {
				ProductSetting setting;
				
		    	if(!StringUtils.isBlank(user.getSetting().getCurrentProduct())) {
		    		setting = getProdSetting(user.getSetting().getCurrentProduct(), products.get(0).getId());
				} else {
		    		setting = getProdSetting(products.get(0).getId(), null);
		    	}
		    	
		    	Integer workingTime = workingTimeCalculation(setting, resp, rolesConstant);
		    	
				boolean isValid = checkWorkingTime(workingTime, resp);
		    	if(!isValid) {
		    		resp.setIsOutOfWorkingTime(true);
		    	}
		    }
			
			LOG.debug("Call getAppSetting");
			ApplicationSetting appSetting = getAppSetting();
			
			if(appSetting.getIsDisable() != null && appSetting.getIsDisable()) {
			    if(rolesConstant != RolesConstant.ROLE_SUPERADMIN) {
			    	throw new BadCredentialsException("System was disabled");
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
		    resp.setProductKey(appSetting.getProductKey());
		    resp.setWebExtractIsEnabled(appSetting.getWebExtractIsEnabled());		    
		    
		    LOG.debug("End refreshToken");
		    return ResponseEntity.ok(resp);
		} catch (BadCredentialsException e) {
			LOG.error(e.toString());
			throw e;
		} catch (InvalidLicenseException e) {
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
				List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>)authentication.getAuthorities();
			    RolesConstant rolesConstant = RolesConstant.valueOf(authorities.get(0).getAuthority());
				Integer workingTime = null;
				
		    	if(!StringUtils.isBlank(user.getSetting().getCurrentProduct())) {
		    		ProductSetting setting = getProdSetting(user.getSetting().getCurrentProduct(), null);
		    		workingTime = workingTimeCalculation(setting, resp, rolesConstant);
		    	}
				
				boolean isValid = checkWorkingTime(workingTime, resp);
		    	if(!isValid) {
		    		resp.setIsOutOfWorkingTime(true);
		    	}
		    }
			
			Calendar now = Calendar.getInstance();
			resp.setServerDateTime(now.getTime());
		    
		    return ResponseEntity.ok(resp);
		} catch (BadCredentialsException e) {
			LOG.error(e.toString());
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
		Field field = query.fields();
		field.include("productName");
		field.include("productSetting.pocModule");
		field.include("productSetting.isHideDashboard");
		field.include("productSetting.isHideAlert");
		field.include("productSetting.privateChatDisabled");
		field.include("productSetting.isSmsEnable");
		
		query.with(new Sort("productName"));
		
	    if(products == null) {
	    	allProds = template.find(query, Product.class);
	    	if(allProds == null || allProds.size() == 0) {	    		
	    		allProds = new ArrayList<>();
	    		Product product = new Product();
	    		product.setProductName("Initial");
	    		allProds.add(product);
	    	}
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
	
	private Integer workingTimeCalculation(ProductSetting setting, AuthenticationResponse resp, RolesConstant rolesConstant) {	    
	    if(rolesConstant != RolesConstant.ROLE_USER && rolesConstant != RolesConstant.ROLE_SUPERVISOR) {
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
			
			if(seconds < 0) seconds = 0;
			
			return seconds;
		}
		
		return null;
	}
	
	private boolean checkWorkingTime(Integer workingTime, AuthenticationResponse resp) {
		if(workingTime != null) {
			resp.setWorkingTime(workingTime);
			
			if(workingTime <= 0) {
				LOG.warn("The time out of working time.");
				return false;
			}
    	}
		return true;
	}
	
	private License checkLicense() {
		try {			
			LicenseManager manager = LicenseManager.getInstance();
			License license = manager.getLicense("");			
			manager.validateLicense(license);
			
			return license;
		} catch (InvalidLicenseException e) {
			throw e;
		} catch (Exception e) {
			throw new InvalidLicenseException("Not really expired have some thing error");
		}
	}
	
}
