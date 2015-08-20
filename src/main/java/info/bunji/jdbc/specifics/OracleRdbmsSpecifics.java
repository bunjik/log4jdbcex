package info.bunji.jdbc.specifics;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OracleRdbmsSpecifics extends RdbmsSpecifics {

	private final SimpleDateFormat tsFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");

	private final SimpleDateFormat dtFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	public OracleRdbmsSpecifics() {
		super();
	}

	@Override
	public String formatParameterObject(Object object) {
		if (object instanceof Timestamp) {
			synchronized (tsFormat) {
				return String.format("to_timestamp('%s','mm/dd/yyyy hh24:mi:ss.ff3')", tsFormat.format(object));
			}
		} else if (object instanceof Date) {
			synchronized (dtFormat) {
				return String.format("to_date('%s','mm/dd/yyyy hh24:mi:ss')", dtFormat.format(object));
			}
		} else {
			return super.formatParameterObject(object);
		}
	}
}
