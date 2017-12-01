package com.may.ple.backend.model;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.Users;

public class PaymentOnlineUpdateModel2 {
	public MongoTemplate template;
	public Product product;
	public List<ColumnFormat> headers;
	public List<Users> users;
}
