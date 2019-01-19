package com.may.ple.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.may.ple.backend.filter.AuthenticationTokenFilter;
import com.may.ple.backend.security.EntryPointUnauthorizedHandler;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled=true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private EntryPointUnauthorizedHandler unauthorizedHandler;
	
	@Autowired
	public void configureAuthentication(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(this.userDetailsService)
		.passwordEncoder(passwordEncoder());
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
		AuthenticationTokenFilter authenticationTokenFilter = new AuthenticationTokenFilter();
		authenticationTokenFilter.setAuthenticationManager(authenticationManagerBean());
		return authenticationTokenFilter;
	}
	
	/*@Bean
    public FilterRegistrationBean corsFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(corsFilterBean());
        registrationBean.addUrlPatterns("/restAct/*", "/login/*");
        return registrationBean;
    }
	
	@Bean
	public CorsFilter corsFilterBean() {
		return new CorsFilter();
	}*/
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();	
	}
	
	@Override
	  protected void configure(HttpSecurity httpSecurity) throws Exception {
	    httpSecurity
	      .csrf()
	        .disable()
	      .exceptionHandling()
	        .authenticationEntryPoint(this.unauthorizedHandler)
	        .and()
	      .sessionManagement()
	        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	        .and()
	      .authorizeRequests()
	      	.antMatchers("/app/js/**", "/app/lib/**", "/app/sounds/**", "/user", "/app/i18n/**",
				     "/app/scripts/**", "/app/styles/**", "/app/images/**", "/app/img/**", "/app/smiley/**",
					 "/app/views/login.html", "/app/index.html", "/favicon**", "/app/manual.html").permitAll()
	        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
	        .antMatchers("/login/**", "/refreshToken/**", "/**/setting/updateLicense**", "/**/setting/contactUs**", "/**/setting/getData").permitAll()
	        .anyRequest().authenticated()
	        .and()
	        .formLogin()
			.loginPage("/app/index.html#/login")
			.permitAll();

	    // Custom JWT based authentication
	    httpSecurity
	      .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
	  }

}
