/**
 *
 */
package info.bunji.jdbc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author f.kinoshita
 *
 */
public class ConnectionProxyTest {

	Connection conn;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Connection c = DriverManager.getConnection(DriverExTest.acceptUrl, "sa", "");
		c.createStatement().execute("drop table if exists test");
		c.createStatement().execute("create table test(aaa varchar(10))");
		c.close();
	}

	@AfterClass
	public static void setUpAfterClass() throws Exception {
		Connection c = DriverManager.getConnection(DriverExTest.acceptUrl, "sa", "");
		c.createStatement().execute("drop table if exists test");
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

	@Test
	public void testGetUrl() {
		ConnectionProxy proxy = new ConnectionProxy(conn, DriverExTest.realUrl);
		assertThat(proxy.url, is(DriverExTest.realUrl));
	}

	@Test
	public void testCreateStatement() throws Exception {
		assertThat(conn.createStatement(), is(notNullValue()));
	}

	@Test
	public void testPreparedStatement() throws Exception {
		assertThat(conn.prepareStatement("select * from test"), is(notNullValue()));
	}

	@Test
	public void testPrepareCall() throws Exception {
		assertThat(conn.prepareCall("select * from test"), is(notNullValue()));
	}

	@Test(expected=SQLException.class)
	public void testInvocationWithException() throws Exception {
		conn.prepareStatement("insert into test(1,2,3)" ,99);
	}
}
