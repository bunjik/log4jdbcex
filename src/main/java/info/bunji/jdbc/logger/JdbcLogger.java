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
package info.bunji.jdbc.logger;

import java.util.Collection;
import java.util.Map;

import info.bunji.jdbc.logger.impl.QueryInfo;
import info.bunji.jdbc.specifics.RdbmsSpecifics;
import info.bunji.jdbc.util.LoggerHelper;

/**
 *
 * @author f.kinoshita
 */
public interface JdbcLogger {
	/** ロガー名 */
	public static final String LOGGER_NAME = "jdbclog";

	public RdbmsSpecifics getSpecifics();

	public String getConnectUrl();

	public boolean isJdbcLoggingEnabled();

	public void setAcceptFilter(String regex);

	public void setIgnoreFilter(String regex);

	public void setTimeThreshold(long millis);

	public void trace(String msg);

	public void debug(String msg, Object... args);

	public void info(String msg);

	public void warn(String msg);

	public void warn(String msg, Throwable t);

	public void error(String msg);

	public void error(String msg, Throwable t);

	public void reportReturned(LoggerHelper helper, Object... params);

	public void reportException(LoggerHelper helper, Throwable t, Object... params);

	/**
	 * 実行中のオブジェクトを追加する
	 * @param helper
	 */
	public void addExecStatement(LoggerHelper helper);

	/**
	 * 実行中のオブジェクトを削除する
	 * @param helper
	 */
	public void removeExecStatement(LoggerHelper helper);

	/**
	 * このロガーの設定情報を取得する
	 * @return 設定情報
	 */
	public Map<String,Object> getSetting();

	/**
	 * このロガーに設定情報を設定する
	 * @return 設定情報
	 */
	public boolean setSetting(Map<String,Object> settings);

	/**
	 * このロガーの設定情報をログ(INFO)に出力する
	 */
	public void printSetting();

	/**
	 * 現在実行中のクエリのリストを取得する
	 */
	public Collection<QueryInfo> getRunningQueries();

	/**
	 * 実行済のクエリのリストを取得する
	 */
	public Collection<QueryInfo> getHistory();
}
