package com.may.ple.backend.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.SimpleDateFormat;
import com.may.ple.backend.constant.ConvertTypeConstant;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;

@Service
public class ToolsService {
	private static final Logger LOG = Logger.getLogger(ToolsService.class.getName());
	@Value("${file.path.temp}")
	private String filePathTemp;
	
	public void excel2txt(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd, ConvertTypeConstant type) throws Exception {
		ByteArrayOutputStream outputArray = null;
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
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			List<ColumnFormat> columnFormats = new ArrayList<>();
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormats);
			if(headerIndex.size() == 0) {		
				LOG.error("Not found Headers");
				return;
			}
			
			outputArray = new ByteArrayOutputStream();
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
						if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {	
							if(HSSFDateUtil.isCellDateFormatted(cell)) {
								LOG.debug("Date format: " + cell.getCellStyle().getDataFormatString() + ", " + cell.getCellStyle().getDataFormat());
								
								if(cell.getCellStyle().getDataFormat() == 14) {
									txtRaw.append(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(cell.getDateCellValue()) + "|");																
								} else {									
									txtRaw.append(new DataFormatter(Locale.ENGLISH).formatCellValue(cell) + "|");							
								}
							} else {
								txtRaw.append(NumberToTextConverter.toText(cell.getNumericCellValue()) + "|");							
							}
						} else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){
							txtRaw.append(new DataFormatter(Locale.ENGLISH).formatCellValue(cell, formulaEvaluator) + "|");	
						} else {
							txtRaw.append(new DataFormatter(Locale.ENGLISH).formatCellValue(cell) + "|");							
						}
					}
				}			
			}
			
			writer.write(txtRaw.toString());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
			if(writer != null) writer.close();
		}
		
		LOG.debug("Call saveToFile");
		saveToFile(filePathTemp, fd.fileName, type.getExt(), outputArray);
	}
	
	public void pdf2img(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd, ConvertTypeConstant type) throws Exception {
		ByteArrayOutputStream outputArray = null;
		OutputStreamWriter writer = null;
		PDDocument document = null;
		
		try {
			LOG.debug("File ext: " + fd.fileExt);						
			
			if(!fd.fileExt.equals(".pdf")) {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			outputArray = new ByteArrayOutputStream();
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
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(document != null) document.close();
			if(writer != null) writer.close();
		}
		
		LOG.debug("Call saveToFile");
		saveToFile(filePathTemp, fd.fileName, type.getExt(), outputArray);
	}
	
	public byte[] getFile(String fileName) throws Exception {
		try {
			String filePath = filePathTemp + "/" + fileName;
			Path path = Paths.get(filePath);
			byte[] data = Files.readAllBytes(path);
			
			if(!new File(filePath).delete()) {
				LOG.warn("Cann't delete file " + filePath);
			}
			
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void saveToFile(String path, String fileNameFull, String ext, ByteArrayOutputStream outputArray) throws Exception {
		FileOutputStream fileOut = null;
		
		try {
			LOG.debug("Start save file");
			
			File file = new File(path);
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			fileOut = new FileOutputStream(path + "/" + fileNameFull + "." + ext);
			fileOut.write(outputArray.toByteArray());
		
			LOG.debug("Finished save file");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(fileOut != null) fileOut.close();
		}
	}
	
}
