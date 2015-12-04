package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	
}
