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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * implements Statement Wrapper.
 * <pre>
 * wrapped for
 *  Statement
 *  PreparedStatement
 *  CallableStatement
 * </pre>
 * @author f.kinoshita
 */
public class StatementProxy extends LoggerHelper implements InvocationHandler {

	/** 実際のインスタンス */
	private Statement _stmt;

	/** ロギング対象のメソッド */
	private static final Set<String> loggingMethods = new HashSet<String>(
			Arrays.asList("execute", "executeQuery", "executeUpdate",  "executeBatch",
							"executeLargeUpdate", "executeLargeBatch"));

	private static final Map<String, Integer> paramTypes = new HashMap<String, Integer>() {{
		// setArray(int parameterIndex, Array x)
		put("setArray", Types.ARRAY);
		// setAsciiStream(int parameterIndex, InputStream x)
		// setAsciiStream(int parameterIndex, InputStream x, int length)
		// setAsciiStream(int parameterIndex, InputStream x, long length)
		put("setAsciiStream", Types.JAVA_OBJECT);
		// setBigDecimal(int parameterIndex, BigDecimal x)
		put("setBigDecimal", Types.NUMERIC);
		// setBinaryStream(int parameterIndex, InputStream x)
		// setBinaryStream(int parameterIndex, InputStream x, int length)
		// setBinaryStream(int parameterIndex, InputStream x, long length)
		put("setBinaryStream", Types.JAVA_OBJECT);
		// setBlob(int parameterIndex, Blob x)
		// setBlob(int parameterIndex, InputStream inputStream)
		// setBlob(int parameterIndex, InputStream inputStream, long length)
		put("setBlob", Types.BLOB);
		// setBoolean(int parameterIndex, boolean x)
		put("setBoolean", Types.BOOLEAN);
		// setByte(int parameterIndex, byte x)
		put("setByte", Types.TINYINT);
		// setBytes(int parameterIndex, byte[] x)
		put("setBytes", Types.BINARY);
		// setCharacterStream(int parameterIndex, Reader reader)
		// setCharacterStream(int parameterIndex, Reader reader, int length)
		// setCharacterStream(int parameterIndex, Reader reader, long length)
		put("setCharacterStream", Types.JAVA_OBJECT);
		// setClob(int parameterIndex, Clob x)
		// setClob(int parameterIndex, Reader reader)
		// setClob(int parameterIndex, Reader reader, long length)
		put("setClob", Types.CLOB);
		// setDate(int parameterIndex, Date x)
		// setDate(int parameterIndex, Date x, Calendar cal)
		put("setDate", Types.DATE);
		// setDouble(int parameterIndex, double x)
		put("setDouble", Types.DOUBLE);
		// setFloat(int parameterIndex, float x)
		put("setFloat", Types.FLOAT);
		// setInt(int parameterIndex, int x)
		put("setInt", Types.INTEGER);
		// setLong(int parameterIndex, long x)
		put("setLong", Types.BIGINT);
		// setNCharacterStream(int parameterIndex, Reader value)
		// setNCharacterStream(int parameterIndex, Reader value, long length)
		put("setNCharacterStream", null);
		// setNClob(int parameterIndex, NClob value)
		// setNClob(int parameterIndex, Reader reader)
		// setNClob(int parameterIndex, Reader reader, long length)
		put("setNClob", Types.NCLOB);
		// setNString(int parameterIndex, String value)
		put("setNString", Types.NCHAR);
		// setNull(int parameterIndex, int sqlType)
		// setNull(int parameterIndex, int sqlType, String typeName)
		put("setNull", null);
		// setObject(int parameterIndex, Object x)
		// setObject(int parameterIndex, Object x, int targetSqlType)
		// setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
		put("setObject", Types.JAVA_OBJECT);
		// setRef(int parameterIndex, Ref x)
		put("setRef", Types.REF);
		// setRowId(int parameterIndex, RowId x)
		put("setRowId", Types.ROWID);
		// setShort(int parameterIndex, short x)
		put("setShort", Types.SMALLINT);
		// setSQLXML(int parameterIndex, SQLXML xmlObject)
		put("setSQLXML", null);
		// setString(int parameterIndex, String x)
		put("setString", Types.CHAR);
		// setTime(int parameterIndex, Time x)
		// setTime(int parameterIndex, Time x, Calendar cal)
		put("setTime", Types.TIME);
		// setTimestamp(int parameterIndex, Timestamp x)
		// setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
		put("setTimestamp", Types.TIMESTAMP);
		// setUnicodeStream(int parameterIndex, InputStream x, int length)
		put("setUnicodeStream", null);
		// setURL(int parameterIndex, URL x)
		put("setURL", null);
	}};

