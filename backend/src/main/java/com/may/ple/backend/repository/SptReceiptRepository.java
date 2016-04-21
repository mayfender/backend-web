package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.SptReceipt;

public interface SptReceiptRepository extends JpaRepository<SptReceipt, Long> {
	List<SptReceipt> findByRefIdAndReceiptTypeOrderByReceiptIdDesc(Long refId, Integer receiptType);
	List<SptReceipt> findByReceiptTypeOrderByReceiptIdDesc(Integer receiptType);
}
