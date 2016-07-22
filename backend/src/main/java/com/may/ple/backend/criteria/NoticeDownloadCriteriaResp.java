package com.may.ple.backend.criteria;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.may.ple.backend.utils.Number2WordUtil;

public class NoticeDownloadCriteriaResp extends CommonCriteriaResp implements StreamingOutput {
	private static final Logger LOG = Logger.getLogger(NoticeDownloadCriteriaResp.class.getName());
	private static final String DATE_FORMAT = "%1$td/%1$tm/%1$tY";
	private String filePath;
	private boolean isFillTemplate;
	private String address;
	private Map<String, Object> taskDetail;
	
	private HWPFDocument replaceTextDoc(HWPFDocument doc, VelocityContext context) {
		StringWriter writer;
		Range r = doc.getRange();
		
		for (int i = 0; i < r.numSections(); ++i) {
			Section s = r.getSection(i);
			
			for (int j = 0; j < s.numParagraphs(); j++) {
				Paragraph p = s.getParagraph(j);
				
				for (int k = 0; k < p.numCharacterRuns(); k++) {
					CharacterRun run = p.getCharacterRun(k);
					String text = run.text();
					
					if (text != null) {
						writer = new StringWriter();
				        Velocity.evaluate(context, writer, "TemplateName", text);
				        run.delete();
				        run.insertBefore(writer.toString());
					}
				}
			}
		}
		
		return doc;
	}
	
	private XWPFDocument replaceTextDocx(XWPFDocument doc, VelocityContext context) {
		try {
			StringWriter writer;
			
			for (XWPFParagraph p : doc.getParagraphs()) {
			    List<XWPFRun> runs = p.getRuns();
			    
			    if (runs != null) {
			        for (XWPFRun r : runs) {
			            String text = r.getText(0);
			            
			            if (text != null) {
			            	writer = new StringWriter();
					        Velocity.evaluate(context, writer, "TemplateName", text);
			                r.setText(writer.toString(), 0);
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

	private HWPFDocument fillTemplateDoc(FileInputStream fis, VelocityContext context) throws Exception {
		try {
			HWPFDocument doc = new HWPFDocument(fis);
			doc = replaceTextDoc(doc, context);
			return doc;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private XWPFDocument fillTemplateDocX(FileInputStream fis, VelocityContext context) throws Exception {
		try {
			return replaceTextDocx(new XWPFDocument(fis), context);
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
				Velocity.init();
				
				VelocityContext context = new VelocityContext();
		        context.put("createdDate", String.format(DATE_FORMAT, new Date()));
		        context.put("address", this.address);
		        context.put("price", Number2WordUtil.bahtText(String.format("%,.2f", 2155.25)));
		        
		        for(Entry<String, Object> entry : this.taskDetail.entrySet()) {
		        	Object val;
		        	if(entry.getValue() instanceof Date) {
		        		try {
		        			val = String.format(DATE_FORMAT, entry.getValue());							
		        		} catch (Exception e) {
		        			val = entry.getValue();
							LOG.error(e.toString());
						}
		        	} else if(entry.getValue() instanceof Number) {
		        		try {
		        			context.put(entry.getKey() + "_word", Number2WordUtil.bahtText(String.valueOf(entry.getValue())));
		        			val = String.format("%,.2f", entry.getValue());
						} catch (Exception e) {
							val = entry.getValue();
							LOG.error(e.toString());
						}
		        	} else {
		        		val = entry.getValue();
		        	}
		        	context.put(entry.getKey().replaceAll("\\s",""), val == null ? " " : val);
		        }
		        
				if (filePath.endsWith(".doc")) {
					HWPFDocument doc = fillTemplateDoc(fis, context);		
					doc.write(out);
				} else {
					try (XWPFDocument doc = fillTemplateDocX(fis, context)){
						doc.write(out);
					} catch (Exception e) {
						LOG.error(e.toString());
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Map getTaskDetail() {
		return taskDetail;
	}

	public void setTaskDetail(Map taskDetail) {
		this.taskDetail = taskDetail;
	}

}
