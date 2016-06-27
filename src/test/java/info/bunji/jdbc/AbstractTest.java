/**
 *
 */
package info.bunji.jdbc;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.h2.tools.SimpleResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * @author f.kinoshita
 *
 */
public abstract class AbstractTest {

	public static String ACCEPT_URL = "jdbc:log4jdbcex:h2:mem:test;DB_CLOSE_DELAY=-1";
	public static String REAL_URL   = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp() throws Exception {
		System.out.println("== begin " + testName.getMethodName() +" ==");
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("== end " + testName.getMethodName() +" ==");
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// create InitialContext
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
		System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
		InitialContext ic = new InitialContext();

		try {
			try {
				Class.forName("org.h2.Driver");
				Class.forName("info.bunji.jdbc.DriverEx");
			} catch(Exception e) {}

			ic.lookup("java:comp/env/jdbc");

			// already initialized.

		} catch (NamingException ne) {
			ic.createSubcontext("java:");
			ic.createSubcontext("java:comp");
			ic.createSubcontext("java:comp/env");
			ic.createSubcontext("java:comp/env/jdbc");

			// bind DataSource
			BasicDataSource ds = new BasicDataSource();
			ds.setUrl(ACCEPT_URL);
			ds.setUsername("sa");
			ic.bind("java:comp/env/jdbc/log4jdbcDs", ds);

			// init database
			Connection c = DriverManager.getConnection(REAL_URL, "sa", "");
			Statement stmt = c.createStatement();

			stmt.execute("drop table if exists test");
			stmt.execute("create table test(aaa varchar(10))");
			stmt.execute("insert into test values('sample')");

			// regist procedures
			stmt.execute("create alias if not exists procTest FOR \"" +
					AbstractTest.class.getName() + ".procTest\"");

			stmt.execute("create alias if not exists waitfunc FOR \"" +
					AbstractTest.class.getName() + ".waitfunc\"");

			stmt.execute("create alias if not exists testCall FOR \"" +
					AbstractTest.class.getName() + ".testCall\"");

			c.close();
		}
	}

	protected Connection getConnection(String name) throws Exception {
		String prefix = "java:comp/env/jdbc/";
		InitialContext ctx = new InitialContext();
		DataSource ds = (DataSource) ctx.lookup(prefix + name);
		return ds.getConnection();
	}

	protected void closeQuietly(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			// do nothing.
		}
	}

	protected <T> T newInstance(Class<T> clazz, Object... args) throws Exception {
		List<Class<?>> argTypes = new ArrayList<Class<?>>();
		for (Object arg : args) {
			if (arg != null) {
				argTypes.add(arg.getClass());
			} else {
				argTypes.add(Object.class);
			}
		}

		Constructor<T> constructor = clazz.getDeclaredConstructor(argTypes.toArray(new Class[0]));
		constructor.setAccessible(true);

		return constructor.newInstance(args);
	}

	public static String procTest(String str, int len) throws SQLException {
		if(str != null && str.length() < len) {
		   return str;
		}
		return str.substring(0, len);

	}

	public static String waitfunc(String str) throws Exception {
		Thread.sleep(2000);
		return str;
	}

	/**
	 * This method is called via reflection from the database.
	 *
	 * @param conn the connection
	 * @param a the value a
	 * @param b the value b
	 * @param c the value c
	 * @param d the value d
	 * @return a result set
	 */
	public static ResultSet testCall(Connection conn, int a, String b, Timestamp c, Timestamp d) throws SQLException {
		SimpleResultSet rs = new SimpleResultSet();
		rs.addColumn("A", Types.INTEGER, 0, 0);
		rs.addColumn("B", Types.VARCHAR, 0, 0);
		rs.addColumn("C", Types.TIMESTAMP, 0, 0);
		rs.addColumn("D", Types.TIMESTAMP, 0, 0);
		if ("jdbc:columnlist:connection".equals(conn.getMetaData().getURL())) {
			return rs;
		}
		rs.addRow(a * 2, b.toUpperCase(), new Timestamp(c.getTime() + 1), d);
		return rs;
    }
}
