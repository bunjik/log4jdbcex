/**
 *
 */
package info.bunji.jdbc;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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

	/**
	 * @throws Exception 意図しない例外
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTest.setUpBeforeClass();
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		conn = DriverManager.getConnection(ACCEPT_URL, "sa", "");
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		conn.close();
	}

	@Test
	public void testGetUrl() {
		ConnectionProxy proxy = new ConnectionProxy(conn, REAL_URL, "testId");
		assertThat(proxy.getUrl(), is(REAL_URL));
		assertThat(proxy.getConnectionId(), is("testId"));
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testCreateStatement() throws Exception {
		Statement stmt = conn.createStatement();
		assertThat(stmt, is(notNullValue()));
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testPreparedStatement() throws Exception {
		assertThat(conn.prepareStatement("select * from test"), is(notNullValue()));
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testPrepareCall() throws Exception {
		assertThat(conn.prepareCall("select * from test"), is(notNullValue()));
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test(expected=SQLException.class)
	public void testInvocationWithException() throws Exception {
		conn.prepareStatement("insert into test(1,2,3)" ,99);
	}
}
