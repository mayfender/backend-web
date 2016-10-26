package com.may.ple.backend.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.log4j.Logger;

public class ExecUtil {
	private static final Logger LOG = Logger.getLogger(ExecUtil.class.getName());
	
	public static void exec(String command, int exitVal) throws Exception {
		
		try {
			CommandLine cmdLine = CommandLine.parse(command);
	        DefaultExecutor executor = new DefaultExecutor();
	        executor.setExitValue(exitVal);
	        
	        //--: kills a run-away process after 60 seconds.
	        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
	        executor.setWatchdog(watchdog);
	        
	        int exitValue = executor.execute(cmdLine);
	        
	        if(exitValue != exitVal) {
	        	throw new Exception("Cann't backup database");
	        }
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
