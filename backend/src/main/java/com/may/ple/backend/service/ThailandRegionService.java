package com.may.ple.backend.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.CaseFormat;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.FileUtil;

@Service
public class ThailandRegionService {
	private static final Logger LOG = Logger.getLogger(ThailandRegionService.class.getName());
	private MongoTemplate templateCenter;
	
	@Autowired	
	public ThailandRegionService(MongoTemplate templateCenter) {
		this.templateCenter = templateCenter;
	}
	
	public void upload(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {
		BufferedReader reader = null;
		
		try {
			LOG.debug("Start");
			
			Date date = Calendar.getInstance().getTime();
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			if(!fd.fileExt.equals(".sql")) {
				throw new Exception("File type is wrong.");
			}
				
			reader = new BufferedReader(new InputStreamReader(uploadedInputStream));
			List<List<String>> collection = new ArrayList<>();
			boolean isAmphures = false;
			List<String> vals, fields;
			int start = 0, end = 0;
			String line;
			
			while ((line = reader.readLine()) != null) {
				if(line.contains("INSERT INTO `amphures`")) {
					isAmphures = true;
					start = line.indexOf("(") + 1;
					end = line.indexOf(")");
					
					line = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, line.substring(start, end).replace("`", ""));
					
					fields = Arrays.asList(line.split(","));
				}
				
				if(isAmphures) {
					start = line.indexOf("(") + 1;
					end = line.indexOf(")");
					
					if(line.contains(");")) break;
					
					vals = Arrays.asList(line.split(","));
					collection.add(vals);
				}
			}
			
			
//			templateCenter.insert(batchToSave, Amphures.class);
			
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(reader != null) reader.close();
		}
	}
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		List<String> fixedSizeList = Arrays.asList("1, '1001', 'เขตพระนคร   ', 'Khet Phra Nakhon', 2, 1".split(","));
		System.out.println();
	}
			
}
