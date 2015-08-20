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

import info.bunji.jdbc.DriverEx;
import info.bunji.jdbc.logger.impl.CommonsLoggingJdbcLogger;
import info.bunji.jdbc.logger.impl.JdkJdbcLogger;
import info.bunji.jdbc.logger.impl.Log4jJdbcLogger;
import info.bunji.jdbc.logger.impl.Slf4jJdbcLogger;

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

import net.arnx.jsonic.JSON;

/**
 *
 * 設定ファイルの形式
 *
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
 *
 * @author f.kinoshita
 */
public class JdbcLoggerFactory {

	private static final String SETTING_FILE = DriverEx.DRIVER_NAME + ".json";

	private static final String DEFAULT_SETTING = "_default_";

	private static final String DEFAULT_LOGGER = "_defaultLogger_";

	private static Map<String,JdbcLogger> loggerCache = new HashMap<String,JdbcLogger>();

	private static Map<String,String> dsNameMap = new HashMap<String,String>();

	private static Constructor<?> constructor;

	/** 設定ファイルの設定情報を保持する */
	private static Map<String,Map<String,Object>> settingMap = new HashMap<String,Map<String,Object>>();

	static {
		// 利用可能なLoggingライブラリを検索する(優先順を考慮)
		Map<String, Class<? extends JdbcLogger>> checkLogger = new LinkedHashMap<String, Class<? extends JdbcLogger>>() {{
			put("org.slf4j.Logger", Slf4jJdbcLogger.class);
			put("org.apache.commons.logging.Log", CommonsLoggingJdbcLogger.class);
			put("org.apache.log4j.Logger", Log4jJdbcLogger.class);
			put("java.util.logging.Logger", JdkJdbcLogger.class);
		}};

		// 利用するLoggerを決定する
		for (Entry<String, Class<? extends JdbcLogger>> entry : checkLogger.entrySet()) {
			try {
				// 見つかったらそのクラスを利用する
				Class.forName(entry.getKey());
				constructor = entry.getValue().getConstructor(String.class);
				//System.out.println("binding logger(" + entry.getKey() + ")");
				break;
			} catch (Exception e) {}
		}

		InputStream is = null;
		try {
			// 設定ファイルの読み込み
			is = JdbcLoggerFactory.class.getResourceAsStream(SETTING_FILE);
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

		InitialContext ctx = null;
		try {
			// JNDIによる名称の取得を試みる
			ctx = new InitialContext();
			NamingEnumeration<NameClassPair> ne = ctx.list("java:comp/env/jdbc");
			while (ne.hasMoreElements()) {
				NameClassPair nc = ne.nextElement();
				DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/" + nc.getName());

				// 一般的なDataSourceの実装にはgetUrl()が含まれるため、それを利用
				Method method = ds.getClass().getMethod("getUrl");
				String url = (String) method.invoke(ds);
				url = url.replace(DriverEx.DRIVER_URL_PREFIX, "jdbc:");
				//System.out.println(nc.getName() + " = " + url);
				dsNameMap.put(url, nc.getName());
			}
		} catch (Exception e) {
			// do nothing.
		} finally {
			try { if (ctx != null) ctx.close(); } catch(Exception e) {}
		}
	}

	/**
	 ********************************************
	 * コンストラクタ
	 ********************************************
	 */
	private JdbcLoggerFactory() {
		// can't call private constructor.
	}

	/**
	 ********************************************
	 * 指定されたURLのロガー名(可能ならDataSource名)を取得する
	 * 取得できない場合は、URLをそのまま返す
	 * @param url 接続URL
	 * @return JNDIのデータソース名が取得できない場合はURLをそのまま返す
	 ********************************************
	 */
	private static String getLoggerName(String url) {
		String name = dsNameMap.get(url);
		return name != null ? name : url;
	}

	/**
	 ********************************************
	 * 接続URLに応じたロガーを取得する.
	 *
	 * この実装では、接続ユーザによる区別は想定していません
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
	public static boolean hasLogger(String url) {
		return loggerCache.containsKey(getLoggerName(url));
	}

	/**
	 ********************************************
	 * デフォルトのロガーを取得する.
	 *
	 * @return デフォルトのロガー（接続URLとは紐付かない）
	 ********************************************
	 */
	public static JdbcLogger getLogger() {
		return getLogger(DEFAULT_LOGGER);
	}

	/**
	 ********************************************
	 * 現在保持しているLoggerの一覧を取得する。
	 * ただし、デフォルトのLoggerは除外する。
	 *
	 * @return 有効なLoggerの一覧
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
	 * 接続URLに応じた設定情報を取得する
	 * @param name
	 * @return
	 ********************************************
	 */
	public static Map<String,Object> getSetting(String name) {
		return settingMap.get(name);
	}
}
