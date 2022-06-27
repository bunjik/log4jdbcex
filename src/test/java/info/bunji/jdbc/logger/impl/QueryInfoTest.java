/**
 *
 */
package info.bunji.jdbc.logger.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 * @author f.kinoshita
 *
 */
public class QueryInfoTest {

//	/**
//	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#QueryInfo(info.bunji.jdbc.LoggerHelper, java.lang.String)} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testQueryInfoLoggerHelperString() {
//		fail("まだ実装されていません");
//	}

//	/**
//	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#QueryInfo(info.bunji.jdbc.LoggerHelper, java.lang.String, java.lang.Throwable)} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testQueryInfoLoggerHelperStringThrowable() {
//		fail("まだ実装されていません");
//	}

//	/**
//	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#QueryInfo(java.lang.Long, java.lang.Long, java.lang.String, java.lang.String, java.lang.Throwable)} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testQueryInfoLongLongStringStringThrowable() {
//		fail("まだ実装されていません");
//	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#setSql(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testSetSql() {
		QueryInfo qi = new QueryInfo(0L, 10L, "select 1", "12345", null);

		assertThat(qi.getSql(), is("select 1"));
		qi.setSql("test");
		assertThat(qi.getSql(), is("test"));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#setHost(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testSetHost() {
		QueryInfo qi = new QueryInfo(0L, 10L, "select 1", "12345", null);

		assertThat(qi.getHost(), is(nullValue()));
		qi.setHost("host1");
		assertThat(qi.getHost(), is("host1"));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#setDataSource(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testSetDataSource() {
		QueryInfo qi = new QueryInfo(0L, 10L, "select 1", "12345", null);

		assertThat(qi.getDataSource(), is(nullValue()));
		qi.setDataSource("ds1");
		assertThat(qi.getDataSource(), is("ds1"));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#getTime()} のためのテスト・メソッド。
	 */
	@Test
	public void testGetTime() {
		QueryInfo qi = new QueryInfo(0L, 10L, "select 1", "12345", null);

		assertThat(qi.getTime(), is(0L));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#getElapsed()} のためのテスト・メソッド。
	 */
	@Test
	public void testGetElapsed() {
		QueryInfo qi = new QueryInfo(0L, 10L, "select 1", "12345", null);

		assertThat(qi.getElapsed(), is(10L));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#getSql()} のためのテスト・メソッド。
	 */
	@Test
	public void testGetSql() {
		QueryInfo qi = new QueryInfo(0L, 10L, "select 1", "12345", null);

		assertThat(qi.getSql(), is("select 1"));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#getId()} のためのテスト・メソッド。
	 */
	@Test
	public void testGetId() {
		QueryInfo qi = new QueryInfo(0L, 10L, "select 1", "12345", null);

		assertThat(qi.getId(), is("12345"));
	}

//	/**
//	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#getHost()} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testGetHost() {
//		QueryInfo qi = new QueryInfo(0L, 10L, "select 1", "12345", null);
//
//		assertThat(qi.getHost(), is(nullValue()));
//		qi.setHost("host1");
//		assertThat(qi.getHost(), is("host1"));
//	}

//	/**
//	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#getDataSource()} のためのテスト・メソッド。
//	 */
//	@Test
//	public void testGetDataSource() {
//		QueryInfo qi = new QueryInfo(0L, 10L, "select 1", "12345", null);
//
//		assertThat(qi.getDataSource(), is(nullValue()));
//		qi.setHost("ds1");
//		assertThat(qi.getDataSource(), is("ds1"));
//	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#isError()} のためのテスト・メソッド。
	 */
	@Test
	public void testIsError() {
		QueryInfo qi1 = new QueryInfo(0L, 10L, "select 1", "12345", null);
		assertThat(qi1.isError(), is(false));

		QueryInfo qi2 = new QueryInfo(0L, 10L, "select 1", "12345", new Exception("error"));
		assertThat(qi2.isError(), is(true));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#getErrorMsg()} のためのテスト・メソッド。
	 */
	@Test
	public void testGetErrorMsg() {
		QueryInfo qi1 = new QueryInfo(0L, 10L, "select 1", "12345", null);
		assertThat(qi1.getErrorMsg(), is(nullValue()));

		QueryInfo qi2 = new QueryInfo(0L, 10L, "select 1", "12345", new Exception("error"));
		assertThat(qi2.getErrorMsg(), is("error"));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#compareTo(info.bunji.jdbc.logger.impl.QueryInfo)} のためのテスト・メソッド。
	 */
	@Test
	public void testCompareTo() {
		QueryInfo qi1 = new QueryInfo(0L, 10L, "select 1", "12345", null);
		QueryInfo qi2 = new QueryInfo(0L, 10L, "select 1", "12345", null);

		assertThat(qi1.compareTo(qi2), is(0));

		QueryInfo qi3 = new QueryInfo(0L, 10L, "select 1", "54321", null);

		assertThat(qi1.compareTo(qi3), is(not(0)));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#equals(java.lang.Object)} のためのテスト・メソッド。
	 */
	@Test
	public void testEqualsObject() {
		QueryInfo qi1 = new QueryInfo(0L, 10L, "select 1", "12345", null);
		QueryInfo qi2 = new QueryInfo(0L, 10L, "select 1", "12345", null);
		assertThat(qi1.equals(qi2), is(true));

		QueryInfo qi3 = new QueryInfo(0L, 10L, "select 1", "54321", null);
		assertThat(qi1.equals(qi3), is(false));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#clone()} のためのテスト・メソッド。
	 */
	@Test
	public void testClone() {
		QueryInfo qi1 = new QueryInfo(0L, 10L, "select 1", "12345", null);
		QueryInfo qi2 = qi1.clone();

		assertThat(qi1.equals(qi2), is(true));

		assertThat(qi1 == qi2, is(false));
	}

	/**
	 * {@link info.bunji.jdbc.logger.impl.QueryInfo#hashCode()} のためのテスト・メソッド。
	 */
	@Test
	public void testHashCode() {
		QueryInfo qi1 = new QueryInfo(0L, 10L, "select 1", "12345", null);
		QueryInfo qi2 = new QueryInfo(0L, 10L, "select 1", "12345", null);

		int hash1 = qi1.hashCode();
		int hash2 = qi2.hashCode();

		assertThat(hash1, is(hash2));

		QueryInfo qi3 = new QueryInfo(0L, 10L, "select 1", "54321", null);
		int hash3 = qi3.hashCode();

		assertThat(hash1, is(not(hash3)));

	}
}
