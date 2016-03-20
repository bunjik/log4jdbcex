/**
 *
 */
package info.bunji.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.BeforeClass;

/**
 * @author f.kinoshita
 *
 */
public abstract class AbstractTest {

	public static String ACCEPT_URL = "jdbc:log4jdbcex:h2:mem:test;DB_CLOSE_DELAY=-1";
	public static String REAL_URL   = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

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

			// regist procedure
			stmt.execute("drop alias if exists proctest");
			StringBuilder proc = new StringBuilder();
			proc.append("create alias proctest as $$")
				.append("String proctest(String str, int len) {")
				.append(" if(str != null && str.length() < len) {")
				.append("   return str;")
				.append(" }")
				.append(" return str.substring(0, len);")
				.append("}$$");
			stmt.execute(proc.toString());

			proc = new StringBuilder();
			proc.append("create alias if not exists waitfunc as ")
				.append("$$String waitfunc(String str) throws Exception { Thread.sleep(2000); return str;}$$");
			stmt.execute(proc.toString());

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
}
