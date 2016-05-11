package com.may.ple.backend;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.may.ple.backend.custom.UserDetailCustom;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.repository.UserRepository;

@Service
public class MongoDBAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
	private static final Logger LOG = Logger.getLogger(MongoDBAuthenticationProvider.class.getName());
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    	UserDetailCustom loadedUser;

        try {
        	Users user = userRepository.findByUsernameAndIsactive(username, true);
        	String rawPassword = authentication.getCredentials().toString();
        	
        	if(user == null) 
        		throw new InternalAuthenticationServiceException("Not found the user");
        	
        	if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
        		throw new BadCredentialsException("Wrong password");
        	}
        	
        	loadedUser = new UserDetailCustom(new User(user.getUsername(), user.getPassword(), user.getRoles()));
        	loadedUser.setShowname(user.getShowname());
        } catch (Exception e) {
        	LOG.error(e.toString());
            throw new InternalAuthenticationServiceException(e.getMessage(), e);
        }

        return loadedUser;
    }

}
