package com.may.ple.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.may.ple.backend.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	@Query(nativeQuery = true, value = "select * from customer where status = ?1 and table_detail = ?2 and ref = ?3 and created_date_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) ")
	Customer findByStatusAndTableDetailAndRef(Integer status, String tableDetail, String ref);
}
