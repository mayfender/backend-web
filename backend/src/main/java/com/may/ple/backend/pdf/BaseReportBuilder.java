package com.may.ple.backend.pdf;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPageEventHelper;

public abstract class BaseReportBuilder extends PdfPageEventHelper {
	private static final Logger LOG = Logger.getLogger(BaseReportBuilder.class.getName());
    protected BaseFont baseFont;
 
    public BaseReportBuilder() {
        super();
        
        /*
         * TH Niramit AS.ttf
         * THSarabun.ttf
         * angsau_0.ttf
         * angsa.ttf
         */
        
        baseFont = load(".", "TH Niramit AS.ttf");
    }
    
    private BaseFont load(String location, String fontname) {
    	ByteArrayOutputStream out = null;
    	InputStream in = null;
    	
        try {
            in = getClass().getClassLoader().getResourceAsStream(location + System.getProperty("file.separator") + fontname);
            out = new ByteArrayOutputStream();
            
            byte buf[] = new byte[1024];
 
            while (true) {
                int size = in.read(buf);
                if (size < 0)
                    break;
                out.write(buf, 0, size);
            }
            buf = out.toByteArray();
            return BaseFont.createFont(fontname, BaseFont.IDENTITY_H, true, true, buf, null);
        } catch (Exception ex) {
        	LOG.error(ex.toString());
            return null;
        } finally {
        	try {
        		if(in != null) in.close();				
			} catch (Exception e) {}
        	try {
        		if(out != null) out.close();				
			} catch (Exception e) {}
        }
    }
    
    public abstract byte[] createPdf() throws Exception;
    
    /*private String priceFormat(Double price) {
    	String result;
    	
    	if(price == null || price == 0) {
    		result = "";
    	} else {
    		result = String.format("%,.2f", price);    		
    	}
    	return result;
    }*/
    
}