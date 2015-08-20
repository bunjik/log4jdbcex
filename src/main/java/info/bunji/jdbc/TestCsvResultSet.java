package info.bunji.jdbc;

import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import com.orangesignal.csv.CsvConfig;
import com.orangesignal.csv.CsvReader;
import com.orangesignal.csv.CsvResultSet;

public class TestCsvResultSet implements ResultSet {

	private ResultSet orgRs;

	private CsvResultSet csvRs;

	ResultSetMetaData rsMeta;

	/** 日付と時間系の変換フォーマット */
	private DateFormat[] dateFormats = {
		new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS"),
		new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"),
		new SimpleDateFormat("yyyy-MM-dd hh:mm"),
		new SimpleDateFormat("yyyy-MM-dd"),
		new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS"),
		new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"),
		new SimpleDateFormat("yyyy/MM/dd hh:mm"),
		new SimpleDateFormat("yyyy/MM/dd"),
	};

	public TestCsvResultSet(ResultSet rs, String fileName) throws SQLException {
		orgRs = rs;
		try {
			CsvConfig config = new CsvConfig(',', '"', '\\');
			config.setIgnoreEmptyLines(true);	// 空行は無視する
			CsvReader csvReader = new CsvReader(new FileReader(fileName), config);
			csvRs = new CsvResultSet(csvReader);

			// カラム名と型を取得するため、メタデータを取得
			rsMeta = orgRs.getMetaData();

		} catch(Exception e) {
			throw new SQLException(e);
		}
	}

	@Override
	public boolean next() throws SQLException {
		return csvRs.next();
	}

	@Override
	public int hashCode() {
		return orgRs.hashCode();
	}

	@Override
	public void close() throws SQLException {
		try { csvRs.close(); } catch(Exception e) {}
		try { orgRs.close(); } catch(Exception e) {}
	}

	@Override
	public boolean wasNull() throws SQLException {
		return csvRs.wasNull();
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		String val = csvRs.getString(columnIndex);
		if (val.length() == 0) val = null;
		return val;
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		return csvRs.getBoolean(columnIndex);
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		return csvRs.getByte(columnIndex);
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		return csvRs.getShort(columnIndex);
	}

