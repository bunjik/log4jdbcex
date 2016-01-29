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
import org.junit.Before;
import org.junit.Test;

/**
 * @author bunji
 *
 */
public class ConnectionProxyTest {

	Connection conn;

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
		assertThat(proxy.getUrl(), is(DriverExTest.realUrl));
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
