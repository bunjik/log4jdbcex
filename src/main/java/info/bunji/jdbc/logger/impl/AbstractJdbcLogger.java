/*
 * Copyright 2015 Fumiharu Kinoshita
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
import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.specifics.OracleRdbmsSpecifics;
import info.bunji.jdbc.specifics.RdbmsSpecifics;
import info.bunji.jdbc.util.FormatUtils;

import java.sql.BatchUpdateException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class AbstractJdbcLogger implements JdbcLogger {

	/** 実行中のStatementオブジェクトを保持する配列 */
	protected List<LoggerHelper> activeStatements = Collections.synchronizedList(new ArrayList<LoggerHelper>());

	protected long timeThreshold = 0L;

	/** ログの出力対象とするSQL文字列の正規表現(デフォルトは全てにマッチ) */
	protected Pattern acceptPattern = null;

	/** ログの出力対象外とするSQL文字列の正規表現(デフォルトはなし) */
	protected Pattern ignorePattern = null;

	/** REST API への出力時にSQLを整形するか */
	protected boolean isFormat = true;

	/** ログ出力時の最大文字数(デフォルト:無制限[-1]) */
	protected int limitLength = -1;

	/** このLoggerインスタンスが出力対象とする接続URL */
	protected String connectUrl;

	protected String dsName = null;

	/** 実行履歴の保持件数(接続URL単位) */
	protected int historyCount = 50;

	protected static final String RETURN_MSG_FORMAT = "[executed %,4d ms] %s";

	protected static final String BATCH_MSG_FORMAT = "[executed (%d/%d)] %s";

	protected static final String BATCH_RESULT_FORMAT = "[batch finished %,4d ms] (%d/%d)";

	protected static final String EXCEPTION_MSG_FORMAT = "[executed %,4d ms] %s";

	protected static final String RUNNING_MSG_FORMAT = "Running [elapsed %,4d ms] %s";

	protected final RdbmsSpecifics specifics;

	private List<QueryInfo> queryHistory = new ArrayList<QueryInfo>() {
		@Override
		public boolean add(QueryInfo info) {
			if (historyCount <= 0) return true;

			// 保持件数を超えるものは古いものから削除
			while(size() >= historyCount) remove(0);
			return super.add(info);
		}
	};

	/**
	 ********************************************
	 * コンスタラクタ
	 * <pre>
	 * ロガーは接続URL単位にインスタンスされる想定であり、接続URL単位に
	 * 出力設定を行える想定。
	 * </pre>
	 * @param url 接続URL
	 ********************************************
	 */
	public AbstractJdbcLogger(String url) {
		connectUrl = url;

		if (url.matches("^jdbc:oracle:")) {
			specifics = new OracleRdbmsSpecifics();
		} else {
			specifics = new RdbmsSpecifics();
		}
	}

	public final RdbmsSpecifics getSpecifics() {
		return specifics;
	}

	/**
	 ********************************************
	 * このLoggerが出力対象とする接続URLを返す
	 *
	 * @return 接続URL
	 ********************************************
	 */
	@Override
	public String getConnectUrl() {
		return connectUrl;
	}

	/**
	 ********************************************
	 * 出力の対象とするログの判定用正規表現の設定
	 * <pre>
	 * 比較対象はパラメータ展開後の文字列となるため、パラメータとして
	 * 指定した文字列も対する指定も可能。
	 * </pre>
	 * @param ログ出力の対象とする文字列を検証する正規表現文字列
	 ********************************************
	 */
	@Override
	public void setAcceptFilter(String regex) {
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
	 ********************************************
	 * 出力の対象とするログの判定用正規表現の設定
	 * <pre>
	 * 比較対象はパラメータ展開後の文字列となるため、パラメータとして
	 * 指定した文字列も対する指定も可能。
	 * </pre>
	 * @param ログ出力の対象とする文字列を検証する正規表現文字列
	 ********************************************
	 */
	@Override
	public void setIgnoreFilter(String regex) {
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
	 ********************************************
	 * ロギング対象とするしきい値(ms)を設定する
	 *
	 * @param millis 出力を抑制するしきい値(ms)
	 ********************************************
	 */
	@Override
	public void setTimeThreshold(long millis) {
		timeThreshold = Math.max(millis, 0);
	}

	/*
	 * {@inheritDoc}
	 */
	@Override
	public void addExecStatement(LoggerHelper helper) {
		activeStatements.add(helper);
	}

	/*
	 * {@inheritDoc}
	 */
	@Override
	public void removeExecStatement(LoggerHelper helper) {
		activeStatements.remove(helper);
	}

	/**
	 ********************************************
	 * ログ出力対象とするかを判定する
	 * @param sql 実行したSQL
	 * @param elapsed 実行時間(ms)
	 * @return 出力対象の場合はtrue、そうでない場合はfalseを返す
	 ********************************************
	 */
	protected boolean isLogging(String sql, long elapsed) {
		if (timeThreshold <= elapsed || elapsed < 0) {
			if (acceptPattern == null || acceptPattern.matcher(sql).find()) {
				if (ignorePattern == null || !ignorePattern.matcher(sql).find()) {
					return true;
				}
			}
		}
		return false;
	}

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
						queryHistory.add(new QueryInfo(helper.getStartTime(), elapsed, sql));
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
							queryHistory.add(new QueryInfo(helper.getStartTime(), -1L, sql));
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
	 * {@inheritDoc}
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
							warn(String.format(BATCH_MSG_FORMAT, i + 1, list.size(), sql));
						}
					}
					debug(BATCH_RESULT_FORMAT, elapsed, ret.length, ret.length);
				} else {
					String sql = helper.dumpSql();
					long now = System.currentTimeMillis();
					long elapsed = now - helper.getStartTime();
					warn(String.format(EXCEPTION_MSG_FORMAT, elapsed, sql));
					queryHistory.add(new QueryInfo(helper.getStartTime(), elapsed, sql, true));
				}
			}
		} catch (Throwable e) {
			// ロギング処理自身による例外は発生させない
		}
	}

	/**
	 ********************************************
	 * このLoggerに対する設定情報を取得する
	 * @return
	 ********************************************
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

		return statusMap;
	}

	/**
	 ********************************************
	 * このLoggerに対する設定情報を出力する
	 ********************************************
	 */
	@Override
	public void printSetting() {
		info("[Setting] url= " + connectUrl);
		info("[Setting] timeThreshold= " + timeThreshold);
		info("[Setting] acceptFilter = " + (acceptPattern != null? acceptPattern.pattern(): ".*"));
		info("[Setting] ignoreFilter = " + (ignorePattern != null? ignorePattern.pattern(): null));
		info("[Setting] historyCount = " + historyCount);
		info("[Setting] format       = " + isFormat);
		info("[Setting] limitLength  = " + limitLength);
	}

	/**
	 *
	 */
	@Override
	public boolean setSetting(Map<String,Object> settings) {
		if (settings == null) return true;

		for (Entry<String,Object> entry : settings.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			try {
				if (key.equalsIgnoreCase("timeThreshold")) {
					int val = Integer.valueOf(value.toString());
					timeThreshold = (val >= 0 ? val : 0);
				} else if (key.equalsIgnoreCase("historyCount")) {
					int val = Integer.valueOf(value.toString());
					historyCount = (val >= 0 ? val : 0);
				} else if (key.equalsIgnoreCase("acceptFilter")) {
					setAcceptFilter((String)value);
				} else if (key.equalsIgnoreCase("ignoreFilter")) {
					setIgnoreFilter((String)value);
				} else if (key.equalsIgnoreCase("format")) {
					isFormat = Boolean.valueOf(value.toString());
				} else if (key.equalsIgnoreCase("limitLength")) {
					int val = Integer.valueOf(value.toString());
					limitLength = (val > 0 ? val : -1);
				}
			} catch (Exception e) {
				System.out.println(String.format("[%s=%s] setting error.(%s)",
								key, value, e.getMessage()));
				return false;
			}
		};
		return true;
	}

	/**
	 ********************************************
	 *
	 ********************************************
	 */
	@Override
	public List<QueryInfo> getRunningQueries() {
		List<QueryInfo> activeQueries = new ArrayList<QueryInfo>();
		synchronized (activeStatements) {
			for (LoggerHelper helper : activeStatements) {
				activeQueries.add(new QueryInfo(helper.getStartTime(),
								System.currentTimeMillis() - helper.getStartTime(),
								isFormat ? FormatUtils.formatSql(helper.dumpSql()) : helper.dumpSql(),
								helper.getQueryId()));
			}
		}
		return activeQueries;
	}

	/**
	 ********************************************
	 * 検索履歴の取得
	 ********************************************
	 */
	@Override
	public List<QueryInfo> getHistory() {
		List<QueryInfo> tmpList = new ArrayList<QueryInfo>(queryHistory);
		if (isFormat) {
			for (int i = 0; i < tmpList.size(); i++) {
				QueryInfo q = tmpList.get(i);
				tmpList.set(i, new QueryInfo(
									q.getTime(),
									q.getElapsed(),
									FormatUtils.formatSql(q.getSql()),
									q.isError()));
			}
		}
		return tmpList;
	}
}
