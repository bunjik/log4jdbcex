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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;

/**
 **********************************************************
 * implements Driver Wrapper.
 *
 * @author f.kinoshita
 **********************************************************
 */
public class DriverProxy implements InvocationHandler {

	/** real Connection */
	private Driver realDriverLast;

	private static final Driver DRIVER_EX = new DriverEx();

	private static final String PARAM_PREFIX = "logging.";

	private static final AtomicInteger counter = new AtomicInteger(0);

	/*
	 * (非 Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			String name = method.getName();

			if (name.equals("acceptsURL")) {
				return acceptsURL((String) args[0]);
			} else if (name.equals("connect")) {
				Driver d = getRealDriver((String) args[0]);
				if (d == null) return null;
				args[0] = getRealUrl((String)args[0]);

				JdbcLogger logger = JdbcLoggerFactory.getLogger((String)args[0]);

				// check logging parameter
				if (args.length > 1) {
					Properties newProps = (Properties) args[1];
					Properties chkProps = new Properties();
					chkProps.putAll(newProps);
					Map<String, Object> settings = new HashMap<String, Object>();
					for (Entry<Object, Object> entry : chkProps.entrySet()) {
						String key = (String)entry.getKey();
						if (key.startsWith(PARAM_PREFIX)) {
							settings.put(key.substring(PARAM_PREFIX.length()), entry.getValue());
							newProps.remove(key);
						}
					}
					logger.setSetting(settings);
				}

				Connection conn = null;
				if (logger.isJdbcLoggingEnabled()) {
					// connectionIdの生成
					String id = String.format("%s%04d", DriverEx.BASE_CONN_ID, counter.incrementAndGet());
					counter.compareAndSet(100, 0);
					long start = System.currentTimeMillis();
					try {
						ConnectionProxy connProxy = new ConnectionProxy((Connection) method.invoke(d, args), (String)args[0], id);
						conn = ProxyFactory.getInstance().newProxyInstance(Connection.class, connProxy);
						if (logger.isConnectionLogging()) {
							logger.debug(String.format(JdbcLogger.RETURN_MSG_FORMAT_WITH_CONN, System.currentTimeMillis() - start, id, "get connection."));
						}
					} catch (Throwable t) {
						if (logger.isConnectionLogging()) {
							logger.error(String.format(JdbcLogger.EXCEPTION_MSG_FORMAT, System.currentTimeMillis() - start, "get connection."), t.getCause());
						}
						throw t;
					}
				} else {
					// get real connection
					conn = (Connection)method.invoke(d, args);
				}
				return conn;
			} else if (name.equals("getPropertyInfo")) {
				Driver d = getRealDriver((String)args[0]);
				return d.getPropertyInfo(getRealUrl((String)args[0]), (Properties) args[1]);
			}

			if(realDriverLast == null) {
				// DriverExのメソッドを呼び出し
				return method.invoke(DRIVER_EX, args);
			}
			return method.invoke(realDriverLast, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	/*
	 *
	 * @param url
	 * @return
	 * @throws SQLException
	 */
	private boolean acceptsURL(String url) throws SQLException {
		return getRealDriver(url) != null ? true : false;
	}

	/**
	 *
	 * @param url
	 * @return real connection String
	 */
	private String getRealUrl(String url) {
		String realUrl = url;
		if (url.startsWith(DriverEx.DRIVER_URL_PREFIX)) {
			// 実際の接続URLを抽出する
			realUrl = "jdbc:" + url.substring(DriverEx.DRIVER_URL_PREFIX.length());
		}
		return realUrl;
	}

	/**
	 * get real driver.
	 *
	 * @param url
	 * @return
	 * @throws SQLException
	 */
	private Driver getRealDriver(String url) throws SQLException {
		Driver driver = null;

		// getDriver()だと再帰となるため自分自身のURLの場合のみ処理する
		if (url != null && url.startsWith(DriverEx.DRIVER_URL_PREFIX)) {
			realDriverLast = DriverManager.getDriver(getRealUrl(url.toString()));
			driver = realDriverLast;
		}
		return driver;
	}
}
