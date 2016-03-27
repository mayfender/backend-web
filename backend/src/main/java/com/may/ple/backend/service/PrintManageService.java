package com.may.ple.backend.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.ServiceData;
import com.may.ple.backend.exception.CustomerException;

@Service
public class PrintManageService {
	private static final Logger LOG = Logger.getLogger(PrintManageService.class.getName());
	private String header = "              ใบเสร็จรับเงิน";
	private String footer = "         วรวุฒิ พลวิชัย 089-2140956";
	
	public void tananatEms(ServiceData serviceData) throws Exception {
		LOG.debug("Start");
		
		try {
			
			Date date = new Date();
			
			printHeader(header);
			printNormal("เลขที่ " + serviceData.getDocNo() + "     " + String.format("%1$td/%1$tm/%1$tY    %1$tH:%1$tM:%1$tS", date));
			printNormal("ธนาณัติ EMS: " + String.format("%.2f", serviceData.getAmount()));
			printNormal("ผู้ส่ง: " + serviceData.getSender());
			printNormal("ผู้รับ: " + serviceData.getReceiver());
			printNormal("ไปรษณีปลายทาง: " + serviceData.getPostDest());
			printNormal("ค่าธรรมเนียม: " + String.format("%.2f", serviceData.getFee() == null ? 0 : serviceData.getFee()));
			printNormal("ค่าบริการอื่นๆ: " + String.format("%.2f", serviceData.getOtherServicePrice() == null ? 0 : serviceData.getOtherServicePrice()));
			printNormal(footer);
			printCut();
			
		} catch (PrintException e) {
			throw new CustomerException(5000, e.toString());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
		LOG.debug("End");
	}
	
	public void payService(ServiceData serviceData) throws Exception {
		LOG.debug("Start");
		
		try {
			
			Date date = new Date();
			
			printHeader(header);
			printNormal("เลขที่ " + serviceData.getDocNo() + "     " + String.format("%1$td/%1$tm/%1$tY    %1$tH:%1$tM:%1$tS", date));
			printNormal("ชำระค่าบริการ: " + String.format("%.2f", serviceData.getAmount()));
			printNormal("ผู้ส่ง: " + serviceData.getSender());
			printNormal("ผู้รับ: " + serviceData.getReceiver());
			printNormal("ค่าธรรมเนียม: " + String.format("%.2f", serviceData.getFee() == null ? 0 : serviceData.getFee()));
			printNormal("ค่าบริการอื่นๆ: " + String.format("%.2f", serviceData.getOtherServicePrice() == null ? 0 : serviceData.getOtherServicePrice()));
			printNormal(footer);
			printCut();
			
		} catch (PrintException e) {
			throw new CustomerException(5000, e.toString());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
		LOG.debug("End");
	}
	
	public void tananatOnline(ServiceData serviceData) throws Exception {
		LOG.debug("Start");
		
		try {
			
			Date date = new Date();
			
			printHeader(header);
			printNormal("เลขที่ " + serviceData.getDocNo() + "     " + String.format("%1$td/%1$tm/%1$tY    %1$tH:%1$tM:%1$tS", date));
			printNormal("ธนาณัติออนไลน์: " + String.format("%.2f", serviceData.getAmount()));
			printNormal("ผู้ส่ง: " + serviceData.getSender());
			printNormal("ผู้รับ: " + serviceData.getReceiver());
			printNormal("ไปรษณีปลายทาง: " + serviceData.getPostDest());
			printNormal("ค่าธรรมเนียม: " + String.format("%.2f", serviceData.getFee() == null ? 0 : serviceData.getFee()));
			printNormal("ค่าบริการอื่นๆ: " + String.format("%.2f", serviceData.getOtherServicePrice() == null ? 0 : serviceData.getOtherServicePrice()));
			printNormal(footer);
			printCut();
			
		} catch (PrintException e) {
			throw new CustomerException(5000, e.toString());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
		LOG.debug("End");
	}
	
	public void payVehicle(ServiceData serviceData) throws Exception {
		LOG.debug("Start");
		
		try {
			
			Date date = new Date();
			
			printHeader(header);
			printNormal("เลขที่ " + serviceData.getDocNo() + "     " + String.format("%1$td/%1$tm/%1$tY    %1$tH:%1$tM:%1$tS", date));
			printNormal("ชำระค่างวดรถยนต์: " + String.format("%.2f", serviceData.getAmount()));
			printNormal("ผู้ส่ง: " + serviceData.getSender());
			printNormal("ผู้รับ: " + serviceData.getReceiver());
			printNormal("ค่าธรรมเนียม: " + String.format("%.2f", serviceData.getFee() == null ? 0 : serviceData.getFee()));
			printNormal("ค่าบริการอื่นๆ: " + String.format("%.2f", serviceData.getOtherServicePrice() == null ? 0 : serviceData.getOtherServicePrice()));
			printNormal(footer);
			printCut();
			
		} catch (PrintException e) {
			throw new CustomerException(5000, e.toString());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
		LOG.debug("End");
	}
	
	public void transfer(ServiceData serviceData) throws Exception {
		LOG.debug("Start");
		
		try {
			
			Date date = new Date();
			
			printHeader(header);
			printNormal("เลขที่ " + serviceData.getDocNo() + "     " + String.format("%1$td/%1$tm/%1$tY    %1$tH:%1$tM:%1$tS", date));
			printNormal("โอนเงินเข้าบัญชีธนาคาร: " + String.format("%.2f", serviceData.getAmount()));
			printNormal("ชื่อบัญชี: " + serviceData.getAccName());
			printNormal("ธนาคาร: " + serviceData.getBankName());
			printNormal("เลขบัญชี: " + serviceData.getAccNo());
			printNormal("ค่าธรรมเนียม: " + String.format("%.2f", serviceData.getFee() == null ? 0 : serviceData.getFee()));
			printNormal("ค่าบริการอื่นๆ: " + String.format("%.2f", serviceData.getOtherServicePrice() == null ? 0 : serviceData.getOtherServicePrice()));
			printNormal(footer);
			printCut();
			
		} catch (PrintException e) {
			throw new CustomerException(5000, e.toString());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
		LOG.debug("End");
	}
	
	private void printHeader(String msg) throws Exception {
		byte[] data = processData(msg.toString());
		
		byte[] initCommand = new byte[]{27, 64};
		byte[] comm = {27, 69, 1};
		byte[] des = new byte[initCommand.length + comm.length + data.length];
		
		System.arraycopy(initCommand, 0, des, 0, initCommand.length);	
		System.arraycopy(comm, 0, des, initCommand.length, comm.length);
		System.arraycopy(data, 0, des, initCommand.length + comm.length, data.length);
		
		print(des);
	}
	
	private void printNormal(String msg) throws Exception {
		
		byte[] data = processData(msg.toString());
		
		byte[] initCommand = new byte[]{27, 64};
		byte[] des = new byte[initCommand.length + data.length];
		
		System.arraycopy(initCommand, 0, des, 0, initCommand.length);	
		System.arraycopy(data, 0, des, initCommand.length, data.length);
		print(des);
	}
	
	private void printCut() throws Exception {
		byte[] feedAndCut = new byte[]{10, 27, 100, 8, 27, 105};
		print(feedAndCut);
	}
	
	private PrintService getPrinterService() {
		String defaultPrinter = PrintServiceLookup.lookupDefaultPrintService().getName();
		System.out.println("Default printer: " + defaultPrinter);
		return PrintServiceLookup.lookupDefaultPrintService();
	}
	
	private void print(byte command[]) throws Exception {
		try {
			PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
			pras.add(new Copies(1));
			
			InputStream is;
			DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
			
			is = new ByteArrayInputStream(command);
			Doc doc = new SimpleDoc(is, flavor, null);
			DocPrintJob job = getPrinterService().createPrintJob();
			
			PrintJobWatcher pjw = new PrintJobWatcher(job);
			job.print(doc, pras);
			pjw.waitForDone();
			is.close();
		} catch (Exception e) {
			throw e;
		}
	}
	
	private byte[] processData(String str) throws Exception {
		
		byte saraUp[] = "ึัี๊้็่๋ิื์".getBytes("tis620");
		byte saraDown[] = "ุู".getBytes("tis620");
		
		byte src[] = str.getBytes("tis620");
		List<String> saraUpUpLst = new ArrayList<>();
		List<String> saraUpLst = new ArrayList<>();
		List<String> saraDownLst = new ArrayList<>();
		List<Byte> character = new ArrayList<>();
		
		int spaceUpUp = -1;
		int spaceUp = -1;
		int spaceDown = -1;
		boolean isLastSaraUp = false;
		
		outer: for (int i = 0; i < src.length; i++) {
			
			for (int j = 0; j < saraUp.length; j++) {
				if(src[i] == saraUp[j]) {
					
					if(isLastSaraUp) {
						saraUpUpLst.add(String.valueOf(spaceUpUp + "," + src[i]));
						isLastSaraUp = false;
						spaceUpUp = -1;
					} else {
						saraUpLst.add("" + spaceUp + "," + src[i]);
						isLastSaraUp = true;
						spaceUp = -1;
					}					
					continue outer;
				}
			}
			
			isLastSaraUp = false;
			
			for (int j = 0; j < saraDown.length; j++) {
				if(src[i] == saraDown[j]) {
					
					saraDownLst.add("" + spaceDown + "," + src[i]);
					spaceDown = -1;
					continue outer;
					
				}
			}
			
			spaceUpUp++;
			spaceUp++;
			spaceDown++;
			character.add(src[i]);
		}
		
		//--------------------------------------
		
		
				
		byte[] genSaraUpUp = genSaraUp(saraUpUpLst, true);
		byte[] genSaraUp = genSaraUp(saraUpLst, false);
		byte[] genCharacter = genCharacter(character);
		byte[] genSaraDown = genSaraDown(saraDownLst);
		byte[] newLine = {10};
		
		byte[] total = new byte[
		                        genSaraUpUp.length + 
		                        genSaraUp.length + 
		                        genCharacter.length + 
		                        genSaraDown.length + 
		                        newLine.length
		                        ];
		
		System.arraycopy(genSaraUpUp, 0, total, 0, genSaraUpUp.length);
		System.arraycopy(genSaraUp, 0, total, genSaraUpUp.length, genSaraUp.length);
		System.arraycopy(genCharacter, 0, total, genSaraUpUp.length + genSaraUp.length, genCharacter.length);
		System.arraycopy(genSaraDown, 0, total, genSaraUpUp.length + genSaraUp.length + genCharacter.length, genSaraDown.length);
		System.arraycopy(newLine, 0, total, genSaraUpUp.length + genSaraUp.length + genCharacter.length + genSaraDown.length, newLine.length);
		
		return total;
	}
	
	private byte[] genSaraUp(List<String> saraUpLst, boolean isUpUp) {
		if(saraUpLst.size() == 0) return new byte[0];
		
		List<Byte> list = new ArrayList<>();
		byte space[] = {32};
		
		for (String str : saraUpLst) {
			String s[] = str.split(",");
			
			int num = Integer.parseInt(s[0]);
			for (int i = 0; i < num; i++) {								
				list.add(space[0]);
			}
			
			list.add(Byte.parseByte(s[1]));
		}
		
		//--------------------: Feed Up :-------------------------------
		byte feedUp[];
		if(isUpUp) {			
			feedUp = new byte[]{27, 74, 8};
		} else {			
			feedUp = new byte[]{27, 74, 15};
		}
		
		for (int i = 0; i < feedUp.length; i++) {
			list.add(feedUp[i]);					
		}
		
		byte bytes[] = new byte[list.size()];
		
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = list.get(i);
		}
		
		return bytes;
	}
	
	private byte[] genCharacter(List<Byte> character) {
		byte bytes[] = new byte[character.size()];
		
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = character.get(i);
		}
		return bytes;
	}
	
	private byte[] genSaraDown(List<String> saraDowLst) {
		if(saraDowLst.size() == 0) return new byte[0];
				
		List<Byte> list = new ArrayList<>();
		byte space[] = {32};
		
		//--------------------: Feed Up :-------------------------------
		byte feedUp[] = new byte[]{27, 74, 15};
		
		for (int i = 0; i < feedUp.length; i++) {
			list.add(feedUp[i]);					
		}
		
		//---------------------------------------------------------------
		for (String str : saraDowLst) {
			String s[] = str.split(",");
			
			int num = Integer.parseInt(s[0]);
			for (int i = 0; i < num; i++) {								
				list.add(space[0]);
			}
			
			list.add(Byte.parseByte(s[1]));
		}
		
		byte bytes[] = new byte[list.size()];
		
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = list.get(i);
		}
		
		return bytes;
	}

}






class PrintJobWatcher {
	boolean done = false;

	PrintJobWatcher(DocPrintJob job) {
		job.addPrintJobListener(new PrintJobAdapter() {
			public void printJobCanceled(PrintJobEvent pje) {
				allDone();
			}

			public void printJobCompleted(PrintJobEvent pje) {
				allDone();
			}

			public void printJobFailed(PrintJobEvent pje) {
				allDone();
			}

			public void printJobNoMoreEvents(PrintJobEvent pje) {
				allDone();
			}

			void allDone() {
				synchronized (PrintJobWatcher.this) {
					done = true;
					System.out.println("Printing done ...");
					PrintJobWatcher.this.notify();
				}
			}
		});
	}

	public synchronized void waitForDone() {
		try {
			while (!done) {
				wait();
			}
		} catch (InterruptedException e) {
		}
	}

}
