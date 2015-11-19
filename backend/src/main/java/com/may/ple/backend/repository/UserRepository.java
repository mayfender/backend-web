package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
	
	Users findByUserNameShow(String userNameShow);
	Users findByUserName(String userName);
	List<Users> findByStatus(int status);

}
