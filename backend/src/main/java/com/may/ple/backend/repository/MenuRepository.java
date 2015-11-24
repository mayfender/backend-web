package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.Users;

public interface MenuRepository extends JpaRepository<Menu, Long> {
	
	Users findByName(String userName);
	List<Users> findByStatus(int status);

}
