package main.java.distributed;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * The WorkloadDistributor is responsible for managing the workload of the clients. 
 * It should enable to send/retrieve/claim/unclaim/finish urls on the server.
 * 
 * A url or workunit can have three different states:
 * - unclaimed/Available:	The url has not been crawled and has no worker assigned.
 * - claimed:				A worker has been assigned, but has not finished crawling.
 * - Checked out/finished:	A worker has been assigned and has finished the crawling.
 *
 */
public class WorkloadDistributor {
	
	final Logger logger = Logger.getLogger(WorkloadDistributor.class.getName());
	
	private ConnectionManager connMgr;
	
	private String workerID;

	/**
	 * Sets up the ConnectionManager and creates an ID based on the hostname and local ip.
	 * @throws IOException The ConnectionManager could not retrieve the settings-file.
	 */
	public WorkloadDistributor() throws IOException {
		connMgr = new ConnectionManager();
		workerID = InetAddress.getLocalHost().toString();
		logger.info("WorkerID: " + workerID);
	}
	
	/**
	 * Retrieve and claim a number of urls from the server (if nothing is available, an empty ArrayList is returned).
	 * @param maxcount the maximum number of urls to retrieve.
	 * @return a list with claimed urls
	 */
	public ArrayList<String> retrieveWork(int maxcount) {
		assert maxcount > 0;
		ArrayList<String> urls = new ArrayList<String>();
		Connection conn = connMgr.getConnection();
		try {
			// Retrieve urls from the server.
			ResultSet res = conn.createStatement().executeQuery("SELECT * FROM  workload WHERE worker = \"\" AND crawled = 0 LIMIT " + maxcount);
			while (res.next()) {
				int id = res.getInt("id");
				urls.add(res.getString("url"));
				// Update the worker-field to show that the urls are claimed/worked on.
				conn.createStatement().executeUpdate("UPDATE workload SET worker=\"" + workerID + "\" WHERE id=" + id);
			}
		} catch (SQLException e) {
			logger.warning(e.getMessage());
		}
		connMgr.closeConnection();
		logger.info("Retrieved workload: " + urls);
		return urls;		
	}
	
	/**
	 * Registering a succesful crawl on the server.
	 * @param url The url to be checked out.
	 * @return true if checkout was succesful, else false. 
	 */
	public boolean checkoutWork(String url) {
		int ret = 0;
		Connection conn = connMgr.getConnection();
		try {
			// Update crawled-field to 1 to show crawl has finished.
			ret = conn.createStatement().executeUpdate("UPDATE workload SET crawled=1 WHERE url=\"" + url + "\"");
			logger.info("Checked out crawl of " + url);
		} catch (SQLException e) {
			logger.warning(e.getMessage());
		}
		connMgr.closeConnection();
		return ret != 0;
	}
	
	/**
	 * Submit a new url/workunit to the server to crawl.
	 * @param url the url to be crawled
	 * @return true if no errors occurred, else false.
	 */
	public boolean submitWork(String url) {
		int ret = 0;
		Connection conn = connMgr.getConnection();
		try {
			// Insert a new row containing the url in the workload-table.
			ret = conn.createStatement().executeUpdate("INSERT INTO workload (url,crawled,worker) VALUES (\"" + url + "\",0,\"\")");
			logger.info("Succesfully submitted " + url + " to the server.");
		} catch (SQLException e) {
			logger.warning(e.getMessage());
		}
		connMgr.closeConnection();
		return ret != 0;
	}

	/**
	 * Attempts to retrieve a number of urls. If nothing is available sleep and try again.
	 * @param maxcount the maximum number of urls to retrieve.
	 * @param sleepMilisecs the number of milliseconds to sleep
	 * @return a list with claimed urls
	 * @throws InterruptedException thrown when the worker is unexpectedly awakened
	 */
	public ArrayList<String> retrieveWork(int maxcount, int sleepMilisecs) throws InterruptedException {
		ArrayList<String> ret = new ArrayList<String>();
		ret = retrieveWork(maxcount);
		while(ret.isEmpty()) {
			ret = retrieveWork(maxcount);
			logger.info("Sleeping for " + sleepMilisecs + " miliseconds");
			Thread.sleep(sleepMilisecs);
		}
		return ret;
	}
	
	/**
	 * Reverts previously checked out or claimed work to the available state.
	 * @param url the url to be reverted
	 * @return true if successful, else false.
	 */
	public boolean revertWork(String url) {
		int ret = 0;
		Connection conn = connMgr.getConnection();
		try {
			Statement st = conn.createStatement();
			// Update the worker and crawled field to the default values for the url.
			ret = st.executeUpdate("UPDATE workload SET crawled=0, worker=\"\" WHERE url=\"" + url + "\"");
			logger.info("Reverted claim/checkout of crawl for " + url);
		} catch (SQLException e) {
			logger.warning(e.getMessage());
		}
		connMgr.closeConnection();
		return ret != 0;
	}
}
