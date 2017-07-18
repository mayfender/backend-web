package com.may.ple.backend.schedulers.jobs;


public interface Job {
	
	void register();
	void run();
	void runSync();

}
