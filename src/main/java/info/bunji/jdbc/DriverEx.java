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
package info.bunji.jdbc;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author f.kinoshita
 */
public class DriverEx implements Driver {

	public static final String DRIVER_NAME = "log4jdbcex";

	/** このドライバの接続URLの前置詞 */
	public static final String DRIVER_URL_PREFIX = "jdbc:" + DRIVER_NAME + ":";

	public static JdbcLogger logger = JdbcLoggerFactory.getLogger();

	/** 実際にDBに接続するドライバ */
	protected Driver realDriver;

	static {
		Set<String> subDrivers = new TreeSet<String>();

		// 一般的なドライバクラスは明示的にロード
		subDrivers.add("oracle.jdbc.driver.OracleDriver");
		subDrivers.add("oracle.jdbc.OracleDriver");
		subDrivers.add("com.sybase.jdbc2.jdbc.SybDriver");
		subDrivers.add("net.sourceforge.jtds.jdbc.Driver");
		subDrivers.add("com.microsoft.jdbc.sqlserver.SQLServerDriver");	// SQLServer 2000
		subDrivers.add("com.microsoft.sqlserver.jdbc.SQLServerDriver");	// SQLServer 2005
		subDrivers.add("weblogic.jdbc.sqlserver.SQLServerDriver");
		subDrivers.add("com.informix.jdbc.IfxDriver");
		subDrivers.add("org.apache.derby.jdbc.ClientDriver");
		subDrivers.add("org.apache.derby.jdbc.EmbeddedDriver");
		subDrivers.add("com.mysql.jdbc.Driver");
		subDrivers.add("org.postgresql.Driver");
		subDrivers.add("org.hsqldb.jdbcDriver");
		subDrivers.add("org.h2.Driver");

		// 自分自身をDriverManagerに登録
		try {
			DriverManager.registerDriver(new DriverEx());
		} catch (SQLException s) {
			throw (RuntimeException) new RuntimeException("could not register driver!").initCause(s);
		}

		// クラスパスに存在するドライバをロードする
		// (JDBC4.0対応のドライバなら不要だが害はないため実行しておく)
		Iterator<String> it = subDrivers.iterator();
		while (it.hasNext()) {
			String driverClass = it.next();
			try {
				Class.forName(driverClass);
				logger.info("FOUND DRIVER " + driverClass);
			} catch (Throwable c) {
				it.remove();
			}
		}

		// JDBCドライバを列挙する
		int count = 0;
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while(drivers.hasMoreElements()) {
			drivers.nextElement();
			count++;
		}
		// 自分以外のDriverが見つからない場合
		if (count <= 1) {
			logger.info("WARNING! jdbc driver not found.");
		}
	}

	/**
	 * 接続URLから設定ファイルのパラメータを抽出する
	 * <pre>
	 * 接続URLに設定ファイルのパラメータがあれば、ファイル名として抽出する。
	 * パラメータがなければnullを返す。
	 * </pre>
	 * @param url 接続URL
	 * @return [0]:設定ファイルのパラメータを除去した接続URL [1]:設定ファイル名
	 */
	public static String[] extractSettingPath(String url) {
		// [0]:本来のURL [1]:設定ファイル名
		String[] ret = new String[2];

		String configParam = "loggerconfig=";
		int bgnPos = url.toLowerCase().indexOf(configParam);
		if (bgnPos != -1) {
			String regex = "(?i)" + configParam + ".*?";
			int endPos = url.indexOf(";", bgnPos);
			if(endPos != -1) {
				regex += ";";
			}
			ret[0] = url.replaceAll(regex, "");
			ret[1] = url.substring(bgnPos + configParam.length());
			ret[1] = ret[1].replaceAll(";", "");
		} else {
			ret[0] = url;
		}
		return ret;
	}

	private Driver getUnderlyingDriver(String url) throws SQLException {

		if (url.startsWith(DRIVER_URL_PREFIX)) {
			// 実際の接続URLを抽出する
			String realUrl = extractSettingPath("jdbc:" + url.substring(DRIVER_URL_PREFIX.length()))[0];

			Enumeration<Driver> e = DriverManager.getDrivers();
			while (e.hasMoreElements()) {
				Driver d = e.nextElement();

				// 自分自身は除く
				if (d.getClass().equals(getClass())) continue;

				// URLを受け付けるドライバかを判定
				if (d.acceptsURL(realUrl)) {
					return d;
				}
			}
		}
		return null;
	}

	/* (非 Javadoc)
	 * @see java.sql.Driver#acceptsURL(java.lang.String)
	 */
	@Override
	public boolean acceptsURL(String url) throws SQLException {
		Driver d = getUnderlyingDriver(url);
		if (d != null) {
			realDriver = d;
			return true;
		} else {
			return false;
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	@Override
	public Connection connect(String url, Properties info) throws SQLException {

		Driver d = getUnderlyingDriver(url);
		if (d == null) {
			return null;
		}

		realDriver = d;
		String[] parsedUrl = extractSettingPath("jdbc:" + url.substring(DRIVER_URL_PREFIX.length()));
		String realUrl = parsedUrl[0];

		Connection conn = d.connect(realUrl, info);
		if (conn == null) {
			throw new SQLException("invalid or unknown driver url: " + realUrl);
		}

		// ロギングが有効、またはCSVロードが有効な場合は、ラップしたコネクションを返す
		if (logger.isJdbcLoggingEnabled() || parsedUrl[1] != null) {
			conn = new ConnectionEx(conn, realUrl, parsedUrl[1]);
		}
		return conn;
	}

	/* (非 Javadoc)
	 * @see java.sql.Driver#getMajorVersion()
	 */
	@Override
	public int getMajorVersion() {
		return realDriver == null ? 1 : realDriver.getMajorVersion();
	}

	/* (非 Javadoc)
	 * @see java.sql.Driver#getMinorVersion()
	 */
	@Override
	public int getMinorVersion() {
		return realDriver == null ? 0 : realDriver.getMinorVersion();
	}

	/* (非 Javadoc)
	 * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
	 */
	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		Driver d = getUnderlyingDriver(url);
		if (d == null) {
			return new DriverPropertyInfo[0];
		}
		realDriver = d;
		return d.getPropertyInfo(url, info);
	}

	/* (非 Javadoc)
	 * @see java.sql.Driver#jdbcCompliant()
	 */
	@Override
	public boolean jdbcCompliant() {
		return realDriver != null && realDriver.jdbcCompliant();
	}
}
