package main.java;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

import main.java.distributed.configuration.ConfigurationDAO;
import main.java.distributed.configuration.IConfigurationDAO;
import main.java.distributed.results.IResultProcessor;
import main.java.distributed.results.ResultProcessor;
import main.java.distributed.workload.IWorkloadDAO;
import main.java.distributed.workload.WorkloadDAO;
import main.java.distributed.workload.WorkTask;
import main.java.distributed.workload.WorkloadRunner;

import com.crawljax.cli.JarRunner;

public class SuiteRunner {

	public static void main(String[] args) {
		// Header
		System.out.println("Crawljax Functional Testing Suite");
		System.out.println("---------------------------------");
		System.out.println("Crawljax CLI details:");
		JarRunner.main(new String[] {"--version"});
		System.out.println("---------------------------------");
		
		// Do stuff
		new SuiteRunner(args);
		// Finish up.
		System.out.println("Finished.");
	}
	
	public SuiteRunner(String[] args) {
		// Parse Args
		String arg = "";
		String[] additionalArgs = null;
		if (args.length > 0) {
			arg = args[0];
			additionalArgs = Arrays.copyOfRange(args, 1, args.length);
		}
		// Actions
		if (arg.equals("-w") || arg.equals("--worker")) {
			actionWorker();
		} else if (arg.equals("-f") || arg.equals("--flush")) {
			actionFlushWebsitesFile();
		} else if (arg.equals("-s") || arg.equals("--settings")) {
			actionFlushSettingsFile();
		} else if (arg.equals("-d") || arg.equals("--distributor")) {
			actionDistributor(additionalArgs);
		} else if (arg.equals("-l") || arg.equals("--local")) {
			actionLocalCrawler();
		} else {
			actionHelp();
		}
	}
	
	private void actionHelp() {
		Options options = new Options();
		options.addOption("w","worker", false, "Setup computer as slave/worker, polling the db continuously.");
		options.addOption("f","flush", false, "Flushes the website-file to the server. Nothing is crawled.");
		options.addOption("s","settings", false, "Flushes setting-file to the server. Nothing is crawled.");
		options.addOption("d","distributor", false, "Runs the commandline interface of the distributor.");
		options.addOption("l","local", false, "Do not use server-functionality. Read the website-file and crawl all.");
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Cryptotrader CLI", options);
	}

	private void actionWorker() {
		try {
			IResultProcessor resultprocessor = new ResultProcessor();
			SuiteManager suite = new SuiteManager();
			IWorkloadDAO workload = new WorkloadDAO();

			System.out.println("Started client crawler/worker.");
			while (true) {

				List<WorkTask>workTasks = workload.retrieveWork(1, 1000 * 10); //poll server every 10 seconds
				
				try {
					for (WorkTask task : workTasks) {
						Map<String, String> args = suite.buildSettings(task.getUrl());
						
						suite.runCrawler(args);
						String dir = args.get(SuiteManager.ARG_OUTPUTDIR);
	
						resultprocessor.uploadOutputJson(task.getId(), dir);
						workload.checkoutWork(task.getUrl());
						System.out.println("crawl: " + task.getUrl() + " completed");
					}
				} catch (URISyntaxException e) {
					System.out.println("Crawl failed: invalid url");
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Sleep interrupted; worker stopped.");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private void actionFlushWebsitesFile() {
		try {
			IWorkloadDAO workload = new WorkloadDAO();
			SuiteManager suite = new SuiteManager();
			
			suite.websitesFromFile(SuiteManager.DEFAULT_SETTINGS_DIR + "/websites.txt");
			String url;
			while((url = suite.getWebsiteQueue().poll()) != null) {
				workload.submitWork(url);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("File flushed to server.");
	}
	
	private void actionFlushSettingsFile() {
		try {
			IConfigurationDAO conf = new ConfigurationDAO();
			Ini ini = new Ini(new FileReader(SuiteManager.DEFAULT_SETTINGS_DIR + SuiteManager.DEFAULT_SETTINGS_INI));
			for (Section section : ini.values()) {
				Map<String,String> map = new HashMap<String,String>();
				List<String> li = new ArrayList<String>();
				li.add(section.getName());
				for (Entry<String, String> el : section.entrySet()) {
					map.put(el.getKey(), el.getValue());
				}
				conf.updateConfiguration(li, map, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void actionLocalCrawler() {
		System.out.println("Started local crawler");
		try {
			SuiteManager suite = new SuiteManager();
			suite.websitesFromFile(SuiteManager.DEFAULT_SETTINGS_DIR + "/websites.txt");
			suite.crawlWebsites();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void actionDistributor(String[] args) {
		System.out.println("Distributor CLI:");
		WorkloadRunner.main(args);
	}
}
