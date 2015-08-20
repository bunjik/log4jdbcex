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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SLF4JのログAPIを利用する実装クラス
 *
 * @author f.kinoshia
 */
public class Slf4jJdbcLogger extends AbstractJdbcLogger implements JdbcLogger {

	private final Logger debugLogger = LoggerFactory.getLogger(LOGGER_NAME);

	public Slf4jJdbcLogger(String url) {
		super(url);
	}

	@Override
	public boolean isJdbcLoggingEnabled() {
		return debugLogger.isErrorEnabled();
	}

	@Override
	public void trace(String msg) {
		if (debugLogger.isTraceEnabled())
		debugLogger.trace(msg);
	}

	@Override
	public void debug(String msg, Object... args) {
		if (debugLogger.isDebugEnabled())
		debugLogger.debug(String.format(msg, args));
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
		debugLogger.error(msg, t);
	}
}
