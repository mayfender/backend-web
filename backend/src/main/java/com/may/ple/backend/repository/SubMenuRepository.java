package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.SubMenu;

public interface SubMenuRepository extends JpaRepository<SubMenu, Long> {
	
	List<SubMenu> findByMenuId(Long id);
	
}
