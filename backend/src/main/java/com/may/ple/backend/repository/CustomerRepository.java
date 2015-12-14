package com.may.ple.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	
}
