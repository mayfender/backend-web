package com.may.ple.backend.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jopendocument.dom.ODSingleXMLDocument;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;

public class XDocUtil {
	private static final Logger LOG = Logger.getLogger(XDocUtil.class.getName());
	
	public static byte[] generateToPdf(String templatePath, Map param) throws Exception {
		FileInputStream in = null;
		
		try {
			// 1) Initial engine with Velocity
			in = new FileInputStream(new File(templatePath));
			XDocReportRegistry registry = XDocReportRegistry.getRegistry();
			IXDocReport report = registry.loadReport(in, TemplateEngineKind.Velocity);
			
			// 2) Create context Java model
			IContext context = report.createContext();
			defaultParams(context);
			context.put("params", param);
			
			// 3) Generate report by merging Java model with the ODT
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			report.process(context, out);
			InputStream raw = new ByteArrayInputStream(out.toByteArray());
			
			byte[] data = JodConverterUtil.toPdf(raw, FilenameUtils.getExtension(templatePath));
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(in != null) in.close(); } catch (Exception e2) {}
		}
	}
	
	public static byte[] generate(String templatePath, Map param) throws Exception {
		FileInputStream in = null;
		
		try {
			// 1) Initial engine with Velocity
			in = new FileInputStream(new File(templatePath));
			XDocReportRegistry registry = XDocReportRegistry.getRegistry();
			IXDocReport report = registry.loadReport(in, TemplateEngineKind.Velocity);
			
			// 2) Create context Java model
			IContext context = report.createContext();
			defaultParams(context);
			context.put("params", param);
			
			// 3) Generate report by merging Java model with the ODT
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			report.process(context, out);
			
			byte[] data = out.toByteArray();
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(in != null) in.close(); } catch (Exception e2) {}
		}
	}
	
	public static void mergeAndRemove(List<String> files, String destination) throws Exception {
		try {
			if(files == null) return;
			ODSingleXMLDocument main = null;
			String file;
			
			for (int i = 0; i < files.size(); i++) {
				file = files.get(i);
				
				if(i == 0) {
					//-- Main file
					main = ODSingleXMLDocument.createFromPackage(new File(file));
					FileUtils.deleteQuietly(new File(file));
					continue;
				}
				
				//-- Concatenate them
				main.add(ODSingleXMLDocument.createFromPackage(new File(file)));
				FileUtils.deleteQuietly(new File(file));
			}

			//-- Save to file and Open the document with OpenOffice.org !
			main.saveToPackageAs(new File(destination));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private static void defaultParams(IContext context) {
		context.put("string", "");
		context.put("Number2WordUtil", Number2WordUtil.class);
		context.put("Locale", Locale.class);
	}

}
