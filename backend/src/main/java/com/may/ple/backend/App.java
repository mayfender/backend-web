package com.may.ple.backend;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

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
	}

	@Configuration
    public static class WebappConfig extends WebMvcConfigurerAdapter {
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        	LOG.info("@RestController Set Mongo's ObjectId converter.");
            Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
            builder.serializerByType(ObjectId.class, new ToStringSerializer());
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(builder.build());
            converters.add(converter);
        }
    }

}
