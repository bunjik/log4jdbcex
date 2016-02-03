/*
 * Copyright 2016 Fumiharu Kinoshita
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.bunji.jdbc.logger.impl;

/**
 *
 * @author f.kinoshita
 */
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