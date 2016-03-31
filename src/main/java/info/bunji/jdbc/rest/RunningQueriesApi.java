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
package info.bunji.jdbc.rest;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
 * @author f.kinoshita
 */
class RunningQueriesApi extends AbstractApi {

	public RunningQueriesApi(ServletContext context) {
		super(context);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.rest.RestApi#getApiName()
	 */
	@Override
	public String getApiName() {
		return "running";
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
										throws ServletException, IOException {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(res.getOutputStream());
			res.setContentType("application/json; charset=UTF-8");

			String host = hostName + ":" + req.getServerPort();

			// response format
			// {
			//   jdbcurl1 : {
			//     { time: xxx, elapsed: xxx, sql : "select xxxx", ... },
			//     { time: xxx, elapsed: xxx, sql : "update xxxx", ... },
			//     ...
			//	 },
			//   jdbcurl2 : {
			//     { time: xxx, elapsed: xxx, sql : "select xxxx", ... },
			//   ]
			// }
			//
			Map<String, List<QueryInfo>> tmpMap = new TreeMap<String, List<QueryInfo>>();
			for (JdbcLogger log : JdbcLoggerFactory.getLoggers()) {
				List<QueryInfo> qiList = log.getRunningQueries();
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
	protected Map<String, List<Object>> postMergeProcess(Map<String, List<Object>> result) {
		// マージされたデータをソートし直す
		for (Entry<String, List<Object>> entry : result.entrySet()) {
			Object[] qiList = JSON.decode(JSON.encode(entry.getValue()), QueryInfo[].class);
			Arrays.sort(qiList);
			entry.setValue(Arrays.asList(qiList));
		}
		return result;
	}
}
