package test.java.analysis;

import static org.junit.Assert.*;

import java.io.File;

import main.java.analysis.AnalysisProcessorFile;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAnalysisProcessorFile {
	
	private AnalysisProcessorFile apf;
	
	private static File testDir = new File(System.getProperty("user.dir") + "/output/temp/");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testDir.mkdir();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		testDir.delete();
	}

	@Before
	public void setUp() throws Exception {
		apf = new AnalysisProcessorFile();
		apf.setOutputDir(testDir);
	}
	
	@Test
	public void testApplyNull() {
		apf.apply(null);
		assertNull(apf.getOutput());
	}
}