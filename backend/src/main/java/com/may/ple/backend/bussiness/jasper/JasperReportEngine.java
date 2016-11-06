package com.may.ple.backend.bussiness.jasper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class JasperReportEngine {
	private static final Logger LOG = Logger.getLogger(JasperReportEngine.class.getName());
	
	private JasperPrint process(String jasperFile, List<Map<String, Object>> params) throws Exception {
		try {
			
			if(params == null || params.size() == 0) throw new Exception("params is empty");
			
			List<Object> dataList = null;
			
			for (Map<String, Object> map : params) {
				dataList = new ArrayList<>();				
				dataList.add(map);
			}
			
			JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperFile, null, beanColDataSource);
			return jasperPrint;
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public byte[] toPdf(String jasperFile, List<Map<String, Object>> params) throws Exception {
		try {
			
			JasperPrint jasperPrint = process(jasperFile, params);
			return JasperExportManager.exportReportToPdf(jasperPrint);		
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	/*public void toPdfTest(String jasperFile, List<Map<String, Object>> params) throws Exception {
		try {
			
			JasperPrint jasperPrint = process(jasperFile, params);
			JasperExportManager.exportReportToPdfFile(jasperPrint, "C:/Users/mayfender/Desktop/report design/notice/mayfender.pdf");		
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public static void main(String[] args) {
		try {
			
			String jasperFile = "C:/Users/mayfender/Desktop/report design/notice/template.jasper";
			
			Map<String, Object> params = new HashMap<>();
			params.put("may", "You can call me May.");
			
			List<Map<String, Object>> obj = new ArrayList<>();
			obj.add(params);
			
			new JasperReportEngine().toPdfTest(jasperFile, obj);
			
			System.out.println("finished");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

}
