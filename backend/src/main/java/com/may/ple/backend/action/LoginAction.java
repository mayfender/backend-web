package com.may.ple.backend.action;

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

import com.may.ple.backend.model.AuthenticationRequest;
import com.may.ple.backend.model.AuthenticationResponse;
import com.may.ple.backend.security.CerberusUser;
import com.may.ple.backend.security.TokenUtils;

@RestController
public class LoginAction {
	private static final Logger LOG = Logger.getLogger(LoginAction.class.getName());
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private TokenUtils tokenUtils;
	
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
		    
		    return ResponseEntity.ok(new AuthenticationResponse(token, cerberusUser.getShowname(), cerberusUser.getUsername(), cerberusUser.getAuthorities()));
		    
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
			
		    return ResponseEntity.ok(new AuthenticationResponse(token, null, null, null));
		    
		} catch (BadCredentialsException e) {
			LOG.error(e.toString());
			throw e;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}

}
