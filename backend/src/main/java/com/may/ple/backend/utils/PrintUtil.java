package com.may.ple.backend.utils;

import java.util.ArrayList;
import java.util.List;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;

import org.apache.log4j.Logger;

public class PrintUtil {
	private static final Logger LOG = Logger.getLogger(PrintUtil.class.getName());

	public static List<String> listPrinter() throws Exception {
		
		try {
			
			AttributeSet aset = new HashAttributeSet();
//			aset.add(new PrinterName("MyPrinter", null));
			PrintService[] pservices = PrintServiceLookup.lookupPrintServices(null, aset);
			List<String> printers = new ArrayList<>();
			
			for (PrintService printService : pservices) {
				printers.add(printService.getName());
			}
			
			return printers;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
