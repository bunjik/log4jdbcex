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
package info.bunji.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;
import info.bunji.jdbc.specifics.RdbmsSpecifics;

/**
 *
 * @author f.Kinoshita
 */
public abstract class LoggerHelper {

	/** connection url */
	protected String url;

	/** このインスタンスが利用するLogger */
	private JdbcLogger logger;

	/** クエリの実行開始時間 */
	private long _startTime = 0L;

	/** クエリを一意に得意するためのキー */
	private String _queryId;

	/** 実行SQL */
	private String _execSql = "";

	/** パラメータを保持するList */
	private List<ParameterInfo> _paramList = new ArrayList<ParameterInfo>();

	/** addBatch用のSQLを保持するList */
	private List<String> _batchList = null;

	private boolean isExecuteBatch = false;

	/** パラメータ置換用の正規表現 */
	private static final Pattern PARAM_REGEX = Pattern.compile("\\?");

	/**
	 ********************************************
	 * @param url execute sql (for PreparedStatement or CallableStatement)
	 ********************************************
	 */
	LoggerHelper(String url) {
		this.url = url;
		logger = JdbcLoggerFactory.getLogger(url);
	}

	/**
	 ********************************************
	 * get connection url.
	 * @return connection url
	 ********************************************
	 */
	public String getUrl() {
		return url;
	}

	/**
	 ********************************************
	 * クエリの開始時に呼び出すメソッド.
	 *
	 * 実行中クエリのキューに自身を登録するとともに、開始時間を記録する
	 *
	 * @param sql execute sql
	 ********************************************
	 */
	void startExecute(String sql) {
		setSql(sql);
		startExecute();
	}

	/**
	 ********************************************
	 * クエリの開始時に呼び出すメソッド.
	 *
	 * 実行中クエリのキューに自身を登録するとともに、開始時間を記録する
	 ********************************************
	 */
	void startExecute() {
		_queryId = UUID.randomUUID().toString();
		_startTime = System.currentTimeMillis();
		logger.addExecStatement(this);
	}

	/**
	 ********************************************
	 * クエリの終了後に呼び出すメソッド.
	 *
	 * 実行中クエリのキューから自身を削除する<br>
	 * ※finally節などで呼び出しを担保すること
	 ********************************************
	 */
	void endExecute() {
		logger.removeExecStatement(this);
	}

	/**
	 ********************************************
	 * 実行用のSQLを蓄積する
	 * @param args addBatchメソッドの引数配列(最大１)
	 ********************************************
	 */
	void addBatchList(Object... args) {
		// SQL文字列を追加する
		if (_batchList == null) _batchList = new ArrayList<String>();
		if (args == null || args.length == 0) {
			// パラメータ埋め込み後のSQL文字列を追加する
			_batchList.add(dumpSql());
		} else {
			_batchList.add((String)args[0]);
		}
	}

	void clearBatchList() {
		_batchList = null;
	}

	void clearParameterList() {
		_paramList.clear();
	}

	void reportReturned() {
		logger.reportReturned(this);
	}

	/**
	 ********************************************
	 * executeBatch対応
	 ********************************************
	 */
	void reportBatchReturned() {
		try {
			isExecuteBatch = true;
			logger.reportReturned(this);
		} finally {
			isExecuteBatch = false;
			clearBatchList();
		}
	}

	/**
	 ********************************************
	 * logging exception.
	 *
	 ********************************************
	 */
	void reportException(Throwable t, Object... params) {
		logger.reportException(this, t, params);
	}

	/**
	 ********************************************
	 * 実行するクエリを設定する.
	 * @param sql 実行SQL文字列
	 ********************************************
	 */
	String setSql(String sql) {
		_execSql = sql;
		return _execSql;
	}

	String getSql() {
		return _execSql;
	}

	/**
	 * get execute start time.
	 * @return start time
	 */
	public long getStartTime() {
		return _startTime;
	}

	/**
	 * get execute sql.
	 *
	 * if parameterized sql, embed parameters.
	 *
	 * @return sql
	 */
	public String dumpSql() {
		try {
			RdbmsSpecifics spec = logger.getSpecifics();
			StringBuffer sqlbuf = new StringBuffer();
			Matcher m = PARAM_REGEX.matcher(_execSql);
			int paramIdx = 0;
			while (m.find()) {
				try {
					ParameterInfo pi = _paramList.get(paramIdx++);
					String param = "NULL";
					if (pi != null) {
						if (pi.getName() != null) {
							param = pi.getName() + "=" + spec.formatParameterObject(pi.getValue());
						} else {
							param = spec.formatParameterObject(pi.getValue());
						}
					}
					m.appendReplacement(sqlbuf, param);
				} catch (IndexOutOfBoundsException ioobe) {
					break;
				}
			}
			m.appendTail(sqlbuf);
			return sqlbuf.toString().trim();
		} catch (Exception e) {
			return "SQL Log generate failed.(" + e.getMessage() + ")";
		}
	}

	void addParameter(int index, int type, Object value) {
		// 必要に応じて領域を確保
		while (index > _paramList.size()) _paramList.add(null);
		_paramList.set(index - 1, ParameterInfo.create(type, value));
	}

	void addParameter(String name, int type, Object value) {
		// TODO:2回同じパラメータを指定された場合は未考慮
		// TODO:indexとnameが混在して指定された場合も未考慮
		_paramList.add(ParameterInfo.create(type, name, value));
	}

	/**
	 * get uniqu queryid.
	 *
	 * @return queryId
	 */
	public String getQueryId() {
		return _queryId;
	}

	/**
	 * get batch sql list.
	 *
	 * if parameterized sql, embed parameters.
	 *
	 * @return batch sql list
	 */
	public List<String> getBatchList() {
		return _batchList;
	}

	public boolean isExecuteBatch() {
		return isExecuteBatch;
	}

	/**
	 ********************************************
	 * パラメータを保持するクラス
	 ********************************************
	 */
	static class ParameterInfo {
		private int type;
		private String name;
		private Object value;

		private ParameterInfo(int type, String name, Object value) {
			this.type = type;
			this.name = name;
			this.value = value;
		}

		public static ParameterInfo create(int type, Object value) {
			return new ParameterInfo(type, null, value);
		}

		public static ParameterInfo create(int type, String name, Object value) {
			return new ParameterInfo(type, name, value);
		}

		public String getName() { return name; }

		//public int getType() { return type; }

		public Object getValue() { return value; }
	}
}