	@Override
	public boolean equals(Object obj) {
		return csvRs.equals(obj);
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		return csvRs.getInt(columnIndex);
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		return csvRs.getLong(columnIndex);
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		return csvRs.getFloat(columnIndex);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		return csvRs.getDouble(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLFeatureNotSupportedException {
		return csvRs.getBigDecimal(columnIndex, scale);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		return csvRs.getBytes(columnIndex);
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		java.util.Date d;
		String dateStr = csvRs.getString(columnIndex);
		if (dateStr != null) {
			ParsePosition pos = new ParsePosition(0);
			for (DateFormat format : dateFormats) {
				d = format.parse(dateStr, pos);
				if (d != null) return new Date(d.getTime());
			}
		}
		return null;
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		Date d = getDate(columnIndex);
		return d == null ? null : new Time(d.getTime());
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		Date d = getDate(columnIndex);
		return d == null ? null : new Timestamp(getDate(columnIndex).getTime());
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return csvRs.getAsciiStream(columnIndex);
	}

	@Override
	public InputStream getUnicodeStream(int columnIndex)
			throws SQLFeatureNotSupportedException {
		return csvRs.getUnicodeStream(columnIndex);
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return csvRs.getBinaryStream(columnIndex);
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return csvRs.getString(findColumn(columnLabel));
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		return csvRs.getBoolean(findColumn(columnLabel));
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		return csvRs.getByte(findColumn(columnLabel));
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		return csvRs.getShort(findColumn(columnLabel));
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		return csvRs.getInt(findColumn(columnLabel));
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		return csvRs.getLong(findColumn(columnLabel));
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return csvRs.getFloat(findColumn(columnLabel));
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return csvRs.getDouble(findColumn(columnLabel));
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale)
			throws SQLException {
		return csvRs.getBigDecimal(findColumn(columnLabel), scale);
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		return csvRs.getBytes(findColumn(columnLabel));
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return getDate(findColumn(columnLabel));
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return getTime(findColumn(columnLabel));
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return getTimestamp(findColumn(columnLabel));
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return csvRs.getAsciiStream(findColumn(columnLabel));
	}

	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return csvRs.getUnicodeStream(findColumn(columnLabel));
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return csvRs.getBinaryStream(findColumn(columnLabel));
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return csvRs.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		csvRs.clearWarnings();
	}

	@Override
	public String getCursorName() throws SQLFeatureNotSupportedException {
		return csvRs.getCursorName();
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return orgRs.getMetaData();
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		// https://docs.oracle.com/javase/jp/1.4/guide/jdbc/getstart/mapping.html#table1
		int type = rsMeta.getColumnType(columnIndex);
		Object ret = null;
		switch (type) {
		case Types.TINYINT:
			ret = getByte(columnIndex);
			break;
		case Types.SMALLINT:
			ret = getShort(columnIndex);
			break;
		case Types.INTEGER:
			ret = getInt(columnIndex);
			break;
		case Types.BIGINT:
			ret = getLong(columnIndex);
			break;
		case Types.REAL:
			ret = getFloat(columnIndex);
			break;
		case Types.FLOAT:
		case Types.DOUBLE:
			ret = getDouble(columnIndex);
			break;
		case Types.DECIMAL:
		case Types.NUMERIC:
			ret = getBigDecimal(columnIndex);
			break;
		case Types.BIT:
			ret = getBoolean(columnIndex);
			break;
		case Types.CHAR:
		case Types.VARCHAR:
			ret = getString(columnIndex);
			break;
		case Types.LONGVARCHAR:
			ret = getAsciiStream(columnIndex);
			break;
		case Types.BINARY:
		case Types.VARBINARY:
			ret = getBytes(columnIndex);
			break;
		case Types.LONGVARBINARY:
			ret = getBinaryStream(columnIndex);
			break;
		case Types.DATE:
			ret = getDate(columnIndex);
			break;
		case Types.TIME:
			ret = getTime(columnIndex);
			break;
		case Types.TIMESTAMP:
			ret = getTimestamp(columnIndex);
			break;
		case Types.CLOB:
			ret = getClob(columnIndex);
			break;
		case Types.BLOB:
			ret = getBlob(columnIndex);
			break;
		case Types.ARRAY:
			ret = getArray(columnIndex);
			break;
		case Types.REF:
			ret = getRef(columnIndex);
			break;
		case Types.STRUCT:
		case Types.JAVA_OBJECT:
			ret = csvRs.getObject(columnIndex);
			break;
		default:
			ret = csvRs.getObject(columnIndex);
			break;
		}
		return ret;

	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return getObject(findColumn(columnLabel));
	}

	@Override
	public String toString() {
		return csvRs.toString();
	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		return orgRs.findColumn(columnLabel);
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return csvRs.getCharacterStream(columnIndex);
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return csvRs.getCharacterStream(findColumn(columnLabel));
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return csvRs.getBigDecimal(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return csvRs.getBigDecimal(findColumn(columnLabel));
	}

	@Override
	public boolean isBeforeFirst() throws SQLFeatureNotSupportedException {
		return csvRs.isBeforeFirst();
	}

	@Override
	public boolean isAfterLast() throws SQLFeatureNotSupportedException {
		return csvRs.isAfterLast();
	}

	@Override
	public boolean isFirst() throws SQLFeatureNotSupportedException {
		return csvRs.isFirst();
	}

	@Override
	public boolean isLast() throws SQLFeatureNotSupportedException {
		return csvRs.isLast();
	}

	@Override
	public void beforeFirst() throws SQLFeatureNotSupportedException {
		csvRs.beforeFirst();
	}

	@Override
	public void afterLast() throws SQLFeatureNotSupportedException {
		csvRs.afterLast();
	}

	@Override
	public boolean first() throws SQLFeatureNotSupportedException {
		return csvRs.first();
	}

	@Override
	public boolean last() throws SQLFeatureNotSupportedException {
		return csvRs.last();
	}

	@Override
	public int getRow() throws SQLException {
		return csvRs.getRow();
	}

	@Override
	public boolean absolute(int row) throws SQLFeatureNotSupportedException {
		return csvRs.absolute(row);
	}

	@Override
	public boolean relative(int rows) throws SQLFeatureNotSupportedException {
		return csvRs.relative(rows);
	}

	@Override
	public boolean previous() throws SQLFeatureNotSupportedException {
		return csvRs.previous();
	}

	@Override
	public void setFetchDirection(int direction)
			throws SQLFeatureNotSupportedException {
		csvRs.setFetchDirection(direction);
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return csvRs.getFetchDirection();
	}

	@Override
	public void setFetchSize(int rows) throws SQLFeatureNotSupportedException {
		csvRs.setFetchSize(rows);
	}

	@Override
	public int getFetchSize() throws SQLException {
		return csvRs.getFetchSize();
	}

	@Override
	public int getType() throws SQLException {
		return orgRs.getType();
	}

	@Override
	public int getConcurrency() throws SQLException {
		return orgRs.getConcurrency();
	}

	@Override
	public boolean rowUpdated() throws SQLFeatureNotSupportedException {
		return csvRs.rowUpdated();
	}

	@Override
	public boolean rowInserted() throws SQLFeatureNotSupportedException {
		return csvRs.rowInserted();
	}

	@Override
	public boolean rowDeleted() throws SQLFeatureNotSupportedException {
		return csvRs.rowDeleted();
	}

	@Override
	public void updateNull(int columnIndex)
			throws SQLFeatureNotSupportedException {
		csvRs.updateNull(columnIndex);
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateBoolean(columnIndex, x);
	}

	@Override
	public void updateByte(int columnIndex, byte x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateByte(columnIndex, x);
	}

	@Override
	public void updateShort(int columnIndex, short x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateShort(columnIndex, x);
	}

	@Override
	public void updateInt(int columnIndex, int x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateInt(columnIndex, x);
	}

	@Override
	public void updateLong(int columnIndex, long x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateLong(columnIndex, x);
	}

	@Override
	public void updateFloat(int columnIndex, float x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateFloat(columnIndex, x);
	}

	@Override
	public void updateDouble(int columnIndex, double x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateDouble(columnIndex, x);
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateBigDecimal(columnIndex, x);
	}

	@Override
	public void updateString(int columnIndex, String x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateString(columnIndex, x);
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateBytes(columnIndex, x);
	}

	@Override
	public void updateDate(int columnIndex, Date x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateDate(columnIndex, x);
	}

	@Override
	public void updateTime(int columnIndex, Time x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateTime(columnIndex, x);
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateTimestamp(columnIndex, x);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLFeatureNotSupportedException {
		csvRs.updateAsciiStream(columnIndex, x, length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLFeatureNotSupportedException {
		csvRs.updateBinaryStream(columnIndex, x, length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLFeatureNotSupportedException {
		csvRs.updateCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength)
			throws SQLFeatureNotSupportedException {
		csvRs.updateObject(columnIndex, x, scaleOrLength);
	}

	@Override
	public void updateObject(int columnIndex, Object x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateObject(columnIndex, x);
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		csvRs.updateNull(findColumn(columnLabel));
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x)
			throws SQLException {
		csvRs.updateBoolean(columnLabel, x);
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		csvRs.updateByte(columnLabel, x);
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		csvRs.updateShort(columnLabel, x);
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		csvRs.updateInt(columnLabel, x);
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		csvRs.updateLong(columnLabel, x);
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		csvRs.updateFloat(columnLabel, x);
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		csvRs.updateDouble(columnLabel, x);
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x)
			throws SQLException {
		csvRs.updateBigDecimal(columnLabel, x);
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		csvRs.updateString(columnLabel, x);
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		csvRs.updateBytes(columnLabel, x);
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		csvRs.updateDate(columnLabel, x);
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		csvRs.updateTime(columnLabel, x);
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x)
			throws SQLException {
		csvRs.updateTimestamp(columnLabel, x);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length)
			throws SQLException {
		csvRs.updateAsciiStream(columnLabel, x, length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length)
			throws SQLException {
		csvRs.updateBinaryStream(columnLabel, x, length);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			int length) throws SQLException {
		csvRs.updateCharacterStream(columnLabel, reader, length);
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength)
			throws SQLException {
		csvRs.updateObject(columnLabel, x, scaleOrLength);
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		csvRs.updateObject(columnLabel, x);
	}

	@Override
	public void insertRow() throws SQLFeatureNotSupportedException {
		csvRs.insertRow();
	}

	@Override
	public void updateRow() throws SQLFeatureNotSupportedException {
		csvRs.updateRow();
	}

	@Override
	public void deleteRow() throws SQLFeatureNotSupportedException {
		csvRs.deleteRow();
	}

	@Override
	public void refreshRow() throws SQLFeatureNotSupportedException {
		csvRs.refreshRow();
	}

	@Override
	public void cancelRowUpdates() throws SQLFeatureNotSupportedException {
		csvRs.cancelRowUpdates();
	}

	@Override
	public void moveToInsertRow() throws SQLFeatureNotSupportedException {
		csvRs.moveToInsertRow();
	}

	@Override
	public void moveToCurrentRow() throws SQLFeatureNotSupportedException {
		csvRs.moveToCurrentRow();
	}

	@Override
	public Statement getStatement() throws SQLException {
		return csvRs.getStatement();
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map)
			throws SQLFeatureNotSupportedException {
		return csvRs.getObject(columnIndex, map);
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLFeatureNotSupportedException {
		return csvRs.getRef(columnIndex);
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		return csvRs.getBlob(columnIndex);
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		return csvRs.getClob(columnIndex);
	}

	@Override
	public Array getArray(int columnIndex)
			throws SQLFeatureNotSupportedException {
		return csvRs.getArray(columnIndex);
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map)
			throws SQLException {
		return csvRs.getObject(columnLabel, map);
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		return csvRs.getRef(findColumn(columnLabel));
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		return csvRs.getBlob(findColumn(columnLabel));
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		return csvRs.getClob(findColumn(columnLabel));
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		return csvRs.getArray(findColumn(columnLabel));
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return csvRs.getDate(columnIndex, cal);
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return csvRs.getDate(columnLabel, cal);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return csvRs.getTime(columnIndex, cal);
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return csvRs.getTime(columnLabel, cal);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		return csvRs.getTimestamp(columnIndex, cal);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal)
			throws SQLException {
		return csvRs.getTimestamp(columnLabel, cal);
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		return csvRs.getURL(columnIndex);
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		return csvRs.getURL(findColumn(columnLabel));
	}

	@Override
	public void updateRef(int columnIndex, Ref x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateRef(columnIndex, x);
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		csvRs.updateRef(columnLabel, x);
	}

	@Override
	public void updateBlob(int columnIndex, Blob x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateBlob(columnIndex, x);
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		csvRs.updateBlob(columnLabel, x);
	}

	@Override
	public void updateClob(int columnIndex, Clob x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateClob(columnIndex, x);
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		csvRs.updateClob(columnLabel, x);
	}

	@Override
	public void updateArray(int columnIndex, Array x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateArray(columnIndex, x);
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		csvRs.updateArray(columnLabel, x);
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return csvRs.getRowId(columnIndex);
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return csvRs.getRowId(findColumn(columnLabel));
	}

	@Override
	public void updateRowId(int columnIndex, RowId x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateRowId(columnIndex, x);
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		csvRs.updateRowId(columnLabel, x);
	}

	@Override
	public int getHoldability() throws SQLException {
		return csvRs.getHoldability();
	}

	@Override
	public boolean isClosed() {
		return csvRs.isClosed();
	}

	@Override
	public void updateNString(int columnIndex, String string)
			throws SQLFeatureNotSupportedException {
		csvRs.updateNString(columnIndex, string);
	}

	@Override
	public void updateNString(String columnLabel, String string)
			throws SQLException {
		csvRs.updateNString(columnLabel, string);
	}

	@Override
	public void updateNClob(int columnIndex, NClob clob)
			throws SQLFeatureNotSupportedException {
		csvRs.updateNClob(columnIndex, clob);
	}

	@Override
	public void updateNClob(String columnLabel, NClob clob) throws SQLException {
		csvRs.updateNClob(columnLabel, clob);
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		return csvRs.getNClob(columnIndex);
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		return csvRs.getNClob(findColumn(columnLabel));
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return csvRs.getSQLXML(columnIndex);
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return csvRs.getSQLXML(findColumn(columnLabel));
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLFeatureNotSupportedException {
		csvRs.updateSQLXML(columnIndex, xmlObject);
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException {
		csvRs.updateSQLXML(columnLabel, xmlObject);
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		return csvRs.getNString(columnIndex);
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		return csvRs.getNString(findColumn(columnLabel));
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return csvRs.getNCharacterStream(columnIndex);
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return csvRs.getNCharacterStream(findColumn(columnLabel));
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLFeatureNotSupportedException {
		csvRs.updateNCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		csvRs.updateNCharacterStream(findColumn(columnLabel), reader, length);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLFeatureNotSupportedException {
		csvRs.updateAsciiStream(columnIndex, x, length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLFeatureNotSupportedException {
		csvRs.updateBinaryStream(columnIndex, x, length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLFeatureNotSupportedException {
		csvRs.updateCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException {
		csvRs.updateAsciiStream(findColumn(columnLabel), x, length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException {
		csvRs.updateBinaryStream(findColumn(columnLabel), x, length);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		csvRs.updateCharacterStream(findColumn(columnLabel), reader, length);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLFeatureNotSupportedException {
		csvRs.updateBlob(columnIndex, inputStream, length);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException {
		csvRs.updateBlob(findColumn(columnLabel), inputStream, length);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLFeatureNotSupportedException {
		csvRs.updateClob(columnIndex, reader, length);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		csvRs.updateClob(findColumn(columnLabel), reader, length);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLFeatureNotSupportedException {
		csvRs.updateNClob(columnIndex, reader, length);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		csvRs.updateNClob(findColumn(columnLabel), reader, length);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateNCharacterStream(columnIndex, x);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		csvRs.updateNCharacterStream(findColumn(columnLabel), reader);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateAsciiStream(columnIndex, x);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateBinaryStream(columnIndex, x);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLFeatureNotSupportedException {
		csvRs.updateCharacterStream(columnIndex, x);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException {
		csvRs.updateAsciiStream(findColumn(columnLabel), x);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException {
		csvRs.updateBinaryStream(findColumn(columnLabel), x);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		csvRs.updateCharacterStream(findColumn(columnLabel), reader);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLFeatureNotSupportedException {
		csvRs.updateBlob(columnIndex, inputStream);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException {
		csvRs.updateBlob(findColumn(columnLabel), inputStream);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader)
			throws SQLFeatureNotSupportedException {
		csvRs.updateClob(columnIndex, reader);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader)
			throws SQLException {
		csvRs.updateClob(findColumn(columnLabel), reader);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader)
			throws SQLFeatureNotSupportedException {
		csvRs.updateNClob(columnIndex, reader);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException {
		csvRs.updateNClob(findColumn(columnLabel), reader);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLFeatureNotSupportedException {
		return csvRs.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface)
			throws SQLFeatureNotSupportedException {
		return csvRs.isWrapperFor(iface);
	}
}
