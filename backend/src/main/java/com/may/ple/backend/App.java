package com.may.ple.backend;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import net.nicholaswilliams.java.licensing.LicenseManagerProperties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.may.ple.backend.license.DmsLicenseProvider;
import com.may.ple.backend.license.DmsLicenseValidator;
import com.may.ple.backend.license.DmsPublicKeyPasswordProvider;
import com.may.ple.backend.license.DmsPublicKeyProvider;
import com.may.ple.backend.service.SettingService;

@Configuration
@EnableAutoConfiguration(exclude={HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class, VelocityAutoConfiguration.class})
@EnableScheduling
@ComponentScan
public class App extends SpringBootServletInitializer {
	private static final Logger LOG = Logger.getLogger(App.class.getName());
	@Autowired
    private ServletContext servletContext;
	@Autowired
	private SettingService settingService;
	
	// Entry point for application
	public static void main(String[] args) {
		LOG.info(":---------: Start by main method :----------:");
		SpringApplication.run(App.class, args);
	}

	// Entry point Servlet Engine
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		LOG.info(":----------: Start by SpringBootServletInitializer :----------:");
		return builder.sources(App.class);
	}

	@PostConstruct
	public void init() {
		LOG.info(":----------: Start Ricoh application :----------:");
		initLicense();
	}
	
	private void initLicense() {
		try {
			LOG.info(":----------: Init License Validator :----------:");			
			LicenseManagerProperties.setPublicKeyDataProvider(new DmsPublicKeyProvider(servletContext));
			LicenseManagerProperties.setPublicKeyPasswordProvider(new DmsPublicKeyPasswordProvider());
			LicenseManagerProperties.setLicenseProvider(new DmsLicenseProvider(settingService));
			LicenseManagerProperties.setLicenseValidator(new DmsLicenseValidator(settingService));
			LOG.info(":----------: Finished init License Validator :----------:");		
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
	}
	
}
