package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.entity.Users;

public interface MenuRepository extends JpaRepository<Menu, Long> {
	
	Users findByName(String userName);
	List<Menu> findByStatus(int status);
	List<Menu> findByMenuType(MenuType type);
	@Query(value="select m from Menu m where m.menuType = ? and m.status != 2 ")
	List<Menu> findByMenuTypeNotDeleted(MenuType type);

}
