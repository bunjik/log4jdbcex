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
import info.bunji.jdbc.specifics.DefaultRdbmsSpecifics;
import info.bunji.jdbc.specifics.OracleRdbmsSpecifics;

/**
 * @author f.kinoshita
 *
 */
public class AbstractJdbcLoggerTest {

	private JdbcLogger logger;

	@Before
	public void beforeClass() {
		logger = new TestJdbcLogger("testLogger");
	}

//	/**
//	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#AbstractJdbcLogger(java.lang.String)} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testAbstractJdbcLogger() {
//		fail("まだ実装されていません");
//	}

//	/**
//	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#makeLoggerName(java.lang.String)} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testMakeLoggerName() {
//	}

//	/**
//	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#isConnectionLogging()} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testIsConnectionLogging() {
//		fail("まだ実装されていません");
//	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#getSpecifics()} のためのテスト・メソッド。
	 */
	@Test
	public void testGetSpecifics() {
		JdbcLogger logger1 = new TestJdbcLogger("jdbc:oracle:test");
		assertThat(logger1.getSpecifics(), is(instanceOf(OracleRdbmsSpecifics.class)));

		JdbcLogger logger2 = new TestJdbcLogger("jdbc:h2:test");
		assertThat(logger2.getSpecifics(), is(instanceOf(DefaultRdbmsSpecifics.class)));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#getConnectUrl()} のためのテスト・メソッド。
	 */
	@Test
	public void testGetConnectUrl() {
		assertThat(logger.getConnectUrl(), is("testLogger"));
	}

//	/**
//	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#addExecStatement(info.bunji.jdbc.LoggerHelper)} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testAddExecStatement() {
//		fail("まだ実装されていません");
//	}
//
//	/**
//	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#removeExecStatement(info.bunji.jdbc.LoggerHelper)} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testRemoveExecStatement() {
//		fail("まだ実装されていません");
//	}
//
//	/**
//	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#isLogging(java.lang.String, long)} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testIsLogging() {
//		fail("まだ実装されていません");
//	}
//
//	/**
//	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#reportReturned(info.bunji.jdbc.LoggerHelper, java.lang.Object[])} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testReportReturned() {
//		fail("まだ実装されていません");
//	}
//
//	/**
//	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#reportException(info.bunji.jdbc.LoggerHelper, java.lang.Throwable, java.lang.Object[])} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testReportException() {
//		fail("まだ実装されていません");
//	}
//
//	/**
//	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#getRunningQueries()} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testGetRunningQueries() {
//		fail("まだ実装されていません");
//	}
//
//	/**
//	 * {@link info.bunji.jdbc.logger.impl.AbstractJdbcLogger#getHistory()} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testGetHistory() {
//		fail("まだ実装されていません");
//	}

	@Test
	public void testHistoryCount() {
		Map<String, Object> settings = new HashMap<String, Object>();

		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("historyCount"), is(50));

		settings.put("historyCount", -10);
		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("historyCount"), is(0));

		settings.put("historyCount", 10);
		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("historyCount"), is(10));

		settings.put("historyCount", null);
		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("historyCount"), is(10));

		settings.put("historyCount", "20");
		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("historyCount"), is(20));

		settings.put("historyCount", "aaa");
		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("historyCount"), is(20));
	}

	@Test
	public void testLimitLength() {
		Map<String, Object> settings = new HashMap<String, Object>();

		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("limitLength"), is(-1));

		settings.put("limitLength", -10);
		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("limitLength"), is(-1));

		settings.put("limitLength", 10);
		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("limitLength"), is(10));

		settings.put("limitLength", null);
		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("limitLength"), is(10));

		settings.put("limitLength", "20");
		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("limitLength"), is(20));

		settings.put("limitLength", "aaa");
		logger.setSetting(settings);
		assertThat((Integer) logger.getSetting().get("limitLength"), is(20));
	}

	@Test
	public void testAcceptFilter() {
		Map<String, Object> settings = new HashMap<String, Object>();

		settings.put("acceptFilter", ".*");
		logger.setSetting(settings);
		assertThat((String) logger.getSetting().get("acceptFilter"), is(".*"));

		settings.put("acceptFilter", null);
		logger.setSetting(settings);
		assertThat((String) logger.getSetting().get("acceptFilter"), is(nullValue()));

		settings.put("acceptFilter", "");
		logger.setSetting(settings);
		assertThat((String) logger.getSetting().get("acceptFilter"), is(nullValue()));

		settings.put("acceptFilter", "*");
		logger.setSetting(settings);
		assertThat((String) logger.getSetting().get("acceptFilter"), is(nullValue()));
	}

	@Test
	public void testIgnoreFilter() {
		Map<String, Object> settings = new HashMap<String, Object>();

		settings.put("ignoreFilter", ".*");
		logger.setSetting(settings);
		assertThat((String) logger.getSetting().get("ignoreFilter"), is(".*"));

		settings.put("ignoreFilter", null);
		logger.setSetting(settings);
		assertThat((String) logger.getSetting().get("ignoreFilter"), is(nullValue()));

		settings.put("ignoreFilter", "");
		logger.setSetting(settings);
		assertThat((String) logger.getSetting().get("ignoreFilter"), is(nullValue()));

		settings.put("ignoreFilter", "*");
		logger.setSetting(settings);
		assertThat((String) logger.getSetting().get("ignoreFilter"), is(nullValue()));
	}

	// for Test
	class TestJdbcLogger extends AbstractJdbcLogger {
		public TestJdbcLogger(String url) { super(url); }

		@Override
		public boolean isJdbcLoggingEnabled() { return true; }

		@Override
		public void trace(String msg) {}

		@Override
		public void debug(String msg, Object... args) {}

		@Override
		public void info(String msg) {}

		@Override
		public void warn(String msg) {}

		@Override
		public void warn(String msg, Throwable t) {}

		@Override
		public void error(String msg) {}

		@Override
		public void error(String msg, Throwable t) {}
	}

}
