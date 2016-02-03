package info.bunji.jdbc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author f.kinoshita
 */
public class DriverExTest {

	public static String acceptUrl = "jdbc:log4jdbcex:h2:~/test;SCHEMA=INFORMATION_SCHEMA";
	public static String realUrl   = "jdbc:h2:~/test;SCHEMA=INFORMATION_SCHEMA";

	private static Driver driver;
	private static Properties prop = new Properties();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		driver = DriverManager.getDriver(acceptUrl);
		prop.setProperty("user", "sa");
		prop.setProperty("password", "");

		// create InitialContext
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
		System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
		InitialContext ic = new InitialContext();
		ic.createSubcontext("java:");
		ic.createSubcontext("java:comp");
		ic.createSubcontext("java:comp/env");
		ic.createSubcontext("java:comp/env/jdbc");

		// bind DataSource
		BasicDataSource ds = new BasicDataSource();
		ds.setUrl(DriverExTest.acceptUrl);
		ds.setUsername("sa");
		ic.bind("java:comp/env/jdbc/log4jdbcDs", ds);
	}

	@Test
	public void testAcceptsURL() throws Exception {
		assertThat(driver.acceptsURL(acceptUrl), is(true));
		assertThat(driver.acceptsURL(realUrl), is(false));
	}

	@Test
	public void testAcceptsURL2() throws Exception {
		Driver d = new DriverEx();
		assertThat(d.acceptsURL(acceptUrl), is(true));
		assertThat(d.acceptsURL(realUrl), is(false));
	}

	@Test
	public void testConnect() throws Exception {
		assertThat(driver.connect(acceptUrl, prop), is(notNullValue()));
		assertThat(driver.connect(realUrl, prop), is(nullValue()));
		assertThat(driver.connect(realUrl, new Properties()), is(nullValue()));
	}

	@Test
	public void testConnect2() throws Exception {
		Driver d = new DriverEx();
		assertThat(d.connect(acceptUrl, prop), is(notNullValue()));
		assertThat(d.connect(realUrl, prop), is(nullValue()));
		assertThat(d.connect(realUrl, new Properties()), is(nullValue()));
	}

	@Test
	public void testGetMajorVersion() {
		Driver d = new DriverEx();
		assertThat(d.getMajorVersion(), is(1));
		Driver h2 = new org.h2.Driver();
		assertThat(driver.getMajorVersion(), is(h2.getMajorVersion()));
	}

	@Test
	public void testGetMinorVersion() {
		Driver d = new DriverEx();
		assertThat(d.getMinorVersion(), is(0));
		Driver h2 = new org.h2.Driver();
		assertThat(driver.getMinorVersion(), is(h2.getMinorVersion()));
	}

	@Test
	public void testGetPropertyInfo() throws Exception {
		Driver realDriver = DriverManager.getDriver(realUrl);
		realDriver.getPropertyInfo(realUrl, prop);
		assertThat(driver.getPropertyInfo(acceptUrl, prop), is(realDriver.getPropertyInfo(realUrl, prop)));

		Driver d = new DriverEx();
		assertThat(d.getPropertyInfo("", prop), is(new DriverPropertyInfo[0]));
	}

	@Test
	public void testJdbcCompliant() {
		Driver d = new DriverEx();
		assertThat(d.jdbcCompliant(), is(false));
		Driver h2 = new org.h2.Driver();
		assertThat(driver.jdbcCompliant(), is(h2.jdbcCompliant()));
	}

//	@Test
//	public void testGetParentLogger() throws Exception {
//		Logger logger = driver.getParentLogger();
//		Driver h2 = new org.h2.Driver();
//		Logger h2Logger = h2.getParentLogger();
//		assertThat(logger, is(h2Logger));
//	}

//	@Test
//	public void testGetParentLogger2() {
//		Driver d = new DriverEx();
//		try {
//			d.getParentLogger();
//			fail();
//		} catch (Exception e) {
//			assertThat(e, instanceOf(SQLFeatureNotSupportedException.class));
//		}
//	}
}
