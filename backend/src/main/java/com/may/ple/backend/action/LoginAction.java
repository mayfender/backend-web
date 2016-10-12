package com.may.ple.backend.action;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.entity.Product;
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
		    
		    String companyName = getCompanyName();
		    
		    AuthenticationResponse resp = new AuthenticationResponse(token, cerberusUser.getId(), cerberusUser.getShowname(), cerberusUser.getUsername(), cerberusUser.getAuthorities(), products, cerberusUser.getSetting(), cerberusUser.getPhoto());
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
				return ResponseEntity.status(400).build();
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
			
			String companyName = getCompanyName();
			
			AuthenticationResponse resp = new AuthenticationResponse(token, user.getId(), user.getShowname(), user.getUsername(), user.getAuthorities(), products, user.getSetting(), photo);
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
	
}
