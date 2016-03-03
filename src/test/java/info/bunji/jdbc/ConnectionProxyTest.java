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
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author f.kinoshita
 *
 */
public class ConnectionProxyTest extends AbstractTest {

	Connection conn;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTest.setUpBeforeClass();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		conn = DriverManager.getConnection(ACCEPT_URL, "sa", "");
	}

	@After
	public void tearDown() throws Exception {
		conn.close();
	}

	@Test
	public void testGetUrl() {
		ConnectionProxy proxy = new ConnectionProxy(conn, REAL_URL);
		assertThat(proxy.url, is(REAL_URL));
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
