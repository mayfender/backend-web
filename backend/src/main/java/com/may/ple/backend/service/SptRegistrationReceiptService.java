package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.constant.ExportTypeConstant;
import com.may.ple.backend.entity.SptReceipt;
import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.pdf.ReceiptRegistration;
import com.may.ple.backend.repository.SptReceiptRepository;
import com.may.ple.backend.repository.SptRegistrationRepository;

@Service
public class SptRegistrationReceiptService {
	private static final Logger LOG = Logger.getLogger(SptRegistrationReceiptService.class.getName());
	private SptRegistrationRepository sptRegistrationRepository;
	private SptReceiptRepository sptReceiptRepository;
	
	@Autowired
	public SptRegistrationReceiptService(SptRegistrationRepository sptRegistrationRepository,
										 SptReceiptRepository sptReceiptRepository) {
		this.sptRegistrationRepository = sptRegistrationRepository;
		this.sptReceiptRepository = sptReceiptRepository;
	}
	
	public byte[] proceed(Long id) throws Exception {
		try {
			LOG.debug("Get registration");
			SptRegistration registration = sptRegistrationRepository.findOne(id);
			
			LOG.debug("Get receipt");
			List<SptReceipt> receipts = sptReceiptRepository.findByRefIdAndReceiptTypeOrderByReceiptIdDesc(registration.getRegId(), ExportTypeConstant.RECEIPT.getId());
			
			String receiptNo = "";
			
			if(receipts.size() == 0) {
				Date date = new Date();
				
				receiptNo = genReceiptNo();
				
				LOG.debug("Save");
				SptReceipt sptReceipt = new SptReceipt(receiptNo, ExportTypeConstant.RECEIPT.getId(), date, date, registration.getRegId());
				sptReceiptRepository.save(sptReceipt);
			} else {
				LOG.debug("Get receiptNo from last");
				receiptNo = receipts.get(0).getReceiptNo();
			}
			
			byte[] data = new ReceiptRegistration(registration, receiptNo).createPdf();			
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String genReceiptNo() {
		try {
			String receiptNo = "SPTP-R";
			
			LOG.debug("Get receiptByType");
			List<SptReceipt> findByReceiptTypes = sptReceiptRepository.findByReceiptTypeOrderByReceiptIdDesc(ExportTypeConstant.RECEIPT.getId());
					
			if(findByReceiptTypes.size() == 0) {
				receiptNo += String.format("%07d", 1);
			} else {
				String nowReceiptNo = findByReceiptTypes.get(0).getReceiptNo();
				int runNumber = Integer.parseInt(nowReceiptNo.substring(9)) + 1;
				receiptNo += String.format("%07d", runNumber);					
			}
			return receiptNo;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
