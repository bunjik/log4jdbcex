package info.bunji.jdbc.specifics;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RdbmsSpecifics {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");

	public RdbmsSpecifics() {
		// do nothing.
	}

	/**
	 * Format an Object that is being bound to a PreparedStatement parameter, for display. The goal is to reformat the
	 * object in a format that can be re-run against the native SQL client of the particular Rdbms being used.  This
	 * class should be extended to provide formatting instances that format objects correctly for different RDBMS
	 * types.
	 *
	 * @param object jdbc object to be formatted.
	 * @return formatted dump of the object.
	 */
	public String formatParameterObject(Object object) {
		if (object == null) {
			return "NULL";
		} else {
			if (object instanceof String) {
				return "'" + escapeString((String) object) + "'";
			} else if (object instanceof Date) {
				synchronized (dateFormat) {
					return "'" + dateFormat.format(object) + "'";
				}
			} else {
				return object.toString();
			}
		}
	}

	/**
	 * Make sure string is escaped properly so that it will run in a SQL query analyzer tool.
	 * At this time all we do is double any single tick marks.
	 * Do not call this with a null string or else an exception will occur.
	 *
	 * @return the input String, escaped.
	 */
	String escapeString(String in) {
		StringBuilder out = new StringBuilder();
		for (int i = 0, j = in.length(); i < j; i++) {
			char c = in.charAt(i);
			if (c == '\'') {
				out.append(c);
			}
			out.append(c);
		}
		return out.toString();
	}
}
