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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

/**
 *
 * @author f.kinoshita
 */
public class PreparedStatementEx extends StatementEx implements PreparedStatement {

	/** 実際のPreparedStatementオブジェクト */
	private PreparedStatement realStmt;

	/**
	 ********************************************
	 * コンストラクタ
	 * @param conn
	 * @param statement
	 ********************************************
	 */
	PreparedStatementEx(ConnectionEx connEx, String sql, PreparedStatement statement) {
		super(connEx, statement);
		this.realStmt = statement;
		setSql(sql);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#executeQuery()
	 */
	@Override
	public ResultSet executeQuery() throws SQLException {
		startExecute();
		try {
			return reportReturned(realStmt.executeQuery());
		} catch(SQLException e) {
			reportException(e);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#executeUpdate()
	 */
	@Override
	public int executeUpdate() throws SQLException {
		startExecute();
		try {
			return reportReturned(realStmt.executeUpdate());
		} catch(SQLException e) {
			reportException(e);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setNull(int, int)
	 */
	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		realStmt.setNull(parameterIndex, sqlType);
		addParameter(parameterIndex, sqlType, null);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setBoolean(int, boolean)
	 */
	@Override
	public void setBoolean(int parameterIndex, boolean b) throws SQLException {
		realStmt.setBoolean(parameterIndex, b);
		addParameter(parameterIndex, Types.BOOLEAN, b);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setByte(int, byte)
	 */
	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
		realStmt.setByte(parameterIndex, x);
		addParameter(parameterIndex, Types.TINYINT, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setShort(int, short)
	 */
	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
		realStmt.setShort(parameterIndex, x);
		addParameter(parameterIndex, Types.SMALLINT, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setInt(int, int)
	 */
	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		realStmt.setInt(parameterIndex, x);
		addParameter(parameterIndex, Types.INTEGER, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setLong(int, long)
	 */
	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
		realStmt.setLong(parameterIndex, x);
		addParameter(parameterIndex, Types.BIGINT, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setFloat(int, float)
	 */
	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
		realStmt.setFloat(parameterIndex, x);
		addParameter(parameterIndex, Types.FLOAT, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setDouble(int, double)
	 */
	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
		realStmt.setDouble(parameterIndex, x);
		addParameter(parameterIndex, Types.DOUBLE, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
	 */
	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		realStmt.setBigDecimal(parameterIndex, x);
		addParameter(parameterIndex, Types.NUMERIC, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setString(int, java.lang.String)
	 */
	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		realStmt.setString(parameterIndex, x);
		addParameter(parameterIndex, Types.CHAR, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setBytes(int, byte[])
	 */
	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		realStmt.setBytes(parameterIndex, x);
		addParameter(parameterIndex, Types.BINARY, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
	 */
	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		realStmt.setDate(parameterIndex, x);
		addParameter(parameterIndex, Types.DATE, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
	 */
	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		realStmt.setTime(parameterIndex, x);
		addParameter(parameterIndex, Types.TIME, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
	 */
	@Override
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		realStmt.setTimestamp(parameterIndex, x);
		addParameter(parameterIndex, Types.TIMESTAMP, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
	 */
	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		realStmt.setAsciiStream(parameterIndex, x, length);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(AsciiStream[len=" + length + "])");
	}

	/**
	 * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream, int)
	 * @deprecated
	 */
	@Override
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
		realStmt.setUnicodeStream(parameterIndex, x, length);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(UnicodeStream[len=" + length + "])");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
	 */
	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
		realStmt.setBinaryStream(parameterIndex, x, length);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(BinaryStream[len=" + length + "])");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#clearParameters()
	 */
	@Override
	public void clearParameters() throws SQLException {
		realStmt.clearParameters();
		clearParameterList();
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
	 */
	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		realStmt.setObject(parameterIndex, x, targetSqlType);
		addParameter(parameterIndex, targetSqlType, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
	 */
	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		realStmt.setObject(parameterIndex, x);
		addParameter(parameterIndex, Types.JAVA_OBJECT, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#execute()
	 */
	@Override
	public boolean execute() throws SQLException {
		startExecute();
		try {
			return reportReturned(realStmt.execute());
		} catch(SQLException e) {
			reportException(e);
			throw e;
		} finally {
			endExecute();
		}
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#addBatch()
	 */
	@Override
	public void addBatch() throws SQLException {
		realStmt.addBatch();
		addBatchList();
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
	 */
	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		realStmt.setCharacterStream(parameterIndex, reader, length);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(CharacterStream[len=" + length + "])");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
	 */
	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
		realStmt.setRef(parameterIndex, x);
		addParameter(parameterIndex, Types.REF, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
	 */
	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		realStmt.setBlob(parameterIndex, x);
		addParameter(parameterIndex, Types.BLOB, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
	 */
	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
		realStmt.setClob(parameterIndex, x);
		addParameter(parameterIndex, Types.CLOB, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
	 */
	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
		realStmt.setArray(parameterIndex, x);
		addParameter(parameterIndex, Types.ARRAY, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#getMetaData()
	 */
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return realStmt.getMetaData();
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)
	 */
	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		realStmt.setDate(parameterIndex, x, cal);
		addParameter(parameterIndex, Types.DATE, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time, java.util.Calendar)
	 */
	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		realStmt.setTime(parameterIndex, x, cal);
		addParameter(parameterIndex, Types.TIME, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
	 */
	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		realStmt.setTimestamp(parameterIndex, x, cal);
		addParameter(parameterIndex, Types.TIMESTAMP, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
	 */
	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
		realStmt.setNull(parameterIndex, sqlType, typeName);
		addParameter(parameterIndex, Types.NULL, null);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
	 */
	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
		realStmt.setURL(parameterIndex, x);
		addParameter(parameterIndex, Types.JAVA_OBJECT, x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#getParameterMetaData()
	 */
	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		return realStmt.getParameterMetaData();
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
	 */
	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		realStmt.setRowId(parameterIndex, x);
		addParameter(parameterIndex, Types.ROWID, x.toString());
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
	 */
	@Override
	public void setNString(int parameterIndex, String value) throws SQLException {
		realStmt.setNString(parameterIndex, value);
		addParameter(parameterIndex, Types.NCHAR, value);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		realStmt.setNCharacterStream(parameterIndex, value, length);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(NCharacterStream[len=" + length + "])");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
	 */
	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		realStmt.setNClob(parameterIndex, value);
		addParameter(parameterIndex, Types.NCLOB, value);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
	 */
	@Override
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		realStmt.setClob(parameterIndex, reader, length);
		addParameter(parameterIndex, Types.CLOB,  "(Clob[len=" + length + "])");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
	 */
	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		realStmt.setBlob(parameterIndex, inputStream, length);
		addParameter(parameterIndex, Types.BLOB,  "(Blob[len=" + length + "])");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
	 */
	@Override
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		realStmt.setNClob(parameterIndex, reader, length);
		addParameter(parameterIndex, Types.NCLOB,  "(NClob[len=" + length + "])");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		realStmt.setSQLXML(parameterIndex, xmlObject);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
	 */
	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
		realStmt.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
		addParameter(parameterIndex, Types.JAVA_OBJECT,  x);
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, long)
	 */
	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		realStmt.setAsciiStream(parameterIndex, x, length);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(AsciiStream[len=" + length + "])");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, long)
	 */
	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
		realStmt.setBinaryStream(parameterIndex, x, length);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(BinaryStream[len=" + length + "])");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		realStmt.setCharacterStream(parameterIndex, reader, length);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(CharacterStream[len=" + length + "])");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
	 */
	@Override
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
		realStmt.setAsciiStream(parameterIndex, x);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(AsciiStream)");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
	 */
	@Override
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
		realStmt.setBinaryStream(parameterIndex, x);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(BinaryStream)");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		realStmt.setCharacterStream(parameterIndex, reader);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(CharacterStream)");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
		realStmt.setNCharacterStream(parameterIndex, value);
		addParameter(parameterIndex, Types.JAVA_OBJECT, "(NCharacterStream)");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
	 */
	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		realStmt.setClob(parameterIndex, reader);
		addParameter(parameterIndex, Types.CLOB, "(Clob)");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
	 */
	@Override
	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		realStmt.setBlob(parameterIndex, inputStream);
		addParameter(parameterIndex, Types.BLOB, "(Blob)");
	}

	/* (非 Javadoc)
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
	 */
	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		realStmt.setNClob(parameterIndex, reader);
		addParameter(parameterIndex, Types.NCLOB, "(NClob)");
	}
}
