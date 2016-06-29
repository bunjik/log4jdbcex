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
package info.bunji.jdbc.logger;

import java.util.List;
import java.util.Map;

import info.bunji.jdbc.LoggerHelper;
import info.bunji.jdbc.logger.impl.QueryInfo;
import info.bunji.jdbc.specifics.RdbmsSpecifics;

/**
 * All logger interface.
 *
 * @author f.kinoshita
 */
public interface JdbcLogger {

	/** loggerName */
	public static final String LOGGER_NAME = "jdbclog";

	public  static final String MSG_FORMAT = "[executed %,4d ms] ";

	public  static final String MSG_FORMAT_WITH_CONN = "[executed %,4d ms] [%s] ";

	/** success log format with connection id */
	public static final String RETURN_MSG_FORMAT_WITH_CONN = "[executed %,4d ms] [%s] %s";

	/** error log format with connection id */
	public static final String EXCEPTION_MSG_FORMAT_WITH_CONN = "[executed %,4d ms] [%s] %s";

	public RdbmsSpecifics getSpecifics();

	/**
	 * get connection url.
	 *
	 * @return jdbc url
	 */
	public String getConnectUrl();

	/**
	 * get show url.
	 *
	 * @return jdbc url
	 */
	public String getDispUrl();


	/**
	 * get logging status
	 *
	 * @return if logging enabled true, other false
	 */
	public boolean isJdbcLoggingEnabled();

	/**
	 * get connection logging status
	 *
	 * @return if logging enabled true, other false
	 */
	public boolean isConnectionLogging();

	/**
	 * output trace log.
	 * @param msg log message
	 */
	public void trace(String msg);

	/**
	 * output debug log.
	 * @param msg log message
	 * @param args message parameters
	 */
	public void debug(String msg, Object... args);

	/**
	 * output info log.
	 * @param msg log message
	 */
	public void info(String msg);

	/**
	 * output warn log.
	 * @param msg log message
	 */
	public void warn(String msg);

	/**
	 * output warn log.
	 * @param msg log message
	 * @param t throwable
	 */
	public void warn(String msg, Throwable t);

	/**
	 * output error log.
	 * @param msg log message
	 */
	public void error(String msg);

	/**
	 * output error log.
	 * @param msg log message
	 * @param t throwable
	 */
	public void error(String msg, Throwable t);

	/**
	 *
	 * @param helper
	 * @param params
	 */
	public void reportReturned(LoggerHelper helper, Object... params);

	/**
	 *
	 * @param helper
	 * @param t throwable
	 * @param params
	 */
	public void reportException(LoggerHelper helper, Throwable t, Object... params);

	/**
	 * add execute statement.
	 *
	 * @param statement target statement
	 */
	public void addExecStatement(LoggerHelper statement);

	/**
	 * remove execute statment.
	 *
	 * @param statement target statement
	 */
	public void removeExecStatement(LoggerHelper statement);

	/**
	 * get setting map.
	 *
	 * @return 設定情報
	 */
	public Map<String,Object> getSetting();

	/**
	 * set setting map.
	 *
	 * @param settings
	 * @return if successed true, other false
	 */
	public boolean setSetting(Map<String,Object> settings);

	/**
	 * get executing sql info list.
	 * @return query list
	 */
	public List<QueryInfo> getRunningQueries();

	/**
	 * get executed sql info list.
	 * @return query list
	 */
	public List<QueryInfo> getHistory();
}
