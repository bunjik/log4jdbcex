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

import info.bunji.jdbc.logger.JdbcLogger;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * JDKのログAPIを利用する実装クラス
 *
 * @author f.kinoshita
 */
public class JdkJdbcLogger extends AbstractJdbcLogger implements JdbcLogger {

	private final Logger debugLogger = Logger.getLogger(LOGGER_NAME);

	public JdkJdbcLogger(String url) {
		super(url);
	}

	@Override
	public boolean isJdbcLoggingEnabled() {

		return debugLogger.isLoggable(Level.SEVERE);
	}

	@Override
	public void trace(String msg) {
		if (debugLogger.isLoggable(Level.FINEST))
		debugLogger.finest(msg);
	}

	@Override
	public void debug(String msg, Object... args) {
		if (debugLogger.isLoggable(Level.FINE))
		debugLogger.fine(String.format(msg, args));
	}

	@Override
	public void info(String msg) {
		debugLogger.info(msg);
	}

	@Override
	public void warn(String msg) {
		debugLogger.warning(msg);
	}

	@Override
	public void warn(String msg, Throwable t) {
		LogRecord record = new LogRecord(Level.WARNING, msg);
		record.setLoggerName(LOGGER_NAME);
		record.setThrown(t);
		debugLogger.log(record);
	}

	@Override
	public void error(String msg) {
		debugLogger.severe(msg);
	}

	@Override
	public void error(String msg, Throwable t) {
		LogRecord record = new LogRecord(Level.SEVERE, msg);
		record.setLoggerName(LOGGER_NAME);
		record.setThrown(t);
		debugLogger.log(record);
	}
}
