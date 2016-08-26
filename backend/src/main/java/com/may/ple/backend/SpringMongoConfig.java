package com.may.ple.backend;

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
import org.springframework.data.authentication.UserCredentials;
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

import com.may.ple.backend.entity.Database;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.model.DbFactory;
import com.mongodb.MongoClient;

@Configuration
public class SpringMongoConfig {
	private static final Logger LOG = Logger.getLogger(SpringMongoConfig.class.getName());
	@Autowired
	MongoDbFactory factory;
	
	@Bean
	public DbFactory dbFactory() throws Exception {
		
		MongoTemplate templateCore = new MongoTemplate(factory);
		List<Product> products = templateCore.find(Query.query(Criteria.where("enabled").is(1)), Product.class);
		Map<String, MongoTemplate> dbClients = new HashMap<>();
		DbFactory dbFactory = new DbFactory();
		SimpleMongoDbFactory krungsi;
		MongoTemplate template;
		Database db;
		
		for (Product prod : products) {
			db = prod.getDatabase();
			
			if(db == null || StringUtils.isBlank(db.getHost())) continue;
			
			UserCredentials credential = null;
			
			if(prod.getDatabase() != null) {
				credential = new UserCredentials(prod.getDatabase().getUserName(), prod.getDatabase().getPassword());
			}
			
			krungsi = new SimpleMongoDbFactory(new MongoClient(db.getHost(), db.getPort()), db.getDbName(), credential);
			template = new MongoTemplate(krungsi, mappingMongoConverter(null, null, null));
			dbClients.put(prod.getId(), template);		
			LOG.debug(prod);
		}
		
		dbFactory.setTemplates(dbClients);
		
		return dbFactory;
	}

	@Bean
	public MappingMongoConverter mappingMongoConverter(MongoDbFactory factory, MongoMappingContext context, BeanFactory beanFactory) {
		
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
