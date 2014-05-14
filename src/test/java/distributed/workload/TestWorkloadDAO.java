package test.java.distributed.workload;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import main.java.distributed.ConnectionManager;
import main.java.distributed.workload.WorkTask;
import main.java.distributed.workload.WorkloadDAO;

import org.junit.Test;

public class TestWorkloadDAO {
	
	
	
	@Test
	public void testRetrieveWorkInt0() throws SQLException, MalformedURLException {
		// Result set 
		List<WorkTask> expected = new ArrayList<WorkTask>();
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet results = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expected.size());
		when(statement.executeQuery(anyString())).thenReturn(results);
		when(results.next()).thenReturn(false);
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expected.size());
		assertEquals(finalReturn, expected);
	}
	
	@Test
	public void testRetrieveWorkInt1() throws SQLException, MalformedURLException {
		// Result set 
		List<WorkTask> expected = new ArrayList<WorkTask>();
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		expected.add(wt1);
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet results = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expected.size());
		when(statement.executeQuery(anyString())).thenReturn(results);
		when(results.next()).thenReturn(true, false);
		when(results.getInt("id")).thenReturn(wt1.getId());
		when(results.getString("url")).thenReturn(wt1.getURL().toString());
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expected.size());
		assertEquals(finalReturn, expected);
	}
	
	@Test
	public void testRetrieveWorkInt2() throws SQLException, MalformedURLException {
		// Result set 
		List<WorkTask> expected = new ArrayList<WorkTask>();
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		WorkTask wt2 = new WorkTask(2, new URL("http://2.com"));
		expected.add(wt1); expected.add(wt2);
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet results = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expected.size());
		when(statement.executeQuery(anyString())).thenReturn(results);
		when(results.next()).thenReturn(true, true, false);
		when(results.getInt("id")).thenReturn(wt1.getId(),wt2.getId());
		when(results.getString("url")).thenReturn(wt1.getURL().toString(),wt2.getURL().toString());
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expected.size());
		assertEquals(finalReturn, expected);
	}
	
	@Test
	public void testRetrieveWorkIntSQLExceptionSELECT() throws SQLException, MalformedURLException {
		// Result set 
		int expectedSize = 4;
		List<WorkTask> expected = new ArrayList<WorkTask>();
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expectedSize);
		when(statement.executeQuery(anyString())).thenThrow(new SQLException("MOCK SQL ERROR"));
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expectedSize);
		assertEquals(finalReturn, expected);
	}
	
	@Test
	public void testRetrieveWorkIntSQLExceptionUPDATE() throws SQLException, MalformedURLException {
		// Result set 
		int expectedSize = 4;
		List<WorkTask> expected = new ArrayList<WorkTask>();
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet results = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenThrow(new SQLException("MOCK SQL ERROR"));
		when(statement.executeQuery(anyString())).thenReturn(results);
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expectedSize);
		assertEquals(finalReturn, expected);
	}
	
	@Test
	public void testRetrieveWorkIntMoreThanAvailable() throws SQLException, MalformedURLException {
		// Result set 
		List<WorkTask> expected = new ArrayList<WorkTask>();
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		expected.add(wt1);
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet results = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expected.size());
		when(statement.executeQuery(anyString())).thenReturn(results);
		when(results.next()).thenReturn(true, false);
		when(results.getInt("id")).thenReturn(wt1.getId());
		when(results.getString("url")).thenReturn(wt1.getURL().toString());
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expected.size() + 10); // Ask for 10 more, than received
		assertEquals(finalReturn, expected);
	}

	@Test(expected=AssertionError.class)
	public void testRetrieveWorkNegativeInt() {
		ConnectionManager connMgr = mock(ConnectionManager.class);
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		wldao.retrieveWork(-1);
	}
	
	@Test
	public void testCheckoutWork() throws SQLException, MalformedURLException {
		int expected = 3;
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expected);
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		assertEquals(wldao.checkoutWork(wt1), true);
		verify(statement).executeUpdate(anyString());
	}
	
	@Test
	public void testCheckoutWorkSQLException() throws SQLException, MalformedURLException {
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenThrow(new SQLException("MOCK SQL ERROR"));
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		assertEquals(wldao.checkoutWork(wt1), false);
		verify(statement).executeUpdate(anyString());
	}

	@Test
	public void testCheckoutWorkInvalid() throws SQLException, MalformedURLException {
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		assertFalse(wldao.checkoutWork(wt1));
	}
	
	@Test
	public void testSubmitWork() throws MalformedURLException, SQLException {
		int expected = 1;
		URL url1 = new URL("http://1.com");
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString(), anyInt())).thenReturn(expected);
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		assertEquals(wldao.submitWork(url1), expected);
	}
	
	@Test
	public void testSubmitWorkSQLException() throws MalformedURLException, SQLException {
		URL url1 = new URL("http://1.com");
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString(), anyInt())).thenThrow(new SQLException("MOCK SQL ERROR"));
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		assertEquals(wldao.submitWork(url1), -1);
	}

	@Test
	public void testSubmitWorkNull() throws MalformedURLException, SQLException {
		URL url1 = null;
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		wldao.submitWork(url1);		
	}

	@Test
	public void testRevertWork() throws SQLException {
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(1);
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		assertTrue(wldao.revertWork(42)); 
	}
	
	@Test
	public void testRevertWorkUnknownId() throws SQLException {
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(0);
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		assertFalse(wldao.revertWork(42)); 
	}
	
	@Test
	public void testRevertWorkSQLException() throws SQLException {
		int id = 1;
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenThrow(new SQLException("MOCK SQL ERROR"));
		// Run method under inspection
		WorkloadDAO wldao = new WorkloadDAO(connMgr);
		assertFalse(wldao.revertWork(id)); 
	}
}
