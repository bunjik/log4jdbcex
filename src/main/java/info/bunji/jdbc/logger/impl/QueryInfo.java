package info.bunji.jdbc.logger.impl;

public class QueryInfo implements Cloneable, Comparable<QueryInfo> {
	private Long time;
	private Long elapsed;
	private String sql;
	private String queryId = null;
	private String host = null;
	private String dataSource = null;
	private boolean isError = false;
	private String errorMsg = null;

	QueryInfo() {
		// do nothing.
	}

	QueryInfo(Long time, Long elapsed, String sql) {
		this(time, elapsed, sql, (String) null);
	}

	QueryInfo(Long time, Long elapsed, String sql, Throwable t) {
		this(time, elapsed, sql);
		if (t != null) {
			this.isError = true;
			this.errorMsg = t.getMessage();
		}
	}

	QueryInfo(Long time, Long elapsed, String sql, String queryId) {
		this.time = time;
		this.elapsed = elapsed;
		this.sql = sql;
		this.queryId = queryId;
	}

	public void setTime(Long time) { this.time = time; }
	public void setElapsed(Long elapsed) { this.elapsed = elapsed; }
	public void setSql(String sql) { this.sql = sql; }
	public void setId(String queryId) { this.queryId = queryId; }
	public void setHost(String host) { this.host = host; }
	public void setDataSource(String dataSource) { this.dataSource = dataSource; }
	public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; isError = (errorMsg != null); }

	public Long getTime() { return time; }
	public Long getElapsed() { return elapsed; }
	public String getSql() { return sql; }
	public String getId() { return queryId; }
	public String getHost() { return host; }
	public String getDataSource() { return dataSource; }
	public boolean isError() { return isError; }
	public String getErrorMsg() { return errorMsg; }

	@Override
	public int compareTo(QueryInfo o) {
		return (int)(o.getTime() - time);
	}

	@Override
	public QueryInfo clone() {
		try {
			// shallow copy
			return (QueryInfo) super.clone();
		} catch (CloneNotSupportedException cnse) {
			throw new InternalError(cnse.getMessage());
		}
	}
}