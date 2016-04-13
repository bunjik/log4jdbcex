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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.sql.DataSource;

import info.bunji.jdbc.DriverEx;
import info.bunji.jdbc.logger.impl.CommonsLoggingJdbcLogger;
import info.bunji.jdbc.logger.impl.JdkJdbcLogger;
import info.bunji.jdbc.logger.impl.Log4jJdbcLogger;
import info.bunji.jdbc.logger.impl.Slf4jJdbcLogger;
import net.arnx.jsonic.JSON;

/**
 * logger factory.
 * <pre>
 * find a log implementation from class path.
 *
 * Search priority
 *  1. slf4j
 *  2. commons-logging
 *  3. log4j
 *  4. java.util.logging
 *
 * logging setting examples:
 * {
 *  "_default_": {
 *		"timeThreshold": 0,
 *		"acceptFilter": ".*",
 *		"ignoreFilter": null,
 *		"historyCount": 30,
 *		"format": true
 *	},
 *
 *	"jdbc:h2:tcp://localhost/~/test;SCHEMA=INFORMATION_SCHEMA": {
 *		"timeThreshold": 0,
 *		"acceptFilter": ".*",
 *		"ignoreFilter": null,
 *		"historyCount": 30,
 *		"limmitLength": 100,
 *		"format": true
 *	}
 * }
 * </pre>
 * @author f.kinoshita
 */
public class JdbcLoggerFactory {

	private static final String SETTING_FILE = DriverEx.DRIVER_NAME + ".json";

	private static final String DEFAULT_SETTING = "_default_";

	private static final String DEFAULT_LOGGER = "_defaultLogger_";

	private static Map<String,JdbcLogger> loggerCache = new HashMap<String,JdbcLogger>();

	private static Map<String,String> dsNameMap = new HashMap<String,String>();

	private static Constructor<?> constructor;

	@SuppressWarnings("unused")
	private static JdbcLoggerFactory instance = new JdbcLoggerFactory();

	/** 設定ファイルの設定情報を保持する */
	private static Map<String,Map<String,Object>> settingMap = new HashMap<String,Map<String,Object>>();

	static {
//		// 利用可能なLoggingライブラリを検索する(優先順を考慮)
//		Map<String, Class<? extends JdbcLogger>> checkLogger = new LinkedHashMap<String, Class<? extends JdbcLogger>>() {{
//			put("org.slf4j.Logger", Slf4jJdbcLogger.class);
//			put("org.apache.commons.logging.Log", CommonsLoggingJdbcLogger.class);
//			put("org.apache.log4j.Logger", Log4jJdbcLogger.class);
//			put("java.util.logging.Logger", JdkJdbcLogger.class);
//		}};
//
//		// 利用するLoggerを決定する
//		for (Entry<String, Class<? extends JdbcLogger>> entry : checkLogger.entrySet()) {
//			try {
//				// 見つかったらそのクラスを利用する
//				Class.forName(entry.getKey());
//				constructor = entry.getValue().getConstructor(String.class);
//				break;
//			} catch (Exception e) {
//				// do nothing.
//			}
//		}

		InputStream is = null;
		try {
			// 設定ファイルの読み込み
			is = JdbcLoggerFactory.class.getResourceAsStream("/" + SETTING_FILE);
			if (is != null) {
				try {
					settingMap = JSON.decode(is);
				} catch (Exception e) {
					System.err.println("jdbc logging setting error:" + e.getMessage());
				} finally {
					try { is.close(); } catch(Exception e) {}
				}
			} else {
				//System.out.println("[" + SETTING_FILE + "] not found. use default.");
			}
		} catch (Exception e) {
			// do nothihg.
		} finally {
			try { if (is != null) is.close(); } catch(Exception e) {}
		}
	}

	/**
	 ********************************************
	 * private constructor.
	 ********************************************
	 */
	private JdbcLoggerFactory() {
		// 利用するLoggerを決定する
		constructor = getLoggerConstructor();
	}

