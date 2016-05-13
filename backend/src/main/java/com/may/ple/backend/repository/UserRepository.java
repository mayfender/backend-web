package com.may.ple.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.may.ple.backend.entity.Users;

public interface UserRepository extends MongoRepository<Users, String> {
	
	Users findByUsernameAndEnabled(String username, Boolean enabled);
	Users findByEnabled(Boolean enabled);
	
}
