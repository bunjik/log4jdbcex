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

import java.sql.BatchUpdateException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import info.bunji.jdbc.LoggerHelper;
import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.specifics.DefaultRdbmsSpecifics;
import info.bunji.jdbc.specifics.OracleRdbmsSpecifics;
import info.bunji.jdbc.specifics.RdbmsSpecifics;
import info.bunji.jdbc.util.FormatUtils;

/**
 *
 * @author f.kinoshita
 */
public abstract class AbstractJdbcLogger implements JdbcLogger {

	/** 実行中のStatementオブジェクトを保持する配列 */
	List<LoggerHelper> activeStatements = Collections.synchronizedList(new ArrayList<LoggerHelper>());

	long timeThreshold = 0L;

	/** ログの出力対象とするSQL文字列の正規表現(デフォルトは全てにマッチ) */
	Pattern acceptPattern = null;

	/** ログの出力対象外とするSQL文字列の正規表現(デフォルトはなし) */
	Pattern ignorePattern = null;

	/** REST API への出力時にSQLを整形するか */
	boolean isFormat = true;

	/** ログ出力時の最大文字数(デフォルト:無制限[-1]) */
	int limitLength = -1;

	/** コネクション取得・切断時のロギング(デフォルト:false) */
	boolean isConnectionLogging = false;

	/** このLoggerインスタンスが出力対象とする接続URL */
	String connectUrl;

	/** このLoggerインスタンスが出力対象とする接続URL */
	String dispUrl;

	/** 実行履歴の保持件数(接続URL単位) */
	int historyCount = 50;

	private final RdbmsSpecifics specifics;

	private List<QueryInfo> queryHistory = new ArrayList<QueryInfo>() {
		@Override
		public boolean add(QueryInfo info) {
			if (historyCount <= 0) return true;

			//info.setDataSource(connectUrl);
			info.setDataSource(dispUrl);

			// 保持件数を超えるものは古いものから削除
			while(size() >= historyCount) remove(0);
			return super.add(info);
		}
	};

	/**
	 ********************************************
	 * <pre>
	 * ロガーは接続URL単位にインスタンスされ、接続URL単位に
	 * 出力設定を行うことが可能
	 * </pre>
	 * @param url connection url
	 ********************************************
	 */
	public AbstractJdbcLogger(String url) {
		connectUrl = url;
		dispUrl = url;

		// URLパラメータにはパスワードを含む場合があるため除去
		int pos = dispUrl.indexOf("?");
		if (pos != -1) {
			dispUrl = dispUrl.substring(0, pos);
		}
		pos = dispUrl.indexOf(";");
		if (pos != -1) {
			dispUrl = dispUrl.substring(0, pos);
		}

		if (url.matches("^jdbc:oracle:.*")) {
			specifics = new OracleRdbmsSpecifics();
		} else {
			specifics = new DefaultRdbmsSpecifics();
		}
	}

