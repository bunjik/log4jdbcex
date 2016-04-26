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

import info.bunji.jdbc.LoggerHelper;

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

	QueryInfo(LoggerHelper helper, String sql) {
		this(helper, sql, null);
	}

	QueryInfo(LoggerHelper helper, String sql, Throwable t) {
		this(helper.getStartTime(), System.currentTimeMillis() - helper.getStartTime(), sql, helper.getQueryId(), t);
	}

	QueryInfo(Long time, Long elapsed, String sql, String queryId, Throwable t) {
		this.time = time;
		this.elapsed = elapsed;
		this.sql = sql;
		this.queryId = queryId;
		if (t != null) {
			this.isError = true;
			this.errorMsg = t.getMessage();
		}
	}

	public void setSql(String sql) { this.sql = sql; }
	public void setHost(String host) { this.host = host; }
	public void setDataSource(String dataSource) { this.dataSource = dataSource; }

	public Long getTime() { return time; }
	public Long getElapsed() { return elapsed; }
	public String getSql() { return sql; }
	public String getId() { return queryId; }
	public String getHost() { return host; }
	public String getDataSource() { return dataSource; }
	public boolean isError() { return isError; }
	public String getErrorMsg() { return errorMsg; }

	/*
	 * (非 Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(QueryInfo o) {
		int ret = 0;
		ret = (int)(o.getTime() - time);
		if (ret == 0) {
			ret = queryId.compareTo(o.getId());
		}
		return ret;
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof QueryInfo) {
			return compareTo((QueryInfo) obj) == 0;
		}
		return false;
	}

	/*
	 * (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return queryId.hashCode();
	}

	/*
	 * (非 Javadoc)
	 * @see java.lang.Object#clone()
	 */
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
