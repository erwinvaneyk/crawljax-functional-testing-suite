package main.java.distributed.workload;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import main.java.distributed.ConnectionManager;

/**
 * SQL-server-based implementation of the IWorkloadDistributor-interface. The 
 * WorkloadDistributor is responsible for managing the workload of the clients.  
 */
@Slf4j
public class WorkloadDaoImpl implements WorkloadDao {
	
	private static final String TABLE = "workload";
	private static final String COLUMN_ID = "id";
	private static final String COLUMN_URL = "url";
	private static final String COLUMN_WORKERID = "worker";
	private static final String COLUMN_CRAWLED = "crawled";
	
	private ConnectionManager connMgr;
	
	private static String workerID;

	static {
		try {
			workerID = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets up the ConnectionManagerImpl and creates an ID based on the hostname and local ip.
	 * @throws IOException The ConnectionManagerImpl could not retrieve the settings-file.
	 */
	@Inject
	public WorkloadDaoImpl(ConnectionManager conn) {
		connMgr = conn;
		log.info("WorkerID: " + workerID);
	}
	
	/**
	 * Retrieve and claim a number of urls from the server (if nothing is available, an empty ArrayList is returned).
	 * @param maxcount the maximum number of urls to retrieve.
	 * @return a list with claimed urls
	 */
	public List<WorkTask> retrieveWork(int maxcount) {
		assert maxcount >= 0;
		ArrayList<WorkTask> workTasks = new ArrayList<WorkTask>();
		Connection conn = connMgr.getConnection();
		try {
			int claimed = conn.createStatement().executeUpdate("UPDATE "+ TABLE +" SET " + COLUMN_WORKERID + "=\"" + workerID
					+ "\"  WHERE " + COLUMN_CRAWLED + " = 0 AND " + COLUMN_WORKERID + "=\"\" LIMIT " + maxcount);
			log.debug("Workunits claimed by worker: " + claimed);
			// Retrieve urls from the server.
				// Note: this will also return the claimed/unfinished websites not signed off.
			ResultSet res = conn.createStatement().executeQuery("SELECT * FROM  "+ TABLE +" WHERE "+ COLUMN_WORKERID + " = \"" 
					+ workerID + "\" AND " + COLUMN_CRAWLED + " = 0");
			while (res.next()) {
				try {
					int id = res.getInt("id");
					URL url = new URL(res.getString("url"));
					WorkTask workTask = new WorkTask(id, url);
					workTasks.add(workTask);
					log.info("Worktask retrieved: " + workTask.getURL());
				} catch (MalformedURLException e) {
					log.error(e.getMessage());
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		connMgr.closeConnection();
		return workTasks;		
	}
	
	/**
	 * Registering a succesful crawl on the server.
	 * @param url The url to be checked out.
	 * @return true if checkout was succesful, else false. 
	 */
	public boolean checkoutWork(WorkTask wt) {
		assert wt != null;
		int ret = 0;
		Connection conn = connMgr.getConnection();
		try {
			// Update crawled-field to 1 to show crawl has finished.
			ret = conn.createStatement().executeUpdate("UPDATE " + TABLE + " SET " + COLUMN_CRAWLED +"=1 WHERE " + COLUMN_ID + "=\"" + wt.getId() + "\"");
			log.info("Checked out crawl of id: " + wt.getId());
		} catch (SQLException e) {
			log.error(e.getMessage());
		} catch (NullPointerException e) {
			log.error("Unexpected null, probably the input " + wt + " or the connection " + conn);
		}
		connMgr.closeConnection();
		return ret != 0;
	}
	
	/**
	 * Submit a new url/workunit to the server to crawl.
	 * @param url the url to be crawled
	 * @return generated id of the url, if failed: -1
	 */
	public int submitWork(URL url, boolean claim) {
		int ret = -1;
		Connection conn = connMgr.getConnection();
		try {
			String worker = claim ? workerID : "";
			// Insert a new row containing the url in the workload-table.
			Statement statement = conn.createStatement();
			ret = statement.executeUpdate("INSERT INTO " + TABLE +" (" + COLUMN_URL +"," 
					+ COLUMN_CRAWLED +"," + COLUMN_WORKERID + ") VALUES (\"" + url + "\",0, \"" + worker + "\")", Statement.RETURN_GENERATED_KEYS);
			log.info("Succesfully submitted {} to the server.", url);
			
			// Get generated key
			ResultSet generatedkeys = statement.getGeneratedKeys();
	        if (generatedkeys.next()) {
	            ret = generatedkeys.getInt(1);
	        }
		} catch (SQLException e) {
			log.error(e.getMessage());
		}  catch (NullPointerException e) {
			log.error("Unexpected null, probably the input " + url + " or the connection " + conn);
		}
		connMgr.closeConnection();
		return ret;
	}
	
	/**
	 * Reverts previously checked out or claimed work to the available state.
	 * @param url the url to be reverted
	 * @return true if successful, else false.
	 */
	public boolean revertWork(int id) {
		int ret = 0;
		Connection conn = connMgr.getConnection();
		try {
			Statement st = conn.createStatement();
			// Update the worker and crawled field to the default values for the url.
			ret = st.executeUpdate("UPDATE " + TABLE +" SET " + COLUMN_CRAWLED + "=0, " + COLUMN_WORKERID 
					+"=\"\" WHERE " + COLUMN_ID + "=\"" + id + "\"");
			log.info("Reverted claim/checkout of crawl for id: " + id);
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		connMgr.closeConnection();
		return ret != 0;
	}
}
