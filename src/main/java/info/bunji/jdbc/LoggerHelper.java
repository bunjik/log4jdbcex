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
package info.bunji.jdbc;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;
import info.bunji.jdbc.specifics.RdbmsSpecifics;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.arnx.jsonic.JSON;

/**
 *
 * @author f.kinoshita
 */
public abstract class LoggerHelper {

	/** このインスタンスが利用するLogger */
	protected JdbcLogger logger;

	/** ConnectionExオブジェクト */
	protected ConnectionEx connEx;

	/** クエリの実行開始時間 */
	protected long _startTime = 0L;

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

	/** テスト用の設定情報を保持する(接続URL毎) */
	private static final Map<String,Setting> testSettingMap = new HashMap<String,Setting>();

	/** 現在のコネクションに対応するテスト設定情報 */
	private Setting currentSetting;

	/**
	 ********************************************
	 * テスト設定を保持するクラス
	 ********************************************
	 */
	class Setting {
		private Map<Pattern,String> patternMap = new LinkedHashMap<Pattern,String>();

		private boolean isFakeCommit = false;

		/**
		 *
		 * @param regex
		 * @param fileName
		 */
		void addPattern(String regex, String fileName) {
			Pattern pattern = Pattern.compile(regex);
			// ファイルの存在チェックをする？
			if ((new File(fileName)).exists()) {
				patternMap.put(pattern, fileName);
				logger.info(String.format("accept setting. regex=[%s] datafile=[%s]", regex, fileName));
			} else {
				logger.warn(String.format("datafile not found. disable setting. regex=[%s] filename=[%s]", regex, fileName));
			}
		}

		/**
		 *
		 * @param sql チェック対象SQL
		 * @return パターンに一致する対象のテストファイル名。一致するものがない場合はnullを返す。
		 */
		String matchPattern(String sql) {
			String fileName = null;
			for (Entry<Pattern,String> entry : patternMap.entrySet()) {
				if (entry.getKey().matcher(sql).find()) {
					fileName = entry.getValue();
					logger.debug("match sql : datafile is " + fileName);
					break;
				}
			}
			return fileName;
		}

		private void setFakeCommit(boolean flag) {
			isFakeCommit = flag;
		}

		boolean isFakeCommit() {
			return isFakeCommit;
		}
	}

	/**
	 ********************************************
	 * コンストラクタ
	 * @param conn
	 ********************************************
	 */
	LoggerHelper(ConnectionEx conn) {
		if (conn != null) {
			setConnectionEx(conn);
		}
	}

	/**
	 ********************************************
	 * ConnecionExをセットする.
	 * ConnecionExが自身を登録するために利用するものであり、通常は利用しない。
	 * すでに設定されている場合は何もしない。
	 * @param conn
	 ********************************************
	 */
	protected void setConnectionEx(ConnectionEx conn) {
		if (connEx == null) {
			this.connEx = conn;
			String url = connEx.getUrl();
			logger = JdbcLoggerFactory.getLogger(url);

			// 初回のみ設定を読み込む
			synchronized (testSettingMap) {
				if (!testSettingMap.containsKey(url)) {
					InputStream is = null;
					String testFileName = conn.getTestConfigName();
					Setting setting = new Setting();
					if (testFileName != null) {
						try {
							is = new FileInputStream(testFileName);
							Map<String,?> tmpMap = JSON.decode(is);

							// ログ出力設定の読み込み
							if (tmpMap.containsKey("logSetting")) {
								@SuppressWarnings("unchecked")
								Map<String,Object> logMap = (Map<String,Object>) tmpMap.get("logSetting");
								logger.setSetting(logMap);
								logger.printSetting();
							}
							// テスト用設定の読み込み
							if (tmpMap.containsKey("testSetting")) {
								@SuppressWarnings("unchecked")
								Map<String,?> testMap = (Map<String,?>) tmpMap.get("testSetting");
								@SuppressWarnings("unchecked")
								List<Map<String,String>> patternList = (List<Map<String,String>>)testMap.get("pattern");
								for (Map<String,String> pattern : patternList) {
									setting.addPattern(pattern.get("regex"), pattern.get("file"));
								}
								if (tmpMap.containsKey("fakeCommit")) {
									setting.setFakeCommit((Boolean)tmpMap.get("fakeCommit"));
								}
								logger.debug("test setting loaded.[" +testFileName + "]");
							}
						} catch (Exception e) {
							logger.error("test setting load error:" + e.getMessage());
						} finally {
							try { if (is != null) is.close(); } catch(Exception e) {}
						}
					}
					testSettingMap.put(url, setting);
				}
				currentSetting = testSettingMap.get(url);
			}
		} else {
			logger.info("Wrapped Connection is already exists. do nothting.");
		}
	}

