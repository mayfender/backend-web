package com.may.ple.backend.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import com.may.ple.backend.entity.Users;
import com.may.ple.backend.security.CerberusUserFactory;
import com.may.ple.backend.security.TokenUtils;

public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {

	@Value("${cerberus.token.header}")
	private String tokenHeader;

	@Autowired
	private TokenUtils tokenUtils;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String authToken = httpRequest.getHeader(this.tokenHeader);
		String username = this.tokenUtils.getUsernameFromToken(authToken);

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			ArrayList<LinkedHashMap<String, String>> authorities = this.tokenUtils.getAuthoritiesFromToken(authToken);
			List<SimpleGrantedAuthority> auths = new ArrayList<>();
			
			for (Object obj : authorities) {
				LinkedHashMap<String, String> auth = (LinkedHashMap<String, String>)obj;
				auths.add(new SimpleGrantedAuthority(auth.get("authority")));
			}
			
			Users user = new Users(null, username, null, null, null, null, auths);
			UserDetails userDetails = CerberusUserFactory.create(user);
			
			if (this.tokenUtils.validateToken(authToken, userDetails)) {
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}

		chain.doFilter(request, response);
	}

}
