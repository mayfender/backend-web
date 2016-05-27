package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.may.ple.backend.entity.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
	List<Product> findByEnabled(Boolean isEnabled);
}
