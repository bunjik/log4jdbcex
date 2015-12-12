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
package info.bunji.jdbc.rest;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;
import info.bunji.jdbc.logger.impl.QueryInfo;
import net.arnx.jsonic.JSON;

/**
 *
 */
public class HistoryApi extends AbstractApi {

	public HistoryApi(ServletContext context) {
		super(context);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
										throws ServletException, IOException {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(res.getOutputStream());
			res.setContentType("application/json; charset=UTF-8");

			String host = hostName + ":" + req.getServerPort();

			Map<String, Collection<QueryInfo>> tmpMap = new TreeMap<String, Collection<QueryInfo>>();
			for (JdbcLogger log : JdbcLoggerFactory.getLoggers()) {
				Collection<QueryInfo> qiList = log.getHistory();
				for (QueryInfo qi : qiList) {
					qi.setHost(host);
				}
				tmpMap.put(log.getConnectUrl(), qiList);
			}

			JSON.encode(tmpMap, bos, false);

			res.setDateHeader("Last-modified", System.currentTimeMillis());
			res.setStatus(HttpServletResponse.SC_OK);
			bos.flush();
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 *
	 */
	@Override
	protected Map<String, Object> postMergeProcess(Map<String, Object> result) {
		// マージされたデータをソートし直す
		for (Entry<String, Object> entry : result.entrySet()) {
			Object[] qiList = JSON.decode(JSON.encode(entry.getValue()), QueryInfo[].class);
			Arrays.sort(qiList);
			entry.setValue(Arrays.asList(qiList));
		}
		return result;
	}
}
