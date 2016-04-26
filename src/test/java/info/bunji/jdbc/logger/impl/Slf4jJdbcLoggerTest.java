/**
 *
 */
package info.bunji.jdbc.logger.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import info.bunji.jdbc.logger.JdbcLogger;

/**
 * @author f.kinoshita
 *
 */
public class Slf4jJdbcLoggerTest {

	private JdbcLogger logger;

	@Before
	public void beforeClass() {
		logger = new Slf4jJdbcLogger("testLogger");
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Slf4jJdbcLogger#isJdbcLoggingEnabled()} のためのテスト・メソッド。
	 */
	@Test
	public void testIsJdbcLoggingEnabled() {
		assertThat(logger.isJdbcLoggingEnabled(), is(true));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Slf4jJdbcLogger#trace(java.lang.String)} のためのテスト・メソッド。
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
	 * {@link info.bunji.jdbc.logger.impl.Slf4jJdbcLogger#debug(java.lang.String, java.lang.Object[])} のためのテスト・メソッド。
	 */
	@Test
	public void testDebug() {
		try {
			logger.debug("debug message[%s]", "param1");
			logger.debug("debug message");
			logger.debug(null);
		} catch(Exception e) {
			fail();
		}
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Slf4jJdbcLogger#info(java.lang.String)} のためのテスト・メソッド。
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
	 * {@link info.bunji.jdbc.logger.impl.Slf4jJdbcLogger#warn(java.lang.String)} のためのテスト・メソッド。
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
	 * {@link info.bunji.jdbc.logger.impl.Slf4jJdbcLogger#warn(java.lang.String, java.lang.Throwable)} のためのテスト・メソッド。
	 */
	@Test
	public void testWarnStringThrowable() {
		try {
			Throwable t = new Exception("testException");
			t.setStackTrace(new StackTraceElement[0]);
			logger.warn("warn message", t);
			logger.warn(null);
		} catch(Exception e) {
			fail();
		}
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.Slf4jJdbcLogger#error(java.lang.String)} のためのテスト・メソッド。
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
	 * {@link info.bunji.jdbc.logger.impl.Slf4jJdbcLogger#error(java.lang.String, java.lang.Throwable)} のためのテスト・メソッド。
	 */
	@Test
	public void testErrorStringThrowable() {
		try {
			Throwable t = new Exception("testException");
			t.setStackTrace(new StackTraceElement[0]);
			logger.error("error message", t);
			logger.error(null);
		} catch(Exception e) {
			fail();
		}
	}
}
