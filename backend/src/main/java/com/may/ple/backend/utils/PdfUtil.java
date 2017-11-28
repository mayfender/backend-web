package com.may.ple.backend.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.github.jhonnymertz.wkhtmltopdf.wrapper.Pdf;
import com.github.jhonnymertz.wkhtmltopdf.wrapper.configurations.WrapperConfig;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

public class PdfUtil {
	private static final Logger LOG = Logger.getLogger(PdfUtil.class.getName());
	
	public static void mergePdf(List<String> pdfFiles, String mergedFilePath) throws Exception {
		Document document = new Document();
		
		try {
			LOG.debug("Amount of pdf file: " + pdfFiles.size());
			PdfCopy copy = new PdfCopy(document, new FileOutputStream(mergedFilePath));
			document.open();
			PdfReader reader;
			int n;
			
			for (String pdfFile : pdfFiles) {
				reader = new PdfReader(pdfFile);
				n = reader.getNumberOfPages();
				
				for (int page = 0; page < n; ) {
	                copy.addPage(copy.getImportedPage(reader, ++page));
	            }
				
	            copy.freeReader(reader);
	            reader.close();
	            FileUtils.deleteQuietly(new File(pdfFile));
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			document.close();
		}
	}
	
	public static void html2pdf(String html) throws Exception {
		try {
			WrapperConfig wrapperConfig = new WrapperConfig("C:\\Program Files\\wkhtmltopdf\\bin\\wkhtmltopdf.exe");
			Pdf pdf = new Pdf(wrapperConfig);
			pdf.addPageFromString(html);
			pdf.saveAs("C:\\Users\\mayfender\\Desktop\\กยศ\\mayfender.pdf");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
