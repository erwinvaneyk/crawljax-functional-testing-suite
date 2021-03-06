/**
 * 
 */
package suite.analysis;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestStatistic {

	/**
	 * Test method for {@link suite.analysis.Statistic#hasDetails()}.
	 */
	@Test
	public void testHasDetailsTrue() {
		List<Object> details = new ArrayList<Object>();
		details.add("detail 1");
		Statistic stat = new Statistic("Name", "value", details);
		assertTrue(stat.hasDetails());
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#hasDetails()}.
	 */
	@Test
	public void testHasDetailsFalse() {
		Statistic stat = new Statistic("Name", "value", null);
		assertFalse(stat.hasDetails());
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#getName()}.
	 */
	@Test
	public void testGetName() {
		Statistic stat = new Statistic("Name", "value");
		assertEquals("Name", stat.getName());
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#getValue()}.
	 */
	@Test
	public void testGetValue() {
		Statistic stat = new Statistic("Name", "value");
		assertEquals("value", stat.getValue());
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#getDetails()}.
	 */
	@Test
	public void testGetDetails() {
		List<Object> details = new ArrayList<Object>();
		details.add("detail 1");
		Statistic stat = new Statistic("Name", "value", details);
		assertEquals(details, stat.getDetails());
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		List<Object> details = new ArrayList<Object>();
		details.add("detail 1");
		Statistic stat = new Statistic("Name", "value", details);
		List<Object> details2 = new ArrayList<Object>();
		details2.add("detail 1");
		Statistic stat2 = new Statistic("Name", "value", details2);
		assertTrue(stat.equals(stat2));
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObjectOtherDetails() {
		List<Object> details = new ArrayList<Object>();
		details.add("detail 1");
		Statistic stat = new Statistic("Name", "value", details);
		List<Object> details2 = new ArrayList<Object>();
		Statistic stat2 = new Statistic("Name", "value", details2);
		assertFalse(stat.equals(stat2));
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObjectOtherName() {
		Statistic stat = new Statistic("OTHER", "value");
		Statistic stat2 = new Statistic("Name", "value");
		assertFalse(stat.equals(stat2));
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObjectOtherValue() {
		Statistic stat = new Statistic("Name", "OTHER");
		Statistic stat2 = new Statistic("Name", "value");
		assertFalse(stat.equals(stat2));
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObjectNull() {
		Statistic stat = new Statistic("Name", "value");
		assertFalse(stat.equals(null));
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObjectOtherObject() {
		Statistic stat = new Statistic("Name", "value");
		assertFalse(stat.equals(new Object()));
	}

	/**
	 * Test method for {@link suite.analysis.Statistic#toString()}.
	 */
	@Test
	public void testToString() {
		Statistic stat = new Statistic("Name", "value");
		assertNotNull(stat.toString());
	}

}
