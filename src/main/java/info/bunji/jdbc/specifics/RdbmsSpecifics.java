package info.bunji.jdbc.specifics;

public interface RdbmsSpecifics {

	/**
	 * Format an Object that is being bound to a PreparedStatement parameter, for display. The goal is to reformat the
	 * object in a format that can be re-run against the native SQL client of the particular Rdbms being used.  This
	 * class should be extended to provide formatting instances that format objects correctly for different RDBMS
	 * types.
	 *
	 * @param object jdbc object to be formatted.
	 * @return formatted dump of the object.
	 */
	String formatParameterObject(Object object);
}