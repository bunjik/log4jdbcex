package info.bunji.jdbc.logger.impl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Log4jのログAPIを利用する実装クラス
 *
 * @author f.kinoshita
 */
public class Log4jJdbcLogger extends AbstractJdbcLogger {

	private final Logger debugLogger = Logger.getLogger(LOGGER_NAME);

	public Log4jJdbcLogger(String url) {
		super(url);
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
		debugLogger.error(msg);
	}
}
