package com.may.ple.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.may.ple.backend.entity.Users;

public interface UserRepository extends MongoRepository<Users, String> {

	Users findByUsernameAndEnabled(String username, Boolean enabled);
	Users findByEnabled(Boolean enabled);
	Users findByUsername(String username);
	Users findByShowname(String showname);
	Users findByLineUserId(String lineUserId);
	Users findByShownameAndDealerIdIsNull(String showname);
	Users findByShownameAndDealerId(String showname, String dealerId);

}
