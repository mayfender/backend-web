package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.OrderMenu;

public interface OrderRepository extends JpaRepository<OrderMenu, Long> {
	List<OrderMenu> findByCusIdOrderByOrderRoundCreatedDateTimeAsc(Long id);
}
