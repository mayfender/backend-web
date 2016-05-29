package com.may.ple.backend.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.AuthenticationRequest;
import com.may.ple.backend.model.AuthenticationResponse;
import com.may.ple.backend.repository.ProductRepository;
import com.may.ple.backend.repository.UserRepository;
import com.may.ple.backend.security.CerberusUser;
import com.may.ple.backend.security.TokenUtils;

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
	private ProductRepository productRepository;
	
	@RequestMapping(value="/login", method = RequestMethod.POST)
	public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest, Device device) {
		String token;
		
		try {			
			
		    Authentication authentication = authenticationManager.authenticate(
		    		new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), new String(Base64.decode(authenticationRequest.getPassword().getBytes())))
		    );
		    
		    SecurityContextHolder.getContext().setAuthentication(authentication);
		    
		    UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken)authentication;
		    CerberusUser cerberusUser = (CerberusUser)authToken.getPrincipal();

		    token = tokenUtils.generateToken(cerberusUser, device);		    
		    
		    List<Map<String, String>> products = prePareProduct(cerberusUser.getProducts());
		    
		    return ResponseEntity.ok(new AuthenticationResponse(token, cerberusUser.getShowname(), cerberusUser.getUsername(), cerberusUser.getAuthorities(), products));
		    
		} catch (BadCredentialsException e) {
			LOG.error(e.toString());
			throw e;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@RequestMapping(value="/refreshToken", method = RequestMethod.POST)
	public ResponseEntity<?> refreshToken(@RequestBody AuthenticationRequest authenticationRequest, Device device) {
		try {			
			
			String token = tokenUtils.refreshToken(authenticationRequest.getToken());
			String username = tokenUtils.getUsernameFromToken(token);
			
			Users user = userRepository.findByUsername(username);
			
			List<Map<String, String>> products = prePareProduct(user.getProducts());
			
		    return ResponseEntity.ok(new AuthenticationResponse(token, user.getShowname(), user.getUsername(), user.getAuthorities(), products));
		    
		} catch (BadCredentialsException e) {
			LOG.error(e.toString());
			throw e;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	private List<Map<String, String>> prePareProduct(List<String> products) {
	    
	    if(products == null) return null;
	    List<Map<String, String>> productsResult = new ArrayList<>();
	    Map<String, String> prodMap;
	    
	    for (String prodId : products) {
	    	Product prod = productRepository.findByIdAndEnabled(prodId, 1);				
	    	
	    	if(prod == null) continue;
	    	
	    	prodMap = new HashMap<>();
	    	prodMap.put("id", prod.getId());
	    	prodMap.put("productName", prod.getProductName());
	    	productsResult.add(prodMap);
		}
	    
	    return productsResult;
	}

}
