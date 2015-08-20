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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author f.kinoshita
 */
public class ConnectionEx extends LoggerHelper implements Connection {

	/** 実際のコネクション */
	private Connection realConn;
	/** 接続URL */
	private String url;

	private String testConfigName;

	private boolean fakeAutoCommitMode;

	private AtomicInteger savePointId = new AtomicInteger();

	/**
	 ********************************************
	 * テスト用にコミットを無効化している場合のダミーSavePointクラス
	 * 実際には何も管理しません
	 ********************************************
	 */
	class DummySavePoint implements Savepoint {
		private int savePoint;
		private String name;

		DummySavePoint() {
			savePoint = savePointId.getAndIncrement();
			name = String.valueOf(savePoint);
		}
		DummySavePoint(String name) {
			savePoint = savePointId.getAndIncrement();
			this.name = name;
		}
		@Override
		public int getSavepointId() throws SQLException {
			return savePoint;
		}
		@Override
		public String getSavepointName() throws SQLException {
			return name;
		}
	}

	/**
	 ********************************************
	 * コンストラクタ
	 ********************************************
	 */
	public ConnectionEx(Connection conn, String url, String testConfigName) throws SQLException {
		super(null);
		this.realConn = conn;
		this.url = url;
		this.testConfigName = testConfigName;
		setConnectionEx(this);

		// オートコミットの設定を独自に管理する
		if (isFakeCommit()) {
			fakeAutoCommitMode = realConn.getAutoCommit();
			// オートコミットを無効化
			realConn.setAutoCommit(false);
		}
	}

	/**
	 ********************************************
	 * 実際にDBと接続しているConnectionを取得する
	 *
	 * @return 実際のConnectionオブジェクト
	 **********************************************
	 */
	protected Connection getRealConnection() {
		return realConn;
	}

	/**
	 ********************************************
	 * テスト用の設定ファイル名を取得する
	 * @return テスト設定ファイル名。未設定時はnullを返す
	 ********************************************
	 */
	protected String getTestConfigName() {
		return testConfigName;
	}

	/**
	 **********************************************
	 * dbの接続urlを取得する
	 * @return 接続url
	 **********************************************
	 */
	public String getUrl() {
		return url;
	}

	/* (非 Javadoc)
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return realConn.unwrap(iface);
	}

	/* (非 Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return realConn.isWrapperFor(iface);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#createStatement()
	 */
	@Override
	public Statement createStatement() throws SQLException {
		Statement stmt = realConn.createStatement();
		if (logger.isJdbcLoggingEnabled()) {
			stmt = new StatementEx(this, stmt);
		}
		return stmt;
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement ps = realConn.prepareStatement(sql);
		if (logger.isJdbcLoggingEnabled()) {
			ps = new PreparedStatementEx(this, sql, ps);
		}
		return ps;
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		CallableStatement cs = realConn.prepareCall(sql);
		if (logger.isJdbcLoggingEnabled()) {
			cs = new CallableStatementEx(this, sql, cs);
		}
		return cs;
	}

