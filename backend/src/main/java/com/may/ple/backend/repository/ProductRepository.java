package com.may.ple.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.may.ple.backend.entity.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
	Product findByIdAndEnabled(String id, Integer enabled);
}
