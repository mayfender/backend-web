package com.may.ple.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.Users;
import com.may.ple.backend.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users user = userRepository.findByUsernameAndEnabled(username, true);

		if (user == null) {
			throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
		} else {
			return CerberusUserFactory.create(user);
		}
	}

}
