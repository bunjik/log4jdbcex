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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import info.bunji.jdbc.DriverEx;

/**
 * logger implementation for log4j.
 *
 * @author f.kinoshita
 */
public class Log4jJdbcLogger extends AbstractJdbcLogger {

	private final Logger debugLogger;

	public Log4jJdbcLogger(String url) {
		super(url);

		// Datasource指定時はLogger名に付与する
		if (url.startsWith(DriverEx.DRIVER_URL_PREFIX) || url.equals("_defaultLogger_")) {
			debugLogger = Logger.getLogger(LOGGER_NAME);
		} else {
			debugLogger = Logger.getLogger(LOGGER_NAME + "." + url);
		}
	}

	@Override
	public boolean isJdbcLoggingEnabled() {
		return debugLogger.isEnabledFor(Level.ERROR);
	}

	@Override
	public void trace(String msg) {
		if (debugLogger.isTraceEnabled())
		debugLogger.trace(msg);
	}

	@Override
	public void debug(String msg, Object... args) {
		if (debugLogger.isDebugEnabled())
		debugLogger.debug(msg != null ? String.format(msg, args) : null);
	}

	@Override
	public void info(String msg) {
		debugLogger.info(msg);
	}

	@Override
	public void warn(String msg) {
		debugLogger.warn(msg);
	}

	@Override
	public void warn(String msg, Throwable t) {
		debugLogger.warn(msg, t);
	}

	@Override
	public void error(String msg) {
		debugLogger.error(msg);
	}

	@Override
	public void error(String msg, Throwable t) {
		debugLogger.error(msg);
	}
}
