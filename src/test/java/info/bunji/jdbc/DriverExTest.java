package info.bunji.jdbc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author f.kinoshita
 */
public class DriverExTest extends AbstractTest {

	private static Driver driver;
	private static Properties prop = new Properties();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTest.setUpBeforeClass();

		driver = DriverManager.getDriver(ACCEPT_URL);
		prop.setProperty("user", "sa");
		prop.setProperty("password", "");
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
		assertThat(driver.connect(ACCEPT_URL, prop), is(notNullValue()));
		assertThat(driver.connect(REAL_URL, prop), is(nullValue()));
		assertThat(driver.connect(REAL_URL, new Properties()), is(nullValue()));
	}

	@Test
	public void testConnect2() throws Exception {
		Driver d = new DriverEx();
		assertThat(d.connect(ACCEPT_URL, prop), is(notNullValue()));
		assertThat(d.connect(REAL_URL, prop), is(nullValue()));
		assertThat(d.connect(REAL_URL, new Properties()), is(nullValue()));
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