	protected String makeLoggerName(String url) {
		String loggerName = LOGGER_NAME;

		// Datasource指定時はLogger名に付与する
		if (!url.startsWith("jdbc:") && !url.equals("_defaultLogger_")) {
			loggerName += "." + url;
		}
		return loggerName;
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#isConnectionLogging()
	 */
	@Override
	public boolean isConnectionLogging() {
		boolean ret = isJdbcLoggingEnabled();
		return ret ? isConnectionLogging : ret;
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#getSpecifics()
	 */
	@Override
	public final RdbmsSpecifics getSpecifics() {
		return specifics;
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#getConnectUrl()
	 */
	@Override
	public String getConnectUrl() {
		return connectUrl;
	}

	@Override
	public String getDispUrl() {
		return dispUrl;
	}

	/**
	 * set logging accept conditiion.
	 *
	 * @param regex accept filter regex string
	 */
	private void setAcceptFilter(String regex) {
		if (regex == null || regex.isEmpty()) {
			this.acceptPattern = null;
			//trace("Accept Filter:not defined.");
		} else {
			try {
				this.acceptPattern = Pattern.compile(regex);
				//trace("Accept Filter:'" + regex + "'");
			} catch (PatternSyntaxException pse) {
				info("Not Accept Filter:'" + regex + "' (" + pse.getMessage() + ")");
			}
		}
	}

	/**
	 * set logging ignore conditiion.
	 *
	 * @param regex ignore filter regex string
	 */
	private void setIgnoreFilter(String regex) {
		if (regex == null || regex.isEmpty()) {
			this.ignorePattern = null;
			//trace("ignore Filter:not defined.");
		} else {
			try {
				this.ignorePattern = Pattern.compile(regex);
				//trace("set ignore Filter:'" + regex + "'");
			} catch (PatternSyntaxException pse) {
				info("Not ignore Filter:'" + regex + "' (" + pse.getMessage() + ")");
			}
		}
	}

	/**
	 * set logging thresthold time.
	 * @param millis threshold milliseconds
	 */
	private void setTimeThreshold(long millis) {
		timeThreshold = Math.max(millis, 0);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#addExecStatement(info.bunji.jdbc.LoggerHelper)
	 */
	@Override
	public void addExecStatement(LoggerHelper statement) {
		activeStatements.add(statement);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#removeExecStatement(info.bunji.jdbc.LoggerHelper)
	 */
	@Override
	public void removeExecStatement(LoggerHelper statement) {
		activeStatements.remove(statement);
	}

	/**
	 ********************************************
	 * It is determined whether the log output target.
	 *
	 * @param sql execute sql
	 * @param elapsed exec time(ms)
	 * @return if output true, other false
	 ********************************************
	 */
	boolean isLogging(String sql, long elapsed) {
		if (timeThreshold <= elapsed || elapsed < 0) {
			if (acceptPattern == null || acceptPattern.matcher(sql).find()) {
				if (ignorePattern == null || !ignorePattern.matcher(sql).find()) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#reportReturned(info.bunji.jdbc.LoggerHelper, java.lang.Object[])
	 */
	@Override
	public void reportReturned(LoggerHelper helper, Object... params) {
		try {
			if (isJdbcLoggingEnabled()) {
				long elapsed = System.currentTimeMillis() - helper.getStartTime();
				if (!helper.isExecuteBatch()) {
					String sql = helper.dumpSql();
					if (isLogging(sql, elapsed)) {
						if (limitLength != -1 && limitLength < sql.length()) {
							debug(RETURN_MSG_FORMAT, elapsed, sql.substring(0, limitLength) + "...");
						} else {
							debug(RETURN_MSG_FORMAT, elapsed, sql);
						}
						queryHistory.add(new QueryInfo(helper, sql));
					}
				} else if (helper.getBatchList() != null) {
					// バッチ実行の場合
					int i = 1;
					int cnt = helper.getBatchList().size();
					for (String sql : helper.getBatchList()) {
						if (isLogging(sql, elapsed)) {
							if (limitLength != -1 && limitLength < sql.length()) {
								debug(BATCH_MSG_FORMAT, i, cnt, sql.substring(0, limitLength) + "...");
							} else {
								debug(BATCH_MSG_FORMAT, i, cnt, sql);
							}
							queryHistory.add(new QueryInfo(helper.getStartTime(), -1L, sql, helper.getQueryId(), null));
						}
						i++;
					}
					if (timeThreshold <= elapsed || elapsed < 0) {
						debug(BATCH_RESULT_FORMAT, elapsed, cnt, cnt);
					}
				}
			}
		} catch (Throwable t) {
			// ロギング処理自身による例外は発生させない
		}
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#reportException(info.bunji.jdbc.LoggerHelper, java.lang.Throwable, java.lang.Object[])
	 */
	@Override
	public void reportException(LoggerHelper helper, Throwable t, Object... params) {
		try {
			if (isJdbcLoggingEnabled()) {
				// バッチ実行時は例外から処理結果を取得して出力
				if (t instanceof BatchUpdateException) {
					int[] ret = ((BatchUpdateException)t).getUpdateCounts();
					long elapsed = System.currentTimeMillis() - helper.getStartTime();
					List<String> list = helper.getBatchList();
					for (int i = 0; i < ret.length; i++) {
						String sql = list.get(i);
						if (ret[i] != Statement.EXECUTE_FAILED) {
							if (isLogging(sql, -1)) {
								debug(BATCH_MSG_FORMAT, i + 1, list.size(), sql);
							}
						} else {
							// エラー時は条件にかかわらず出力
							error(String.format(BATCH_MSG_FORMAT, i + 1, list.size(), sql), t);
						}
					}
					debug(BATCH_RESULT_FORMAT, elapsed, ret.length, ret.length);
				} else {
					String sql = helper.dumpSql();
					long now = System.currentTimeMillis();
					long elapsed = now - helper.getStartTime();
					error(String.format(EXCEPTION_MSG_FORMAT, elapsed, sql), t);
					queryHistory.add(new QueryInfo(helper, sql, t));
				}
			}
		} catch (Throwable e) {
			// ロギング処理自身による例外は発生させない
		}
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#getSetting()
	 */
	@Override
	public Map<String,Object> getSetting() {
		Map<String,Object> statusMap = new LinkedHashMap<String,Object>();

		statusMap.put("timeThreshold", timeThreshold);
		statusMap.put("acceptFilter", acceptPattern != null? acceptPattern.pattern(): null);
		statusMap.put("ignoreFilter", ignorePattern != null? ignorePattern.pattern(): null);
		statusMap.put("historyCount", historyCount);
		statusMap.put("format",       isFormat);
		statusMap.put("limitLength",  limitLength);
		statusMap.put("connectionLogging",  isConnectionLogging);
		//statusMap.put("lastUpdate",   lastUpdate);

		return statusMap;
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#setSetting(java.util.Map)
	 */
	@Override
	public boolean setSetting(Map<String,Object> settings) {
		if (settings == null) return true;

		for (Entry<String,Object> entry : settings.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			try {
				if (key.equalsIgnoreCase("timeThreshold")) {
					setTimeThreshold(Long.parseLong(value.toString()));
				} else if (key.equalsIgnoreCase("historyCount")) {
					int val = Integer.parseInt(value.toString());
					historyCount = (val >= 0 ? val : 0);
				} else if (key.equalsIgnoreCase("acceptFilter")) {
					setAcceptFilter((String)value);
				} else if (key.equalsIgnoreCase("ignoreFilter")) {
					setIgnoreFilter((String)value);
				} else if (key.equalsIgnoreCase("format")) {
					isFormat = Boolean.valueOf(value.toString());
				} else if (key.equalsIgnoreCase("limitLength")) {
					int val = Integer.parseInt(value.toString());
					limitLength = (val > 0 ? val : -1);
				} else if (key.equalsIgnoreCase("connectionLogging")) {
					isConnectionLogging = Boolean.valueOf(value.toString());
				}
			} catch (Exception e) {
				System.out.println(String.format("[%s=%s] setting error.(%s)",
								key, value, e.getMessage()));
				return false;
			}
		}
		return true;
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#getRunningQueries()
	 */
	@Override
	public List<QueryInfo> getRunningQueries() {
		List<QueryInfo> activeQueries = new ArrayList<QueryInfo>();
		synchronized (activeStatements) {
			for (LoggerHelper helper : activeStatements) {
				QueryInfo qi = new QueryInfo(helper,
							isFormat ? FormatUtils.formatSql(helper.dumpSql()) : helper.dumpSql(),
							null);
				qi.setDataSource(dispUrl);
				activeQueries.add(qi);
			}
		}
		return activeQueries;
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#getHistory()
	 */
	@Override
	public List<QueryInfo> getHistory() {
		Set<QueryInfo> tmpList = new TreeSet<QueryInfo>();
		if (isFormat) {
			synchronized (queryHistory) {
				for (QueryInfo qi : queryHistory) {
					QueryInfo tmpQi = qi.clone();
					tmpQi.setSql(FormatUtils.formatSql(qi.getSql()));
					tmpList.add(tmpQi);
				}
			}
		} else {
			tmpList.addAll(queryHistory);
		}
		return new ArrayList<QueryInfo>(tmpList);
	}
}
