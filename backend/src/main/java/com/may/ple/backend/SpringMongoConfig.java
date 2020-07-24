package com.may.ple.backend;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.may.ple.backend.entity.Dealer;
import com.may.ple.backend.model.DbFactory;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@Configuration
public class SpringMongoConfig {
	private static final Logger LOG = Logger.getLogger(SpringMongoConfig.class.getName());
	@Autowired
	MongoDbFactory factory;

	@Bean
	public DbFactory dbFactory() throws Exception {

		MongoTemplate templateCore = new MongoTemplate(factory);
		List<Dealer> dealers = templateCore.find(Query.query(Criteria.where("enabled").is(true)), Dealer.class);
		Map<String, MongoTemplate> dbClients = new HashMap<>();
		DbFactory dbFactory = new DbFactory();
		SimpleMongoDbFactory factory;
		ServerAddress serverAddress;
		MongoCredential credential;
		MongoClient mongoClient;
		MongoTemplate template;

		for (Dealer dealer : dealers) {
			if(StringUtils.isBlank(dealer.getHost())) continue;

			credential = MongoCredential.createCredential(dealer.getUsername(), dealer.getDbname(), dealer.getPassword().toCharArray());
			serverAddress = new ServerAddress(dealer.getHost(), dealer.getPort());
			mongoClient = new MongoClient(serverAddress, Arrays.asList(credential));
			factory = new SimpleMongoDbFactory(mongoClient, dealer.getDbname());

			template = new MongoTemplate(factory, mappingMongoConverter(null, null, null));
			dbClients.put(dealer.getId(), template);
			LOG.debug(dealer);
		}

		dbFactory.setTemplates(dbClients);

		return dbFactory;
	}

	@Bean
	public MappingMongoConverter mappingMongoConverter(MongoDbFactory factory, MongoMappingContext context, BeanFactory beanFactory) {
		LOG.debug("mappingMongoConverter");

		DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
		MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);

		try {
			mappingConverter.setCustomConversions(beanFactory.getBean(CustomConversions.class));
		} catch (NoSuchBeanDefinitionException ignore) {

		}

		// Don't save _class to mongo
		mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));

		return mappingConverter;
	}

}