/**
 *
 */
package info.bunji.jdbc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author bunji
 *
 */
public class StatementProxyTest {

	Connection conn;

	@BeforeClass
	public static void setUpClass() throws Exception {
		Connection c = DriverManager.getConnection(DriverExTest.acceptUrl, "sa", "");
		c.createStatement().execute("drop table if exists test");
		c.createStatement().execute("create table test(aaa varchar(10))");
		c.close();
	}


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		conn = DriverManager.getConnection(DriverExTest.acceptUrl, "sa", "");
	}

	@After
	public void tearDown() throws Exception {
		conn.close();
	}

	/**
	 * {@link info.bunji.jdbc.StatementProxy#StatementProxy()} のためのテスト・メソッド。
	 */
	@Test
	public void testStatementProxyStatement() throws Exception {
		Statement  stmt = conn.createStatement();
		assertThat(stmt.executeQuery("SELECT * from test"), is(notNullValue()));
	}

	/**
	 * {@link info.bunji.jdbc.StatementProxy#StatementProxy(java.sql.Statement, java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testStatementProxyStatementString() throws Exception {
		PreparedStatement stmt = conn.prepareStatement("SELECT * from test");
		stmt.clearParameters();
		assertThat(stmt, is(notNullValue()));
	}

	@Test
	public void testAddBatchString() throws Exception {
		Statement stmt = conn.createStatement();
		stmt.addBatch("SELECT * from test");
		assertTrue(true);
	}

	@Test
	public void testAddBatchExecute() throws Exception {
		Statement stmt = conn.createStatement();
		stmt.clearBatch();
		stmt.addBatch("INSERT into test values('aaa')");
		stmt.addBatch("INSERT into test values('bbb')");
		int[] ret = stmt.executeBatch();
		int[] val = { 1, 1 };
		assertThat(ret, is(equalTo(val)));
	}

	@Test
	public void testAddBatchExecute2() throws Exception {
		PreparedStatement stmt = conn.prepareStatement("INSERT into test values(?)");
		stmt.setString(1, "ccc");
		stmt.addBatch();
		//stmt.setString(1, "ddd");
		stmt.setNull(1, Types.VARCHAR);
		stmt.addBatch();
		//stmt.addBatch("INSERT into test values('ccc')");
		int[] ret = stmt.executeBatch();
		int[] val = { 1, 1 };
		assertThat(ret, is(equalTo(val)));
	}

	@Test
	public void testIsClosed() throws Exception {
		Statement stmt = conn.createStatement();
		assertThat(stmt.isClosed(), is(false));
	}

	@Test
	public void testGetConnection() throws Exception {
		Statement stmt = conn.createStatement();
		assertThat(stmt.getConnection(), is(notNullValue()));
	}

	@Test
	public void testSetString() throws Exception {
		PreparedStatement stmt = conn.prepareStatement("SELECT * from test where aaa=?");
		stmt.setString(1, "123");
		assertThat(stmt.executeQuery(), is(notNullValue()));
	}

	/**
	 * {@link info.bunji.jdbc.StatementProxy#StatementProxy()} のためのテスト・メソッド。
	 */
	@Test
	public void testException() throws Exception {
		try {
			Statement stmt = conn.createStatement();
			stmt.executeQuery("SELECT * from bbb");
			fail();
		} catch (SQLException e){
			assertThat(e, instanceOf(SQLException.class));
		}
	}

}
