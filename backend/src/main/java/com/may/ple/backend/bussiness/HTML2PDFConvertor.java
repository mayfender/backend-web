package com.may.ple.backend.bussiness;

import java.util.Map;

import org.apache.log4j.Logger;

import com.may.ple.backend.utils.PdfUtil;

public class HTML2PDFConvertor implements Runnable {
	private static final Logger LOG = Logger.getLogger(HTML2PDFConvertor.class.getName());
	private Map payment;
	private String dir;
	private String wkhtmltopdfPath;
	
	public HTML2PDFConvertor(Map payment, String dir, String wkhtmltopdfPath) {
		this.payment = payment;
		this.dir = dir;
		this.wkhtmltopdfPath = wkhtmltopdfPath;
	}
	
	@Override
	public void run() {
		try {
			if(payment.get("html") == null) {
				LOG.error(payment.get("ID_CARD") + " html not found");
				return;
			}
			
			String pdfFile = dir + "/" + payment.get("ลำดับ").toString().replaceAll("[\\\\/:*?\"<>|]", "_") + "_" + payment.get("ID_CARD") + ".pdf";
						
			PdfUtil.html2pdf(wkhtmltopdfPath,  payment.get("html").toString(), pdfFile);
		} catch (Exception e) {
			LOG.error(e.toString());
		}
	}

}
