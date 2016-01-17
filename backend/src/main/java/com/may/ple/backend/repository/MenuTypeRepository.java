package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.MenuType;

public interface MenuTypeRepository extends JpaRepository<MenuType, Long> {
	List<MenuType> findByParentId(Long parentId);
	List<MenuType> findByParentIdAndIsEnabled(Long parentId, Boolean flag);
}
