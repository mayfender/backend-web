package com.may.ple.backend.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;

@Service
public class ToolsService {
	private static final Logger LOG = Logger.getLogger(ToolsService.class.getName());
	
	public ByteArrayOutputStream excel2txt(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd) throws Exception {
		OutputStreamWriter writer = null;
		Workbook workbook = null;
		
		try {
			LOG.debug("File ext: " + fd.fileExt);						
			
			if(fd.fileExt.equals(".xlsx")) {
				workbook = new XSSFWorkbook(uploadedInputStream);
			} else if(fd.fileExt.equals(".xls")) {
				workbook = new HSSFWorkbook(uploadedInputStream);
			} else {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			Sheet sheet = workbook.getSheetAt(0);
			List<ColumnFormat> columnFormats = new ArrayList<>();
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormats);
			if(headerIndex.size() == 0) {		
				LOG.error("Not found Headers");
				return null;
			}
			
			ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(outputArray, "UTF-8");
			StringBuilder txtRaw = new StringBuilder();
			Set<String> keySet = headerIndex.keySet();
			Cell cell;
			int r = 1;
			Row row;
			
			while(true) {
				if(r > 1) {
					if(txtRaw.length() != 0) {
						txtRaw.deleteCharAt(txtRaw.length() - 1);
						txtRaw.append(System.getProperty("line.separator"));
					}
				}
				
				row = sheet.getRow(r++);
				if(row == null) {
					r--;
					break;
				}
				
				for (String key : keySet) {
					cell = row.getCell(headerIndex.get(key), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					
					if(cell == null || StringUtils.isBlank(String.valueOf(cell))) {
						txtRaw.append("|");
					} else {						
						txtRaw.append(new DataFormatter(Locale.ENGLISH).formatCellValue(cell) + "|");
					}
				}			
			}
			
			writer.write(txtRaw.toString());
			
			return outputArray;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
			if(writer != null) writer.close();
		}
	}
	
	public ByteArrayOutputStream pdf2img(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd) throws Exception {
		OutputStreamWriter writer = null;
		Workbook workbook = null;
		PDDocument document = null;
		
		try {
			LOG.debug("File ext: " + fd.fileExt);						
			
			if(!fd.fileExt.equals(".pdf")) {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
			document = PDDocument.load(uploadedInputStream);
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			BufferedImage bim;
			int widthImg = 0;
			int heightImg = 0;
			float dpi = 128;
			
			for (int page = 0; page < document.getNumberOfPages(); ++page) { 				
			    bim = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
			    
			    widthImg = Math.max(widthImg, bim.getWidth());
			    heightImg += bim.getHeight();			    
			}
			
			BufferedImage combined = new BufferedImage(
					widthImg, // Final image will have width and height as
					heightImg, // addition of widths and heights of the images we already have
					BufferedImage.TYPE_INT_RGB);
			
			Graphics2D gbi = combined.createGraphics();
			heightImg = 0;
			
			for (int page = 0; page < document.getNumberOfPages(); ++page) { 				
			    bim = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
			    gbi.drawImage(bim, 0, heightImg, null);
			    heightImg += bim.getHeight();
			}
			
			ImageIO.write(combined, "jpg", outputArray);
			
			FileOutputStream out = new FileOutputStream("D:\\test.jpg");
			out.write(outputArray.toByteArray());
			out.close();
			
			return outputArray;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(document != null) document.close();
			if(workbook != null) workbook.close();
			if(writer != null) writer.close();
		}
	}
	
}