	/** プロシージャのパラメータ名を保持するMap */
	private static Map<String, Map<String,Integer>> procedureInfo = new HashMap<String, Map<String,Integer>>();

	/** プロシージャ名を抽出する正規表現 */
	private static final Pattern PROC_PATTERN = Pattern.compile("call (.*)\\(", Pattern.CASE_INSENSITIVE);

	/**
	 *
	 * @param instance
	 * @param url
	 */
	StatementProxy(Statement instance, String url) {
		super(url);
		_stmt = instance;
	}

	/**
	 *
	 * @param instance
	 * @param url
	 * @param sql
	 */
	StatementProxy(Statement instance, String url, String sql) {
		this(instance, url);
		setSql(sql);
	}

	/*
	 * (非 Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object ret = null;
		String name = method.getName();
		if (loggingMethods.contains(name)) {
			String sql = getSql();
			if (args != null && args.length > 0) {
				sql = args[0] != null ? args[0].toString() : null;
			}
			try {
				startExecute(sql);
				ret = method.invoke(_stmt, args);
				if(name.endsWith("Batch")) {
					reportBatchReturned();
				} else {
					reportReturned();
				}
			} catch(InvocationTargetException e) {
				reportException(e.getCause(), sql);
				throw e.getCause();
			} finally {
				endExecute();
			}
		} else {
			try {
				ret = method.invoke(_stmt, args);
			} catch(InvocationTargetException e) {
				throw e.getCause();
			}

			if (name.equals("addBatch")){
				addBatchList(args);
			} else if (name.equals("clearBatch")){
				clearBatchList();
			} else if (name.equals("clearParameters")){
				clearParameterList();
/*			} else if (name.equals("registerOutParameter")){
				int sqlType = (Integer) args[1];
				try {
					if (args[0] instanceof String) {
						//String parameterName = (String) args[0];
						//addParameter(getParameterIndex(parameterName), sqlType, "(OUT)");
						addParameter((String) args[0], sqlType, "(OUT)");
					} else {
						Integer parameterIndex = (Integer) args[0];
						addParameter(parameterIndex, sqlType, "(OUT)");
					}
				} catch (Exception e) {
					// do nothing.
				}
*/			} else if (name.equals("getConnection")){
				// 取得したConnectionをラップする
				ret = ProxyFactory.wrapConnection((Connection)ret, url);
			} else if (paramTypes.containsKey(name)) {
				try {
					Integer type = paramTypes.get(name);
					Object  value = args[1];
					if (type == null) {
						type = Types.JAVA_OBJECT;
						value = "(" + name.substring(3) + ")";
					}

					if (method.getParameterTypes()[0].equals(String.class)) {
						// CallableStatment用
						addParameter(getParameterIndex((String)args[0]), type, value);
					} else {
						addParameter((Integer)args[0], type, value);
					}
				} catch (Exception e) {
					// do nothing.
				}
			}
		}
		return ret;
	}

	/**
	 * パラメータ名から項目のインデックスを取得する
	 *
	 * ※非効率な処理
	 *
	 * @param colName カラム名
	 * @return パラメータ名に対応するインデックス値。取得できなかった場合は-1を返す
	 */
	private int getParameterIndex(String colName) {
		// プロシージャ名を抽出する
		Matcher matcher = PROC_PATTERN.matcher(getSql());
		if (!matcher.find()) {
			// 抽出できなかった場合
			return -1;
		}
		String procName = matcher.group(1).trim();

		String name = procName;
		if (!procedureInfo.containsKey(procName)) {
			// 未取得の場合は情報を取得する
			try {
				String catalog = null;
				int pos = procName.indexOf(".");
				if (pos != -1) {
					catalog = procName.substring(0, pos).toUpperCase();
					pos = procName.lastIndexOf(".");
					name = procName.substring(pos + 1);
				} else {
					catalog = _stmt.getConnection().getCatalog();
				}

				Map<String,Integer> procInfo = new HashMap<String,Integer>();

				DatabaseMetaData meta = _stmt.getConnection().getMetaData();
				ResultSet rs = meta.getProcedureColumns(catalog, null, name.toUpperCase(), "%");
				while (rs.next()) {
					// パラメータ名をキーにインデックスを保持する
					procInfo.put(rs.getString("COLUMN_NAME").toUpperCase(), rs.getInt("ORDINAL_POSITION"));
				}
				procedureInfo.put(name, procInfo);
				rs.close();
			} catch (SQLException sqle) {
				// do nothting.
			}
		}

		// カラム名からインデックス値を取得する
		Map<String,Integer> colInfo = procedureInfo.get(procName);
		if (colInfo != null) {
			Integer index = colInfo.get(colName.toUpperCase());
			return index != null ? index.intValue() : -1;
		}
		return -1;
	}

}
