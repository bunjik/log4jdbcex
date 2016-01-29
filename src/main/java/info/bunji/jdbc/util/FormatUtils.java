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
package info.bunji.jdbc.util;

import java.lang.reflect.Field;

import blanco.commons.sql.format.BlancoSqlFormatter;
import blanco.commons.sql.format.BlancoSqlRule;

/**
 *
 * @author f.kinoshita
 */
public class FormatUtils {

	private static final BlancoSqlRule rule = new BlancoSqlRule();

	private static final BlancoSqlFormatter formatter;

	static {
		// SQLキーワードは大文字に変換
		rule.setKeywordCase(BlancoSqlRule.KEYWORD_UPPER_CASE);
		try {
			// 可能ならインデントを変更
			Field field = rule.getClass().getField("indentSring");
			field.setAccessible(true);
			field.set(rule, "  ");
		} catch (Exception e) {}

		formatter = new BlancoSqlFormatter(rule);
	}

	/**
	 **********************************************
	 * Format Sql String.
	 * @param sql original sql
	 * @return formatted sql
	 **********************************************
	 */
	public static String formatSql(final String sql) {
		try {
			// ThreadSafeではないため、インスタンスを生成する
			//return new BlancoSqlFormatter(rule).format(sql);
			synchronized (formatter) {
				return formatter.format(sql);
			}
		} catch (Exception e) {
			// not formated.
			return sql;
		}
	}
}
