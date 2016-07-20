package com.may.ple.backend.criteria;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class NoticeDownloadCriteriaResp extends CommonCriteriaResp implements StreamingOutput {
	private static final Logger LOG = Logger.getLogger(NoticeDownloadCriteriaResp.class.getName());
	private String filePath;
	private boolean isFillTemplate;
	
	private HWPFDocument replaceTextDoc(HWPFDocument doc, String findText, String replaceText) {
		Range r = doc.getRange();
		for (int i = 0; i < r.numSections(); ++i) {
			Section s = r.getSection(i);
			for (int j = 0; j < s.numParagraphs(); j++) {
				Paragraph p = s.getParagraph(j);
				for (int k = 0; k < p.numCharacterRuns(); k++) {
					CharacterRun run = p.getCharacterRun(k);
					String text = run.text();
					if (text.contains(findText)) {
						run.replaceText(findText, replaceText);
					}
				}
			}
		}
		return doc;
	}
	
	private XWPFDocument replaceTextDocx(XWPFDocument doc, String findText, String replaceText) {
		try {
			for (XWPFParagraph p : doc.getParagraphs()) {
			    List<XWPFRun> runs = p.getRuns();
			    
			    if (runs != null) {
			        for (XWPFRun r : runs) {
			            String text = r.getText(0);
			            if (text != null && text.contains(findText)) {
			                text = text.replace(findText, replaceText);
			                r.setText(text, 0);
			            }
			        }
			    }
			}

			return doc;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private HWPFDocument fillTemplateDoc(FileInputStream fis) throws Exception {
		try {
			HWPFDocument doc = new HWPFDocument(fis);
			doc = replaceTextDoc(doc, "${username}", "Kavita Inthong 19042528");
			return doc;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private XWPFDocument fillTemplateDocX(FileInputStream fis) throws Exception {
		try {
			return replaceTextDocx(new XWPFDocument(fis), "${username}", "Kavita Inthong 19042528");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(fis != null) fis.close();
		}
	}

	@Override
	public void write(OutputStream os) throws IOException, WebApplicationException {
		OutputStream out = null;
		ByteArrayInputStream in = null;
		FileInputStream fis = null;
		
		try {
			if(isFillTemplate) {
				LOG.debug("Fill template values");
				out = new BufferedOutputStream(os);
				fis = new FileInputStream(new File(filePath));
				
				if (filePath.endsWith(".doc")) {
					HWPFDocument doc = fillTemplateDoc(fis);		
					doc.write(out);
				} else {
					try (XWPFDocument doc = fillTemplateDocX(fis)){
						doc.write(out);
					} catch (Exception e) {
						throw e;
					}
				}
			} else {
				LOG.debug("Get byte");
				java.nio.file.Path path = Paths.get(filePath);
				byte[] data = Files.readAllBytes(path);					
				
				in = new ByteArrayInputStream(data);
				out = new BufferedOutputStream(os);
				int bytes;
				
				while ((bytes = in.read()) != -1) {
					out.write(bytes);
				}
			}
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
		} finally {
			if(fis != null) fis.close();
			if(in != null) in.close();			
			if(out != null) out.close();			
		}	
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isFillTemplate() {
		return isFillTemplate;
	}

	public void setFillTemplate(boolean isFillTemplate) {
		this.isFillTemplate = isFillTemplate;
	}

}
