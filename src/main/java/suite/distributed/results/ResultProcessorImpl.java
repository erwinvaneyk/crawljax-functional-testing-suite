package suite.distributed.results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;

import com.google.inject.Inject;

/**
 * ResultProcessorImpl should deal with the results of crawls, sending them to the SQL server.
 */
@Slf4j
public class ResultProcessorImpl implements ResultProcessor {

	private static final String PATH_RESULTS_JSON = "result.json";
	private static final String PATH_RESULTS_DOM = "doms";
	private static final String PATH_RESULTS_STRIPPEDDOM = "strippedDOM";
	private static final String PATH_RESULTS_SCREENSHOTS = "screenshots";

	private ResultUpload upload;

	@Inject
	public ResultProcessorImpl(ResultUpload upload) {
		this.upload = upload;
	}

	/**
	 * Upload the resulting all the results to the database.
	 * 
	 * @param id
	 *            The id of the crawled website that genarates the output folder
	 * @param dir
	 *            The directory that contains the output of the crawl
	 * @param duration
	 * 			  The duration of the crawl
	 */
	public void uploadResults(int id, File dir, long duration) {
		int websiteID = this.uploadJson(id, dir, duration);
		this.uploadDom(websiteID, dir);
		this.uploadStrippedDom(websiteID, dir);
		this.uploadScreenshot(websiteID, dir);

		// this.removeDir(dir);

		upload.closeConnection();
	}

	/**
	 * Upload only the result.json file to the database.
	 * 
	 * @param id
	 *            The id of the website
	 * @param dir
	 *            The output directory
	 * @param duration
	 *            The duration of the crawl
	 */
	public int uploadJson(int id, File dir, long duration) {
		File jsonFile = this.findFile(dir, PATH_RESULTS_JSON);
		String fileContent = this.readFile(jsonFile);
		return upload.uploadJson(id, fileContent, duration);
	}

	/**
	 * Upload only the dom of every state to the database.
	 * 
	 * @param websiteId
	 *            The id of the website
	 * @param dir
	 *            The output directory
	 */
	public void uploadDom(int websiteId, File dir) {
		File dirOfMap = this.findFile(dir, PATH_RESULTS_DOM);
		File[] files = dirOfMap.listFiles();

		log.info(files.length + " domstates found");
		for (File file : files) {
			String fileContent = this.readFile(file);
			String stateId = this.getStateId(file);

			upload.uploadDom(websiteId, fileContent, stateId);
		}
	}

	/**
	 * Upload only the stripped dom of every state to the database.
	 * 
	 * @param id
	 *            The id of the website
	 * @param dir
	 *            The output directory
	 */
	public void uploadStrippedDom(int id, File dir) {
		File dirOfMap = this.findFile(dir, PATH_RESULTS_STRIPPEDDOM);
		File[] files = dirOfMap.listFiles();

		log.info(files.length + " stripped dom-states found");
		for (File file : files) {
			String fileContent = this.readFile(file);
			String stateId = this.getStateId(file);
			upload.uploadStrippedDom(id, fileContent, stateId);
		}
	}

	/**
	 * Upload only the screenshot of every state to the database.
	 * 
	 * @param id
	 *            The id of the website
	 * @param dir
	 *            The output directory
	 */
	public void uploadScreenshot(int id, File dir) {
		File dirOfMap = this.findFile(dir, PATH_RESULTS_SCREENSHOTS);
		FileInputStream fr = null;
		for (File file : dirOfMap.listFiles()) {
			String stateId = this.getStateId(file);
			try {
				fr = new FileInputStream(file);
				upload.uploadScreenshot(id, fr, stateId);
			} catch (IOException e) {
				log.error("Can not close FileInputStream by uploading state {}, because {}.",
				        stateId, e.getMessage());
			} finally {
				try {
					fr.close();
				} catch (IOException e) {
					log.error("Failed to close file, because: {}", e.getMessage());
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void removeDir(String dir) {
		try {
			FileUtils.deleteDirectory(new File(dir));
			log.debug("Output directory removed.");
		} catch (IOException e) {
			log.error("IOException while removing the output directory: " + e.getMessage());
		}
	}

	private File findFile(File dir, String file) throws ResultProcessorException {
		File result = null;
		for (File fileOfDir : dir.listFiles()) {
			if (fileOfDir.getName().contains(file)) {
				result = fileOfDir;
			}
		}
		if (result == null) {
			throw new ResultProcessorException("The file, " + file
			        + ", cannot be found in the output directory " + dir.getAbsolutePath());
		}
		return result;
	}

	private String getStateId(File f) {
		String fileName = f.getName();
		int indexOfExtension = fileName.lastIndexOf('.');
		return fileName.substring(0, indexOfExtension);
	}

	/**
	 * Reads and returns all contents from a given file.
	 * 
	 * @param file
	 *            the relevant file
	 * @return contents of file
	 */
	private String readFile(File file) throws ResultProcessorException {
		StringBuilder fileContent = new StringBuilder((int) file.length());
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = br.readLine();
			while (line != null) {
				fileContent.append(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			throw new ResultProcessorException("Could not read file " + file.getName());
		}
		return fileContent.toString();
	}
}
