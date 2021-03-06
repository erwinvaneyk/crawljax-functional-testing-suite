package suite.distributed;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import suite.crawljax.CrawlManager;
import suite.distributed.configuration.ConfigurationDao;
import suite.distributed.workload.WorkloadDao;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
/**
 * The DatabaseUtils-class contains all of the miscellaneous functionality related to the database.
 */
public class DatabaseUtils {
	private ConnectionManager con;
	private CrawlManager crawlManager;
	private WorkloadDao workload;
	private ConfigurationDao config;

	@Inject
	public DatabaseUtils(ConnectionManager con, WorkloadDao workload, CrawlManager suite,
	        ConfigurationDao conf) {
		this.con = con;
		this.workload = workload;
		this.crawlManager = suite;
		this.config = conf;
	}

	/**
	 * Delete all the results that are inserted in the database with the crawl of id.
	 * 
	 * @param id
	 *            The id of the website, which results should be deleted
	 * @return true if the results are correctly deleted
	 */
	public boolean deleteAllResultsById(int id) {
		boolean result = false;
		Connection connection = con.getConnection();
		try {
			String getWorkTaskId = "SELECT workTask_id FROM WebsiteResults WHERE id=?";
			PreparedStatement statementWId = connection.prepareStatement(getWorkTaskId);
			statementWId.setInt(1, id);
			ResultSet resultWorkTask = statementWId.executeQuery();
			int workTaskId = 0;
			while (resultWorkTask.next()) {
				workTaskId = resultWorkTask.getInt(1);
				log.info("workTaskId={}", workTaskId);
			}
			log.info("Deleting the results of websiteId: " + id + ", workTaksId: " + workTaskId
			        + "...");

			boolean deleteDomResult = this.deleteById("DomResults", "websiteResult_id", id);
			boolean deleteWebsiteResults = this.deleteById("WebsiteResults", "id", id);
			boolean deleteWorkTask = this.deleteById("workload", "id", workTaskId);

			con.closeConnection();
			if (deleteDomResult && deleteWebsiteResults && deleteWorkTask) {
				log.info("Succesfully deleted the results of id={}", id);
				result = true;
			} else {
				log.warn("Not all results for websiteId {} could be deleted.", id);
			}
		} catch (SQLException e) {
			log.error("SQLException while deleting the results of id={}, reason {}.", id,
			        e.getMessage());
		}

		return result;
	}

	/**
	 * Delete an entry in a table given a column and a value.
	 * 
	 * @param table
	 *            the relevant table
	 * @param column
	 *            the column in the where-clause
	 * @param value
	 *            The value of the column to be matched.
	 * @return return true if the deleting was a success.
	 * @throws SQLException when there are problems with the connection to the database
	 */
	private boolean deleteById(String table, String column, int value) throws SQLException {
		boolean succes = false;
		String sql = "DELETE FROM " + table + " WHERE " + column + " = ?";
		PreparedStatement statement = con.getConnection().prepareStatement(sql);
		statement.setInt(1, value);
		int deleteDom = statement.executeUpdate();
		if (deleteDom > 0) {
			succes = true;
		} else {
			log.warn("The {} of id={} can not be deleted.", table, value);
		}
		return succes;
	}

	/**
	 * Sent a file, which contains websites, to the database.
	 * 
	 * @param file
	 *            The location of the file which content will be sent to the database
	 */
	public void actionFlushWebsitesFile(File file) {
		try {
			crawlManager.websitesFromFileToQueue(file);
			String rawUrl = crawlManager.getWebsiteQueue().poll();
			while (rawUrl != null) {
				workload.submitWork(new URL(rawUrl), false);
				rawUrl = crawlManager.getWebsiteQueue().poll();
			}
		} catch (IOException e) {
			log.error("Error while reading file: " + e.getMessage());
		}
	}

	/**
	 * Flushes entire local settings file to the server, replacing any interfering settings.
	 * 
	 * @param absoluteFilepath
	 *            the filename of the settings file.
	 */
	public void actionFlushSettingsFile(File absoluteFilepath) {
		try (InputStream file = new FileInputStream(absoluteFilepath)) {
			Ini ini = new Ini(file);
			for (Section section : ini.values()) {
				for (Entry<String, String> el : section.entrySet()) {
					config.updateConfiguration(section.getName(), el.getKey(), el.getValue());
				}
			}
		} catch (IOException e) {
			System.out.println("File not found: " + absoluteFilepath);
			log.error("Error while reading file: {}", e.getMessage());
		}
	}

	/**
	 * Retrieves the duplicate-mapping for a given websiteResultID.
	 * 
	 * @param websiteResultId
	 *            the websiteResultID for which the mapping should be retrieved
	 * @return map with tuples defining duplicates, using a format (WebsiteResultID,
	 *         WebsiteResultID), if an error occurred or nothing was found, return an empty map.
	 * @throws SQLException when there are problems with the connection to the database
	 */
	public Map<String, String> retrieveDuplicatesMap(int websiteResultId)
	        throws SQLException {
		Map<String, String> stateIds = new ConcurrentHashMap<String, String>();
		// Retrieve the duplicate mapping from the database.
		ResultSet res =
		        con.getConnection()
		                .createStatement()
		                .executeQuery(
		                        "SELECT * FROM  benchmarkSite WHERE websiteId = "
		                                + websiteResultId);
		while (res.next()) {
			stateIds.put(res.getString("stateIdFirst"), res.getString("stateIdSecond"));
		}
		return stateIds;
	}
}
