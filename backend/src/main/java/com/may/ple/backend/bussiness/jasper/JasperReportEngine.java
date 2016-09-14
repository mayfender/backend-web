package com.may.ple.backend.bussiness.jasper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.apache.log4j.Logger;

public class JasperReportEngine {
	private static final Logger LOG = Logger.getLogger(JasperReportEngine.class.getName());
	
	private JasperPrint process(String jasperFile, Map<String, Object> params) throws Exception {
		try {
			
			List<Object> dataList = new ArrayList<>();
			dataList.add(null);
			
			JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperFile, params, beanColDataSource);
			return jasperPrint;
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public byte[] toPdf(String jasperFile, Map<String, Object> params) throws Exception {
		try {
			
			JasperPrint jasperPrint = process(jasperFile, params);
			return JasperExportManager.exportReportToPdf(jasperPrint);		
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	/*public void toPdfTest(String jasperFile, Map<String, Object> params) throws Exception {
		try {
			
			JasperPrint jasperPrint = process(jasperFile, params);
			JasperExportManager.exportReportToPdfFile(jasperPrint, "C:/Users/sarawuti/Desktop/test_jasper/mayfender.pdf");		
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}*/
	
	/*public static void main(String[] args) {
		try {
			
			String jasperFile = "C:/Users/sarawuti/Desktop/test_jasper/mayfender.jasper";
			
			Map<String, Object> params = new HashMap<>();
			params.put("nickName", "You can call me May.");
			
			new JasperReportEngine().toPdfTest(jasperFile, params);
			
			System.out.println("finished");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

}
