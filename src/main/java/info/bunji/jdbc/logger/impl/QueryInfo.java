package info.bunji.jdbc.logger.impl;

public class QueryInfo implements Cloneable {
	private Long time;
	private Long elapsed;
	private String sql;
	private String queryId = null;
	private boolean isError = false;

	public QueryInfo(Long time, Long elapsed, String sql) {
		this(time, elapsed, sql, null);
	}

	public QueryInfo(Long time, Long elapsed, String sql, boolean isError) {
		this(time, elapsed, sql, null);
		this.isError = isError;
	}

	public QueryInfo(Long time, Long elapsed, String sql, String queryId) {
		this.time = time;
		this.elapsed = elapsed;
		this.sql = sql;
		this.queryId = queryId;
	}

	public Long getTime() { return time; }
	public Long getElapsed() { return elapsed; }
	public String getSql() { return sql; }
	public String getId() { return queryId; }
	public boolean isError() { return isError; }
}