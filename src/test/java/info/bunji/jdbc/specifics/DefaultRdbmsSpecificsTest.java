/**
 *
 */
package info.bunji.jdbc.specifics;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.Test;

/**
 * @author f.kinoshita
 *
 */
public class DefaultRdbmsSpecificsTest {

	/**
	 * {@link info.bunji.jdbc.specifics.DefaultRdbmsSpecifics#formatParameterObject(java.lang.Object)} のためのテスト・メソッド。
	 */
	@Test
	public void testFormatParameterObject() {
		RdbmsSpecifics spec = new DefaultRdbmsSpecifics();
		assertThat(spec.formatParameterObject("testString"), is("'testString'"));
		assertThat(spec.formatParameterObject(null), is("NULL"));

		Date date = new Date(System.currentTimeMillis());
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
		assertThat(spec.formatParameterObject(date), is("'" + format.format(date) + "'"));
	}

	/**
	 * {@link info.bunji.jdbc.specifics.DefaultRdbmsSpecifics#escapeString(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testEscapeString() {
		RdbmsSpecifics spec = new DefaultRdbmsSpecifics();
		assertThat(spec.formatParameterObject("testString"), is("'testString'"));
		assertThat(spec.formatParameterObject(null), is("NULL"));
	}
}
