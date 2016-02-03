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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 * @author Fumiharu Kinoshita
 */
public class DriverEx implements java.sql.Driver {

	/** driver name */
	public static final String DRIVER_NAME = "log4jdbcex";

	/** connection url prefix */
	public static final String DRIVER_URL_PREFIX = "jdbc:" + DRIVER_NAME + ":";

	static {
		Set<String> subDrivers = new TreeSet<String>();

		// 一般的なドライバクラスは明示的にロード
		subDrivers.add("oracle.jdbc.driver.OracleDriver");
		subDrivers.add("oracle.jdbc.OracleDriver");
		subDrivers.add("com.sybase.jdbc2.jdbc.SybDriver");
		subDrivers.add("net.sourceforge.jtds.jdbc.Driver");
		subDrivers.add("com.microsoft.jdbc.sqlserver.SQLServerDriver"); // SQLServer 2000
		subDrivers.add("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // SQLServer 2005
		subDrivers.add("weblogic.jdbc.sqlserver.SQLServerDriver");
		subDrivers.add("com.informix.jdbc.IfxDriver");
		subDrivers.add("org.apache.derby.jdbc.ClientDriver");
		subDrivers.add("org.apache.derby.jdbc.EmbeddedDriver");
		subDrivers.add("com.mysql.jdbc.Driver");
		subDrivers.add("org.postgresql.Driver");
		subDrivers.add("org.hsqldb.jdbcDriver");
		subDrivers.add("org.h2.Driver");

		// クラスパスに存在する既知のドライバをロードする
		// (JDBC4.0対応のドライバなら不要だが害はないため実行しておく)
		Iterator<String> it = subDrivers.iterator();
		while (it.hasNext()) {
			try {
				Class.forName(it.next());
			} catch (Throwable c) {
				// do nothing.
			}
		}

		// 自分自身をDriverManagerに登録
		try {
			DriverManager.registerDriver(ProxyFactory.wrapDriver());
		} catch (Exception s) {
			throw new RuntimeException("could not register driver!", s);
		}
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		Driver driver = ProxyFactory.wrapDriver();
		return driver.connect(url, info);
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see java.sql.Driver#acceptsURL(java.lang.String)
	 */
	@Override
	public boolean acceptsURL(String url) throws SQLException {
		Driver driver = ProxyFactory.wrapDriver();
		return driver.acceptsURL(url);
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see java.sql.Driver#getPropertyInfo(java.lang.String,
	 * java.util.Properties)
	 */
	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return new DriverPropertyInfo[0];
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see java.sql.Driver#getMajorVersion()
	 */
	@Override
	public int getMajorVersion() {
		return 1;
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see java.sql.Driver#getMinorVersion()
	 */
	@Override
	public int getMinorVersion() {
		return 0;
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see java.sql.Driver#jdbcCompliant()
	 */
	//@Override
	public boolean jdbcCompliant() {
		return false;
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see java.sql.Driver#getParentLogger()
	 */
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
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
//	public static String[] extractSettingPath(String url) {
//		// [0]:本来のURL [1]:設定ファイル名
//		String[] ret = new String[2];
//
//		String configParam = "loggerconfig=";
//		int bgnPos = url.toLowerCase().indexOf(configParam);
//		if (bgnPos != -1) {
//			String regex = "(?i)" + configParam + ".*?";
//			int endPos = url.indexOf(";", bgnPos);
//			if(endPos != -1) {
//				regex += ";";
//			}
//			ret[0] = url.replaceAll(regex, "");
//			ret[1] = url.substring(bgnPos + configParam.length());
//			ret[1] = ret[1].replaceAll(";", "");
//		} else {
//			ret[0] = url;
//		}
//		return ret;
//	}

}
