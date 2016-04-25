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
package info.bunji.jdbc.specifics;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author f.kinosshita
 */
public class OracleRdbmsSpecifics extends DefaultRdbmsSpecifics {

	private final SimpleDateFormat tsFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");

	private final SimpleDateFormat dtFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	@Override
	public String formatParameterObject(Object object) {
		String formatStr;
		if (object instanceof Timestamp) {
			synchronized (tsFormat) {
				formatStr = String.format("to_timestamp('%s','mm/dd/yyyy hh24:mi:ss.ff3')", tsFormat.format(object));
			}
		} else if (object instanceof Date) {
			synchronized (dtFormat) {
				formatStr = String.format("to_date('%s','mm/dd/yyyy hh24:mi:ss')", dtFormat.format(object));
			}
		} else {
			formatStr = super.formatParameterObject(object);
		}
		return formatStr;
	}
}
