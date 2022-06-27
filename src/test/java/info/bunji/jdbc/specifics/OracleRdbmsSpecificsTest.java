/**
 *
 */
package info.bunji.jdbc.specifics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.Test;

/**
 * @author f.kinoshita
 *
 */
public class OracleRdbmsSpecificsTest {

	/**
	 * {@link info.bunji.jdbc.specifics.OracleRdbmsSpecifics#formatParameterObject(java.lang.Object)} のためのテスト・メソッド。
	 */
	@Test
	public void testFormatParameterObject() {
		RdbmsSpecifics spec = new OracleRdbmsSpecifics();
		assertThat(spec.formatParameterObject("testString"), is("'testString'"));
		assertThat(spec.formatParameterObject(null), is("NULL"));

		Date date = new Date(System.currentTimeMillis());
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		assertThat(spec.formatParameterObject(date),
					is("to_date('" + format.format(date) + "','mm/dd/yyyy hh24:mi:ss')"));

		Timestamp ts = new Timestamp(System.currentTimeMillis());
		DateFormat tsFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
		assertThat(spec.formatParameterObject(ts),
					is("to_timestamp('" + tsFormat.format(ts) + "','mm/dd/yyyy hh24:mi:ss.ff3')"));
	}

}
