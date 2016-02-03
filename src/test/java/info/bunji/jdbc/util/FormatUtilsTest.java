package info.bunji.jdbc.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormatUtilsTest {

	@Test
	public void testFormatSql() {
		String text = FormatUtils.formatSql("select * from table /*where a='1'*/");
		System.out.println(text);

		fail("まだ実装されていません");
	}

}