	/**
	 ********************************************
	 * find logger constructor.
	 *
	 * @return logger constructor
	 ********************************************
	 */
	private Constructor<?> getLoggerConstructor() {
		// 利用可能なLoggingライブラリ
		Map<String, Class<? extends JdbcLogger>> checkLogger = new LinkedHashMap<String, Class<? extends JdbcLogger>>() {{
			put("org.slf4j.Logger", Slf4jJdbcLogger.class);
			put("org.apache.commons.logging.Log", CommonsLoggingJdbcLogger.class);
			put("org.apache.log4j.Logger", Log4jJdbcLogger.class);
			put("java.util.logging.Logger", JdkJdbcLogger.class);
		}};

		// find use logger
		Constructor<?> c = null;
		for (Entry<String, Class<? extends JdbcLogger>> entry : checkLogger.entrySet()) {
			try {
				Class.forName(entry.getKey());
				c = entry.getValue().getConstructor(String.class);
				break;
			} catch (Exception e) {
				// do nothing
			}
		}
		return c;
	}

	/**
	 ********************************************
	 * get loggerName from jdbc url.
	 *
	 * @param url connection url
	 * @return loggerName(jdbc url or DataSourceName)
	 ********************************************
	 */
	private static String getLoggerName(String url) {
		if (!dsNameMap.containsKey(url)) {
			dsNameMap.put(url, url);

			// datasource名の解決を試みる
			InitialContext ctx = null;
			try {
				// JNDIによる名称の取得を試みる
				ctx = new InitialContext();
				NamingEnumeration<NameClassPair> ne = ctx.list("java:comp/env/jdbc");
				while (ne.hasMoreElements()) {
					NameClassPair nc = ne.nextElement();
					DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/" + nc.getName());
					// 一般的なDataSourceの実装に含まれるgetUrl()を利用
					Method method = ds.getClass().getMethod("getUrl");
					String dsUrl = (String) method.invoke(ds);
					if (dsUrl.startsWith(DriverEx.DRIVER_URL_PREFIX)) {
						dsUrl = dsUrl.replace(DriverEx.DRIVER_URL_PREFIX, "jdbc:");
						if (dsUrl.equals(url)) {
							dsNameMap.put(url, nc.getName());
							break;
						}
					}
				}
			} catch (Exception e) {
				// do nothing.
			} finally {
				try { if (ctx != null) ctx.close(); } catch(Exception e) {}
			}
		}
		return dsNameMap.get(url);
	}

	/**
	 ********************************************
	 * get logger from jdbc url.
	 *
	 * @param url connection url
	 * @return logger
	 ********************************************
	 */
	public static JdbcLogger getLogger(String url) {
		String name = getLoggerName(url);
		synchronized (loggerCache) {
			if (!hasLogger(name)) {
				try {
					loggerCache.put(name, (JdbcLogger)constructor.newInstance(name));
					if (settingMap.containsKey(DEFAULT_SETTING)) {
						// 事前に共通設定を反映
						loggerCache.get(name).setSetting(settingMap.get(DEFAULT_SETTING));
					}
					loggerCache.get(name).setSetting(settingMap.get(name));
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return loggerCache.get(name);
	}

	/**
	 ********************************************
	 * 指定URL用のロガーを保持しているかをチェックする
	 *
	 * @param url 接続URL文字列
	 * @return 保持している場合はtrue，保持していない場合はfalseを返す
	 ********************************************
	 */
	private static boolean hasLogger(String url) {
		return loggerCache.containsKey(getLoggerName(url));
	}

	/**
	 ********************************************
	 * hss valid loggger (exclude default logger)
	 * @return
	 ********************************************
	 */
	public static boolean hasValidLogger() {
		return loggerCache.size() > 1;
	}

	/**
	 ********************************************
	 * get default logger.
	 *
	 * @return default logger
	 ********************************************
	 */
	public static JdbcLogger getLogger() {
		return getLogger(DEFAULT_LOGGER);
	}

	/**
	 ********************************************
	 * get enable logger list(exclude default logger).
	 *
	 * @return logger list
	 ********************************************
	 */
	public static List<JdbcLogger> getLoggers() {
		List<JdbcLogger> loggers = new ArrayList<JdbcLogger>();
		for (Entry<String, JdbcLogger> logger : loggerCache.entrySet()) {
			if (!logger.getKey().equals(DEFAULT_LOGGER)) {
				loggers.add(logger.getValue());
			}
		}
		return loggers;
	}

	/**
	 ********************************************
	 * get logger setting.
	 *
	 * @param loggerName loggerName
	 * @return setting map
	 ********************************************
	 */
	public static Map<String,Object> getSetting(String loggerName) {
		return settingMap.get(loggerName);
	}
}
