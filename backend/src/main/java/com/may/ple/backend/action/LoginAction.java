package com.may.ple.backend.action;

import java.util.Date;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.may.ple.backend.entity.ApplicationSetting;
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
			
			resp = new AuthenticationResponse(token, user.getId(), user.getShowname(), user.getUsername(), user.getAuthorities(), user.getSetting(), photo);
			
			String companyName = getCompanyName();
			
			LOG.debug("Call getAppSetting");
			ApplicationSetting appSetting = getAppSetting();
			
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
	
	private String getCompanyName() {
		ApplicationSetting find = template.findOne(new Query(), ApplicationSetting.class);
		return find == null ? null : find.getCompanyName();
	}
	
	private ApplicationSetting getAppSetting() {
		return template.findOne(new Query(), ApplicationSetting.class);
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