	/* (非 Javadoc)

	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	@Override
	public String nativeSQL(String sql) throws SQLException {
		return realConn.nativeSQL(sql);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if (!isFakeCommit()) {
			realConn.setAutoCommit(autoCommit);
		} else {
			fakeAutoCommitMode = autoCommit;
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#getAutoCommit()
	 */
	@Override
	public boolean getAutoCommit() throws SQLException {
		if (!isFakeCommit()) {
			return realConn.getAutoCommit();
		} else {
			return fakeAutoCommitMode;
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#commit()
	 */
	@Override
	public void commit() throws SQLException {
		if (!isFakeCommit()) {
			realConn.commit();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#rollback()
	 */
	@Override
	public void rollback() throws SQLException {
		realConn.rollback();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#close()
	 */
	@Override
	public void close() throws SQLException {
		// commitが無効化されている場合は、クローズ前にロールバックを実行
		if (isFakeCommit()) {
			realConn.rollback();
			logger.info("fake commit enabled.(transaction rollbacked)");
		}
		realConn.close();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return realConn.isClosed();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#getMetaData()
	 */
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return realConn.getMetaData();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		realConn.setReadOnly(readOnly);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() throws SQLException {
		return realConn.isReadOnly();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	@Override
	public void setCatalog(String catalog) throws SQLException {
		realConn.setCatalog(catalog);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#getCatalog()
	 */
	@Override
	public String getCatalog() throws SQLException {
		return realConn.getCatalog();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		realConn.setTransactionIsolation(level);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	@Override
	public int getTransactionIsolation() throws SQLException {
		return realConn.getTransactionIsolation();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return realConn.getWarnings();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		realConn.clearWarnings();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		Statement stmt = realConn.createStatement(resultSetType, resultSetConcurrency);
		if (logger.isJdbcLoggingEnabled()) {
			stmt = new StatementEx(this, stmt);
		}
		return stmt;
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		PreparedStatement ps = realConn.prepareCall(sql, resultSetType, resultSetConcurrency);
		if (logger.isJdbcLoggingEnabled()) {
			ps = new PreparedStatementEx(this, sql, ps);
		}
		return ps;
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return realConn.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#getTypeMap()
	 */
	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return realConn.getTypeMap();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		realConn.setTypeMap(map);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#setHoldability(int)
	 */
	@Override
	public void setHoldability(int holdability) throws SQLException {
		realConn.setHoldability(holdability);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#getHoldability()
	 */
	@Override
	public int getHoldability() throws SQLException {
		return realConn.getHoldability();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#setSavepoint()
	 */
	@Override
	public Savepoint setSavepoint() throws SQLException {
		if (!isFakeCommit()) {
			return realConn.setSavepoint();
		} else {
			return new DummySavePoint();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		if (!isFakeCommit()) {
			return realConn.setSavepoint(name);
		} else {
			return new DummySavePoint(name);
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		realConn.rollback();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		if (!isFakeCommit()) {
			realConn.releaseSavepoint(savepoint);
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		Statement stmt = realConn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
		if (logger.isJdbcLoggingEnabled()) {
			stmt = new StatementEx(this, stmt);
		}
		return stmt;
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		PreparedStatement ps = realConn.prepareStatement(sql, resultSetType, resultSetConcurrency);
		if (logger.isJdbcLoggingEnabled()) {
			ps = new PreparedStatementEx(this, sql, ps);
		}
		return ps;
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return realConn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		PreparedStatement ps = realConn.prepareStatement(sql, autoGeneratedKeys);
		if (logger.isJdbcLoggingEnabled()) {
			ps = new PreparedStatementEx(this, sql, ps);
		}
		return ps;
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		PreparedStatement ps = realConn.prepareStatement(sql, columnIndexes);
		if (logger.isJdbcLoggingEnabled()) {
			ps = new PreparedStatementEx(this, sql, ps);
		}
		return ps;
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		PreparedStatement ps = realConn.prepareStatement(sql, columnNames);
		if (logger.isJdbcLoggingEnabled()) {
			ps = new PreparedStatementEx(this, sql, ps);
		}
		return ps;
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#createClob()
	 */
	@Override
	public Clob createClob() throws SQLException {
		return realConn.createClob();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#createBlob()
	 */
	@Override
	public Blob createBlob() throws SQLException {
		return realConn.createBlob();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#createNClob()
	 */
	@Override
	public NClob createNClob() throws SQLException {
		return realConn.createNClob();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#createSQLXML()
	 */
	@Override
	public SQLXML createSQLXML() throws SQLException {
		return realConn.createSQLXML();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#isValid(int)
	 */
	@Override
	public boolean isValid(int timeout) throws SQLException {
		return realConn.isValid(timeout);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
	 */
	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		realConn.setClientInfo(name, value);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#setClientInfo(java.util.Properties)
	 */
	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		realConn.setClientInfo(properties);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	@Override
	public String getClientInfo(String name) throws SQLException {
		return realConn.getClientInfo(name);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#getClientInfo()
	 */
	@Override
	public Properties getClientInfo() throws SQLException {
		return realConn.getClientInfo();
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return realConn.createArrayOf(typeName, elements);
	}

	/* (非 Javadoc)
	 * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return realConn.createStruct(typeName, attributes);
	}
}
