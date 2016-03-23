/**
 *
 */
package info.bunji.jdbc.logger.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import info.bunji.jdbc.logger.JdbcLogger;

/**
 * @author f.kinoshita
 *
 */
public class Log4jJdbcLoggerTest {

	private JdbcLogger logger;

	@Before
	public void beforeClass() {
		logger = new Log4jJdbcLogger("testLogger");
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Log4jJdbcLogger#isJdbcLoggingEnabled()} のためのテスト・メソッド。
	 */
	@Test
	public void testIsJdbcLoggingEnabled() {
		assertThat(logger.isJdbcLoggingEnabled(), is(true));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Log4jJdbcLogger#trace(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testTrace() {
		try {
			logger.trace("trace message");
			logger.trace(null);
		} catch(Exception e) {
			fail();
		}
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Log4jJdbcLogger#debug(java.lang.String, java.lang.Object[])} のためのテスト・メソッド。
	 */
	@Test
	public void testDebug() {
		try {
			logger.debug("debug message[{}]", "param1");
			logger.debug("debug message");
			logger.debug(null);
		} catch(Exception e) {
			fail();
		}
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Log4jJdbcLogger#info(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testInfo() {
		try {
			logger.info("info message");
			logger.info(null);
		} catch(Exception e) {
			fail();
		}
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Log4jJdbcLogger#warn(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testWarnString() {
		try {
			logger.warn("warn message");
			logger.warn(null);
		} catch(Exception e) {
			fail();
		}
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Log4jJdbcLogger#warn(java.lang.String, java.lang.Throwable)} のためのテスト・メソッド。
	 */
	@Test
	public void testWarnStringThrowable() {
		try {
			logger.warn("warn message", new Exception("testException"));
			logger.warn(null);
		} catch(Exception e) {
			fail();
		}
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Log4jJdbcLogger#error(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testErrorString() {
		try {
			logger.error("error message");
			logger.error(null);
		} catch(Exception e) {
			fail();
		}
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Log4jJdbcLogger#error(java.lang.String, java.lang.Throwable)} のためのテスト・メソッド。
	 */
	@Test
	public void testErrorStringThrowable() {
		try {
			logger.error("error message", new Exception("testException"));
			logger.error(null);
		} catch(Exception e) {
			fail();
		}
	}

	@Test
	public void testSetAcceptFilter() {
		Map<String, Object> settings = new HashMap<String, Object>();

		settings.put("acceptFilter", ".*");
		logger.setSetting(settings);

		settings.put("acceptFilter", null);
		logger.setSetting(settings);

		settings.put("acceptFilter", "");
		logger.setSetting(settings);

		settings.put("acceptFilter", "*");
		logger.setSetting(settings);
	}

	@Test
	public void testSetIgnoreFilter() {
		Map<String, Object> settings = new HashMap<String, Object>();

		settings.put("ignoreFilter", ".*");
		logger.setSetting(settings);

		settings.put("ignoreFilter", null);
		logger.setSetting(settings);

		settings.put("ignoreFilter", "");
		logger.setSetting(settings);

		settings.put("ignoreFilter", "*");
		logger.setSetting(settings);
	}
}
