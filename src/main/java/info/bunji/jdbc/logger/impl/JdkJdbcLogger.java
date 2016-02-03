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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import info.bunji.jdbc.logger.JdbcLogger;

/**
 * logger implementation for java.util.logging.
 *
 * @author f.kinoshita
 */
public class JdkJdbcLogger extends AbstractJdbcLogger implements JdbcLogger {

	private final Logger debugLogger = Logger.getLogger(LOGGER_NAME);

	public JdkJdbcLogger(String url) {
		super(url);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#isJdbcLoggingEnabled()
	 */
	@Override
	public boolean isJdbcLoggingEnabled() {

		return debugLogger.isLoggable(Level.SEVERE);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#trace(java.lang.String)
	 */
	@Override
	public void trace(String msg) {
		if (debugLogger.isLoggable(Level.FINEST))
		debugLogger.finest(msg);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#debug(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void debug(String msg, Object... args) {
		if (debugLogger.isLoggable(Level.FINE))
		debugLogger.fine(msg != null ? String.format(msg, args) : null);
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
		debugLogger.warning(msg);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#warn(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void warn(String msg, Throwable t) {
		LogRecord record = new LogRecord(Level.WARNING, msg);
		record.setLoggerName(LOGGER_NAME);
		record.setThrown(t);
		debugLogger.log(record);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#error(java.lang.String)
	 */
	@Override
	public void error(String msg) {
		debugLogger.severe(msg);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.logger.JdbcLogger#error(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void error(String msg, Throwable t) {
		LogRecord record = new LogRecord(Level.SEVERE, msg);
		record.setLoggerName(LOGGER_NAME);
		record.setThrown(t);
		debugLogger.log(record);
	}
}
