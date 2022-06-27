package info.bunji.jdbc.util;

import org.junit.Test;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(FormatUtils.class)
public class FormatUtilsTest {

	@Test
	public void testFormatSql() {
		System.out.println(FormatUtils.formatSql("select * from table /*where a='1'*/"));
	}

	// @Test
	// public void testFormatSql2() throws Exception {
	// 	Field field = PowerMockito.field(FormatUtils.class, "formatter");

	// 	Object value = field.get(FormatUtils.class);
	// 	field.set(FormatUtils.class, null);

	// 	FormatUtils.formatSql("select * from table /*where a='1'*/");

	// 	field.set(FormatUtils.class, value);
	// }

	// @Test
	// public void testFormatSql3() throws Exception {
	// 	String sql = "select * from table /*where a='1'*/";
	// 	Field field = PowerMockito.field(FormatUtils.class, "formatter");
	// 	//Formatter value = (Formatter) field.get(FormatUtils.class);
	// 	try {
	// 		//Formatter spyValue = spy(value);
	// 		Mockito.when(spyValue.format(sql)).thenThrow(new RuntimeException());
	// 		field.set(FormatUtils.class, spyValue);

	// 		// exec test
	// 		String ret = FormatUtils.formatSql(sql);

	// 		assertThat(ret, is(sql));
	// 	} finally  {
	// 		field.set(FormatUtils.class, value);
	// 	}
	// }
}
