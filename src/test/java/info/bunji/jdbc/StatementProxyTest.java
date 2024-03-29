/**
 *
 */
package info.bunji.jdbc;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author f.kinoshita
 *
 */
public class StatementProxyTest extends AbstractTest {

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
		conn.close();
		super.tearDown();
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testCreateStatement() throws Exception {
		Statement  stmt = conn.createStatement();
		assertThat(stmt.executeQuery("SELECT * from test"), is(notNullValue()));
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testPrepareStatement() throws Exception {
		PreparedStatement stmt = conn.prepareStatement("SELECT * from test");
		stmt.clearParameters();
		assertThat(stmt, is(notNullValue()));
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testPrepareCall() throws Exception {
		CallableStatement stmt = conn.prepareCall("call proctest(?, ?)");
		assertThat(stmt, is(notNullValue()));

		stmt.setString(1, "TestString");
		stmt.setInt(2, 4);
		//stmt.setString("P1", "TestString");
		//stmt.setInt("P2", 4);

		ResultSet rs = stmt.executeQuery();
		assertThat(rs.next(), is(true));
		assertThat(rs.getString(1), is("Test"));

		stmt.close();
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testRegisterOutParameter() throws Exception {
		CallableStatement stmt = conn.prepareCall("{CALL testCall(?, ?, ?, ?)}");
		stmt.setInt(1, 50);
		stmt.setString(2, "abc");
		long t = System.currentTimeMillis();
		stmt.setTimestamp(3, new Timestamp(t));
		stmt.setTimestamp(4, Timestamp.valueOf("2001-02-03 10:20:30.0"));
		stmt.registerOutParameter(1, Types.INTEGER);
		stmt.registerOutParameter(2, Types.VARCHAR);
		stmt.executeUpdate();
		try {
			stmt.getTimestamp(3);
			fail("not registered out parameter accessible");
		} catch (SQLException e) {
			// expected exception
		}
		stmt.registerOutParameter(3, Types.TIMESTAMP);
		stmt.registerOutParameter(4, Types.TIMESTAMP);
		stmt.executeUpdate();

		assertEquals(t + 1, stmt.getTimestamp(3).getTime());
		//assertEquals(t + 1, stmt.getTimestamp("C").getTime());

		assertEquals("2001-02-03 10:20:30.0", stmt.getTimestamp(4).toString());
		//assertEquals("2001-02-03 10:20:30.0", stmt.getTimestamp("D").toString());
		assertEquals("10:20:30", stmt.getTime(4).toString());
		//assertEquals("10:20:30", stmt.getTime("D").toString());
		assertEquals("2001-02-03", stmt.getDate(4).toString());
		//assertEquals("2001-02-03", stmt.getDate("D").toString());
	}

/* カラム名によるプロシージャ対応が必要
	@Test
	public void testRegisterOutParameter() throws Exception {
		CallableStatement stmt = conn.prepareCall("{CALL testCall(?, ?, ?, ?)}");
		stmt.setInt("A", 50);
		stmt.setString("B", "abc");
		long t = System.currentTimeMillis();
		stmt.setTimestamp("C", new Timestamp(t));
		stmt.setTimestamp("D", Timestamp.valueOf("2001-02-03 10:20:30.0"));
		stmt.registerOutParameter(1, Types.INTEGER);
		stmt.registerOutParameter("B", Types.VARCHAR);
		stmt.executeUpdate();
		try {
			stmt.getTimestamp("C");
			fail("not registered out parameter accessible");
		} catch (SQLException e) {
			// expected exception
		}
		stmt.registerOutParameter(3, Types.TIMESTAMP);
		stmt.registerOutParameter(4, Types.TIMESTAMP);
		stmt.executeUpdate();

		assertEquals(t + 1, stmt.getTimestamp(3).getTime());
		assertEquals(t + 1, stmt.getTimestamp("C").getTime());

		assertEquals("2001-02-03 10:20:30.0", stmt.getTimestamp(4).toString());
		assertEquals("2001-02-03 10:20:30.0", stmt.getTimestamp("D").toString());
		assertEquals("10:20:30", stmt.getTime(4).toString());
		assertEquals("10:20:30", stmt.getTime("D").toString());
		assertEquals("2001-02-03", stmt.getDate(4).toString());
		assertEquals("2001-02-03", stmt.getDate("D").toString());
	}
*/
	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testAddBatchString() throws Exception {
		Statement stmt = conn.createStatement();
		stmt.addBatch("SELECT * from test");
		assertTrue(true);
	}

	/**
	 * @throws Exception 意図しない例外
	 */
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

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testAddBatchExecute2() throws Exception {
		PreparedStatement stmt = conn.prepareStatement("INSERT into test values(?)");
		stmt.setString(1, "'ccc");
		stmt.addBatch();
		//stmt.setString(1, "ddd");
		stmt.setNull(1, Types.VARCHAR);
		stmt.addBatch();
		//stmt.addBatch("INSERT into test values('ccc')");
		int[] ret = stmt.executeBatch();
		int[] val = { 1, 1 };
		assertThat(ret, is(equalTo(val)));
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test(expected=BatchUpdateException.class)
	public void testAddBatchExecute3() throws Exception {
		Statement stmt = conn.createStatement();
		stmt.clearBatch();
		stmt.addBatch("INSERT into test values('aaa')");
		stmt.addBatch("INSERT into unknown values('bbb')");
		stmt.addBatch("INSERT into test values('bbb')");
		stmt.executeBatch();
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testIsClosed() throws Exception {
		Statement stmt = conn.createStatement();
		assertThat(stmt.isClosed(), is(false));
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testGetConnection() throws Exception {
		Statement stmt = conn.createStatement();
		assertThat(stmt.getConnection(), is(notNullValue()));
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test
	public void testSetString() throws Exception {
		PreparedStatement stmt = conn.prepareStatement("SELECT * from test where aaa=?");
		stmt.setString(1, "123");
		assertThat(stmt.executeQuery(), is(notNullValue()));
	}

	/**
	 * @throws Exception 意図しない例外
	 */
	@Test(expected=SQLException.class)
	public void testException() throws Exception {
		Statement stmt = conn.createStatement();
		stmt.executeQuery("SELECT * from bbb");
		fail();
	}
}
