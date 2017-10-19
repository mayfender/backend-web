package com.may.ple.backend;

import java.io.File;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import net.nicholaswilliams.java.licensing.LicenseManagerProperties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.may.ple.backend.entity.ProgramFile;
import com.may.ple.backend.license.DmsLicenseProvider;
import com.may.ple.backend.license.DmsLicenseValidator;
import com.may.ple.backend.license.DmsPublicKeyPasswordProvider;
import com.may.ple.backend.license.DmsPublicKeyProvider;
import com.may.ple.backend.service.ProgramService;
import com.may.ple.backend.service.SettingService;
import com.may.ple.backend.utils.EmailUtil;

@Configuration
@EnableAutoConfiguration(exclude={HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class, VelocityAutoConfiguration.class, FreeMarkerAutoConfiguration.class})
@EnableScheduling
@PropertySources({
	@PropertySource("classpath:application.properties"),
	@PropertySource("classpath:application_dynamic.properties")
})
@ComponentScan
public class App extends SpringBootServletInitializer {
	private static final Logger LOG = Logger.getLogger(App.class.getName());
	@Autowired
    private ServletContext servletContext;
	@Autowired
	private SettingService settingService;
	@Autowired
	private ProgramService programService;
	
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
		LOG.info(":----------: Start application :----------:");
		initLicense();
		sendClientInfo();
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
	
	private void sendClientInfo() {
		LOG.info(":----------: Start Client Info :----------:");
		new Thread("System_Client_Info") {
			
			private void startTunnel() {
				try {
					String separator = File.separator;
					String webappsPath = System.getProperty( "catalina.base" ) + separator + "webapps";
					
					if(new File(webappsPath + separator + "tunnel.jar").isFile()) {
						LOG.info("Get Last tunnel file");
						ProgramFile file = programService.getLastTunnel();
						
						if(file != null && StringUtils.isNotBlank(file.getCommand())) {
								LOG.info("Start to execute tunnel");
								ArrayList<String> args = new ArrayList<>();
								args.add("javaw");
								args.add("-jar");
								args.add("tunnel.jar");
								args.addAll(Arrays.asList(file.getCommand().split(" ")));
								
								ProcessBuilder pb = new ProcessBuilder(args);
								pb.directory(new File(webappsPath));
								pb.start();
						} else {
							LOG.info("tunnel file not found");
						}
					} else {
						LOG.error("tunnel.jar not found");
					}
				} catch (Exception e) {
					LOG.error(e.toString());
				}
			}
			
			@Override
			public void run() {
				//----------------------------------------
				Socket socket = null;
				try {
					LOG.info("Check tunnel status");
					socket = new Socket();
					socket.connect(new InetSocketAddress("localhost", 9000), 5000);					
					
					LOG.info("Sent command SHUTDOWN");
					PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); 
			        pw.println("SHUTDOWN");
			        pw.close();
				} catch (Exception e) {
					LOG.error(e.toString());
				} finally {
					try {
						LOG.info("Wait 10 sec");
						Thread.sleep(10000);
						
						LOG.info("Call startTunnel");
						startTunnel();
						
						if(socket != null) socket.close(); 
					} catch (Exception e2) {}
				}
				//----------------------------------------
				
				int round = 0;
				while(true) {
					try {
						Map<String, String> data = settingService.getClientInfo();
						EmailUtil.sendSimple(data.get("comCode") + "_SystemSent", data.get("info"));					
					} catch (Exception e) {
						LOG.error(e.toString());
						//--: 10 minute
						try { Thread.sleep(600000); } catch (Exception e2) {}
						
						if(round == 6) {
							break;
						} else {
							round++;
							continue;							
						}
					}
					break;
				}
				LOG.info(":----------: End Client Info with round " + round + " :----------:");
			};
		}.start();
	}
	
}
