package info.bunji.jdbc.util;

import org.junit.Test;

public class FormatUtilsTest {

	@Test
	public void testFormatSql() {
		String text = FormatUtils.formatSql("select * from table /*where a='1'*/");
	}

}