	/**
	 ********************************************
	 *
	 ********************************************
	 */
	boolean isFakeCommit() {
		return currentSetting.isFakeCommit();
	}

	/**
	 ********************************************
	 * クエリの開始時に呼び出すメソッド.
	 *
	 * 実行中クエリのキューに自身を登録するとともに、開始時間を記録する
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
	 * Batch実行用のSQLを蓄積する
	 * @param sql
	 ********************************************
	 */
	protected void addBatchList(String sql) {
		// SQL文字列を追加する
		if (_batchList == null) _batchList = new ArrayList<String>();
		_batchList.add(sql);
	}

	/**
	 ********************************************
	 * Batch実行用のSQLを蓄積する
	 * @param sql
	 ********************************************
	 */
	protected void addBatchList() {
		// パラメータ埋め込み後のSQL文字列を追加する
		if (_batchList == null) _batchList = new ArrayList<String>();
		_batchList.add(dumpSql());
	}

	protected void clearBatchList() {
		_batchList = null;
	}

	protected void clearParameterList() {
		_paramList.clear();
	}

	public Object reportResult(Object execResult) {
		logger.reportReturned(this);
		return execResult;
	}

	public int reportReturned(int execResult) {
		logger.reportReturned(this);
		return execResult;
	}

	boolean reportReturned(boolean execResult) {
		logger.reportReturned(this);
		return execResult;
	}

	ResultSet reportReturned(ResultSet execResult) {
		logger.reportReturned(this);

		// 指定条件に一致する場合、本来の結果ではなく
		// CSVから読み込んだ結果を返す
		String fileName = currentSetting.matchPattern(getSql());
		if (fileName != null) {
			try {
				return new TestCsvResultSet(execResult, fileName);
			} catch (Exception e) {
				// エラー発生時
				logger.error(e.getMessage(), e);
			}
		}
		return execResult;
	}

	/**
	 ********************************************
	 * executeBatch対応
	 * @param execResult
	 * @return
	 ********************************************
	 */
	int[] reportReturned(int[] execResult) {
		try {
			isExecuteBatch = true;
			logger.reportReturned(this);
			return execResult;
		} finally {
			isExecuteBatch = false;
			clearBatchList();
		}
	}

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

	public long getStartTime() {
		return _startTime;
	}

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

	public void addParameter(int index, int type, Object value) {
		// 必要に応じて領域を確保
		while (index > _paramList.size()) _paramList.add(null);
		_paramList.set(index - 1, ParameterInfo.create(type, value));
	}

	public void addParameter(String name, int type, Object value) {
		// TODO:2回同じパラメータを指定された場合は未考慮
		// TODO:indexとnameが混在して指定された場合も未考慮
		_paramList.add(ParameterInfo.create(type, name, value));
	}

	String getParameters() {
		StringBuilder buf = new StringBuilder();
		if (_paramList == null | _paramList.isEmpty()) {
			return "";
		}
		for (ParameterInfo p : _paramList) {
			buf.append(", ");
			if (p == null) {
				buf.append("NULL");
			} else {
				if (p.getName() != null) {
					buf.append(p.getName()).append("=");
				}
				buf.append(p.getValue());
			}
		}
		return buf.substring(2);
	}

	void dumpParameters() {
		logger.debug("[" + getParameters() + "]");
	}

	public JdbcLogger getLogger() {
		return logger;
	}

	public String getQueryId() {
		return _queryId;
	}

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

		public int getType() { return type; }

		public Object getValue() { return value; }
	}
}
