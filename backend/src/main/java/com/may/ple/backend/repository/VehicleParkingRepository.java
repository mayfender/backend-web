package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.VehicleParking;

public interface VehicleParkingRepository extends JpaRepository<VehicleParking, Long> {
	
	List<VehicleParking> findVehicleParking(Integer status);
	
}
