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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.bunji.jdbc.DriverEx;
import info.bunji.jdbc.logger.JdbcLogger;

/**
 * logger implementation for slf4j
 *
 * @author f.kinoshia
 */
public class Slf4jJdbcLogger extends AbstractJdbcLogger implements JdbcLogger {

	private final Logger debugLogger;

	public Slf4jJdbcLogger(String url) {
		super(url);

		// Datasource指定時はLogger名に付与する
		if (url.startsWith(DriverEx.DRIVER_URL_PREFIX)) {
			debugLogger = LoggerFactory.getLogger(LOGGER_NAME);
		} else {
			debugLogger = LoggerFactory.getLogger(LOGGER_NAME + "." + url);
		}
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#isJdbcLoggingEnabled()
	 */
	@Override
	public boolean isJdbcLoggingEnabled() {
		return debugLogger.isErrorEnabled();
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#trace(java.lang.String)
	 */
	@Override
	public void trace(String msg) {
		if (debugLogger.isTraceEnabled())
		debugLogger.trace(msg);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#debug(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void debug(String msg, Object... args) {
		if (debugLogger.isDebugEnabled())
		debugLogger.debug(msg != null ? String.format(msg, args) : null);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#info(java.lang.String)
	 */
	@Override
	public void info(String msg) {
		debugLogger.info(msg);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#warn(java.lang.String)
	 */
	@Override
	public void warn(String msg) {
		debugLogger.warn(msg);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#warn(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void warn(String msg, Throwable t) {
		debugLogger.warn(msg, t);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#error(java.lang.String)
	 */
	@Override
	public void error(String msg) {
		debugLogger.error(msg);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#error(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void error(String msg, Throwable t) {
		debugLogger.error(msg, t);
	}
}
