/*
 * Copyright 2016 Fumiharu Kinoshita
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

import com.github.vertical_blank.sqlformatter.SqlFormatter;

/**
 *
 * @author f.kinoshita
 */
public class FormatUtils {

	/**
	 **********************************************
	 * Format Sql String.
	 * @param sql original sql
	 * @return formatted sql
	 **********************************************
	 */
	public static String formatSql(final String sql) {
		String retSql = sql;
		try {
			retSql = SqlFormatter.format(sql);
		} catch (Exception e) {
			// not formated.
		}
		return retSql;
	}
}
