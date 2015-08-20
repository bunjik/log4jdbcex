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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * @author f.kinoshtia
 *
 */
public class StatementEx extends LoggerHelper implements Statement {

	/** 実際のstatementオブジェクト */
	private Statement realStmt;

	/**
	 ********************************************
	 * コンストラクタ
	 * @param conn
	 * @param statement
	 ********************************************
	 */
	public StatementEx(ConnectionEx connEx, Statement stmt) {
		super(connEx);
		this.realStmt = stmt;
	}

	/* (非 Javadoc)
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return realStmt.unwrap(iface);
	}

	/* (非 Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return realStmt.isWrapperFor(iface);
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#executeQuery(java.lang.String)
	 */
	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		startExecute(sql);
		try {
//try {
//	// for Test
//	Thread.sleep(20000);
//} catch(Exception e) {}

			return reportReturned(realStmt.executeQuery(sql));
		} catch(SQLException e) {
			reportException(e, sql);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#executeUpdate(java.lang.String)
	 */
	@Override
	public int executeUpdate(String sql) throws SQLException {
		startExecute(sql);
		try {
			return reportReturned(realStmt.executeUpdate(sql));
		} catch(SQLException e) {
			reportException(e, sql);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#close()
	 */
	@Override
	public void close() throws SQLException {
		// startExecue()と対で呼び出されなかった場合の保険
		//endExecute();
		realStmt.close();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getMaxFieldSize()
	 */
	@Override
	public int getMaxFieldSize() throws SQLException {
		return realStmt.getMaxFieldSize();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#setMaxFieldSize(int)
	 */
	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		realStmt.setMaxFieldSize(max);
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getMaxRows()
	 */
	@Override
	public int getMaxRows() throws SQLException {
		return realStmt.getMaxRows();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#setMaxRows(int)
	 */
	@Override
	public void setMaxRows(int max) throws SQLException {
		realStmt.setMaxRows(max);
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#setEscapeProcessing(boolean)
	 */
	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		realStmt.setEscapeProcessing(enable);
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getQueryTimeout()
	 */
	@Override
	public int getQueryTimeout() throws SQLException {
		return realStmt.getQueryTimeout();
}

	/* (非 Javadoc)
	 * @see java.sql.Statement#setQueryTimeout(int)
	 */
	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		realStmt.setQueryTimeout(seconds);
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#cancel()
	 */
	@Override
	public void cancel() throws SQLException {
		realStmt.cancel();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return realStmt.getWarnings();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		realStmt.clearWarnings();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#setCursorName(java.lang.String)
	 */
	@Override
	public void setCursorName(String name) throws SQLException {
		realStmt.setCursorName(name);
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String)
	 */
	@Override
	public boolean execute(String sql) throws SQLException {
		startExecute(sql);
		try {
			return reportReturned(realStmt.execute(sql));
		} catch(SQLException e) {
			reportException(e, sql);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getResultSet()
	 */
	@Override
	public ResultSet getResultSet() throws SQLException {
		return realStmt.getResultSet();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getUpdateCount()
	 */
	@Override
	public int getUpdateCount() throws SQLException {
		return realStmt.getUpdateCount();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getMoreResults()
	 */
	@Override
	public boolean getMoreResults() throws SQLException {
		return realStmt.getMoreResults();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#setFetchDirection(int)
	 */
	@Override
	public void setFetchDirection(int direction) throws SQLException {
		realStmt.getFetchDirection();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getFetchDirection()
	 */
	@Override
	public int getFetchDirection() throws SQLException {
		return realStmt.getFetchSize();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#setFetchSize(int)
	 */
	@Override
	public void setFetchSize(int rows) throws SQLException {
		realStmt.setFetchSize(rows);
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getFetchSize()
	 */
	@Override
	public int getFetchSize() throws SQLException {
		return realStmt.getFetchSize();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getResultSetConcurrency()
	 */
	@Override
	public int getResultSetConcurrency() throws SQLException {
		return realStmt.getResultSetConcurrency();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getResultSetType()
	 */
	@Override
	public int getResultSetType() throws SQLException {
		return realStmt.getResultSetType();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#addBatch(java.lang.String)
	 */
	@Override
	public void addBatch(String sql) throws SQLException {
		realStmt.addBatch(sql);
		addBatchList(sql);
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#clearBatch()
	 */
	@Override
	public void clearBatch() throws SQLException {
		realStmt.clearBatch();
		clearBatchList();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#executeBatch()
	 */
	@Override
	public int[] executeBatch() throws SQLException {
		try {
			startExecute();
			return reportReturned(realStmt.executeBatch());
		} catch(SQLException e) {
			reportException(e);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return connEx;
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getMoreResults(int)
	 */
	@Override
	public boolean getMoreResults(int current) throws SQLException {
		return realStmt.getMoreResults(current);
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getGeneratedKeys()
	 */
	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		return realStmt.getGeneratedKeys();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int)
	 */
	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		startExecute(sql);
		try {
			return reportReturned(realStmt.executeUpdate(sql, autoGeneratedKeys));
		} catch(SQLException e) {
			reportException(e);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
	 */
	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		startExecute(sql);
		try {
			return reportReturned(realStmt.executeUpdate(sql, columnIndexes));
		} catch(SQLException e) {
			reportException(e);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
	 */
	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		startExecute(sql);
		try {
			return reportReturned(realStmt.executeUpdate(sql, columnNames));
		} catch(SQLException e) {
			reportException(e);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String, int)
	 */
	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		startExecute(sql);
		try {
			return reportReturned(realStmt.execute(sql, autoGeneratedKeys));
		} catch(SQLException e) {
			reportException(e);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String, int[])
	 */
	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		startExecute(sql);
		try {
			return reportReturned(realStmt.execute(sql, columnIndexes));
		} catch(SQLException e) {
			reportException(e);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		startExecute(sql);
		try {
			return reportReturned(realStmt.execute(sql, columnNames));
		} catch(SQLException e) {
			reportException(e);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#getResultSetHoldability()
	 */
	@Override
	public int getResultSetHoldability() throws SQLException {
		return realStmt.getResultSetHoldability();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return realStmt.isClosed();
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#setPoolable(boolean)
	 */
	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		realStmt.setPoolable(poolable);
	}

	/* (非 Javadoc)
	 * @see java.sql.Statement#isPoolable()
	 */
	@Override
	public boolean isPoolable() throws SQLException {
		return realStmt.isPoolable();
	}
}
