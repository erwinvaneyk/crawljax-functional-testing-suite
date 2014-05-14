package main.java.analysis;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import lombok.extern.slf4j.Slf4j;
import main.java.SuiteRunner;
import main.java.distributed.ConnectionManager;
import main.java.distributed.results.WebsiteResult;
import main.java.distributed.workload.WorkloadDAO;

@Slf4j
public class AnalysisFactory {
	
	public AnalysisReport getAnalysis(String title, int[] websiteids) throws SQLException {
		
		List<WebsiteResult> benchmarkedWebsites = retrieveWebsiteResultsById(websiteids);
		List<WebsiteResult> testWebsites = crawlBenchmarkedWebsites(benchmarkedWebsites);
		log.info("Benchmarked websites have been crawled");
		AnalysisReport analyse = new AnalysisReport(title, benchmarkedWebsites);
		analyse.runAnalysis(testWebsites);
		log.info("Results have been analysed");
		return analyse;
	}
	
	public List<WebsiteResult> retrieveWebsiteResultsById(int[] websiteids) throws SQLException {
		// Setup connections
		ConnectionSource connectionSource = new JdbcConnectionSource("jdbc:mysql://sql.ewi.tudelft.nl:3306/crawljaxsuite","erwin","maven.pom.xml");
		Dao<WebsiteResult, String> websiteResultDAO = DaoManager.createDao(connectionSource, WebsiteResult.class);

		// get websites
		Map<String,Object> map = new HashMap<String,Object>();
		for(Object id : websiteids) {
			map.put("workTask_id", id);
		}
		return websiteResultDAO.queryForFieldValues(map);
	}
	
	public List<WebsiteResult> crawlBenchmarkedWebsites(List<WebsiteResult> benchmarkedWebsites) throws SQLException {
		// Setup connections
		ConnectionSource connectionSource = new JdbcConnectionSource("jdbc:mysql://sql.ewi.tudelft.nl:3306/crawljaxsuite","erwin","maven.pom.xml");
		Dao<WebsiteResult, String> websiteResultDAO = DaoManager.createDao(connectionSource, WebsiteResult.class);

		WorkloadDAO workload = new WorkloadDAO(new ConnectionManager());
		Map<String, Object> crawledIds = new HashMap<String,Object>();
		for(WebsiteResult baseWebsite : benchmarkedWebsites) {
			log.debug("Work to submit: " + baseWebsite.getWorkTask());
			int newId = workload.submitWork(baseWebsite.getWorkTask().getURL());
			if(newId > -1) {
				crawledIds.put("workTask_id",newId);
				log.debug("Work submitted: " + baseWebsite.getWorkTask().getURL());
			} else {
				log.warn("Work rejected?!! dafuq");
			}
		}
		// wait until websites have been crawled
		SuiteRunner.main(new String[]{"-w","-finish"});
		// retrieve crawled websites
		return websiteResultDAO.queryForFieldValues(crawledIds);
		
	}
}
