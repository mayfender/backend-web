package com.may.ple.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.OrderMenu;

public interface OrderRepository extends JpaRepository<OrderMenu, Long> {
	
}
