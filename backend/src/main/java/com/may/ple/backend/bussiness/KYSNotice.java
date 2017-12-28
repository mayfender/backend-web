package com.may.ple.backend.bussiness;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.common.io.Files;
import com.may.ple.backend.utils.PdfUtil;
import com.may.ple.backend.utils.XDocUtil;

public class KYSNotice implements Runnable {
	private static final Logger LOG = Logger.getLogger(KYSNotice.class.getName());
	private Map taskDetail;
	private String noticeTemplate;
	private String pdfFile;
	
	public KYSNotice(Map taskDetail, String noticeTemplate, String pdfFile) {
		this.taskDetail = taskDetail;
		this.noticeTemplate = noticeTemplate;
		this.pdfFile = pdfFile;
	}
	
	@Override
	public void run() {
		try {
			byte[] data = XDocUtil.generateToPdf(this.noticeTemplate, this.taskDetail);
			String newPdf = pdfFile + ".new";
			FileUtils.writeByteArrayToFile(new File(newPdf), data);
			
			LOG.debug("Merge pdf");
			List<String> pdfFiles = new ArrayList<>();
			pdfFiles.add(pdfFile);
			pdfFiles.add(newPdf);
			PdfUtil.mergePdf(pdfFiles, pdfFile + ".merged");
			Files.move(new File(pdfFile + ".merged"), new File(pdfFile));
		} catch (Exception e) {
			LOG.error(e.toString());
		}
	}

}
