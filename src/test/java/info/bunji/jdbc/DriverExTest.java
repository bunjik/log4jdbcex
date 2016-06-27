package info.bunji.jdbc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import info.bunji.jdbc.logger.JdbcLoggerFactory;

/**
 * @author f.kinoshita
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JdbcLoggerFactory.class})
public class DriverExTest extends AbstractTest {

	private static Driver driver;
	private static Properties prop = new Properties();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTest.setUpBeforeClass();

		driver = DriverManager.getDriver(ACCEPT_URL);
		prop.setProperty("user", "sa");
		prop.setProperty("password", "");
		prop.setProperty("logging.connectionLogging", "true");
	}

	@Test
	public void testAcceptsURL() throws Exception {
		assertThat(driver.acceptsURL(ACCEPT_URL), is(true));
		assertThat(driver.acceptsURL(REAL_URL), is(false));
	}

	@Test
	public void testAcceptsURL2() throws Exception {
		Driver d = new DriverEx();
		assertThat(d.acceptsURL(ACCEPT_URL), is(true));
		assertThat(d.acceptsURL(REAL_URL), is(false));
	}

	@Test
	public void testConnect() throws Exception {
		assertThat(driver.connect(null, prop), is(nullValue()));
		assertThat(driver.connect(ACCEPT_URL, prop), is(notNullValue()));
		assertThat(driver.connect(REAL_URL, prop), is(nullValue()));
		assertThat(driver.connect(REAL_URL, new Properties()), is(nullValue()));
	}

	@Test
	public void testConnect2() throws Exception {
		Driver d = new DriverEx();
		assertThat(d.connect(null, prop), is(nullValue()));
		assertThat(d.connect(ACCEPT_URL, prop), is(notNullValue()));
		assertThat(d.connect(REAL_URL, prop), is(nullValue()));
		assertThat(d.connect(REAL_URL, new Properties()), is(nullValue()));
	}

//	@Test
//	public void testConnect3() throws Exception {
//		PowerMockito.spy(JdbcLoggerFactory.class);
//		JdbcLogger mockLogger = spy(JdbcLogger.class);
//
//		Field field = PowerMockito.field(JdbcLoggerFactory.class, "loggerCache");
//
//		Mockito.when(mockLogger.isJdbcLoggingEnabled()).thenReturn(false);
//		PowerMockito.when(JdbcLoggerFactory.getLogger(ACCEPT_URL)).thenReturn(mockLogger);
//
//		assertThat(driver.connect(ACCEPT_URL, prop), is(notNullValue()));
//	}

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
		Driver realDriver = DriverManager.getDriver(REAL_URL);
		realDriver.getPropertyInfo(REAL_URL, prop);
		assertThat(driver.getPropertyInfo(ACCEPT_URL, prop), is(realDriver.getPropertyInfo(REAL_URL, prop)));

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
//		org.h2.Driver h2 = new org.h2.Driver();
//		Logger h2Logger = h2.getParentLogger();
//		assertThat(logger, is(h2Logger));
//	}

	// for JDBC 4.1
	@Test(expected=SQLFeatureNotSupportedException.class)
	public void testGetParentLogger2() throws Exception {
		DriverEx d = new DriverEx();
		d.getParentLogger();
		fail();
	}
}
