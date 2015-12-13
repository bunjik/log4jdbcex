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

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;
import info.bunji.jdbc.logger.impl.QueryInfo;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;

/**
 *
 */
public class RunningQueriesApi extends AbstractApi {

	public RunningQueriesApi(ServletContext context) {
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

			Map<String, List<QueryInfo>> tmpMap = new TreeMap<String, List<QueryInfo>>();
			for (JdbcLogger log : JdbcLoggerFactory.getLoggers()) {
				tmpMap.put(log.getConnectUrl(), log.getRunningQueries());
			}
			Map<String, Object> jsonMap = new TreeMap<String, Object>();
			jsonMap.put(host, tmpMap);
			JSON.encode(jsonMap, bos, true);	// 整形して出力

			res.setStatus(HttpServletResponse.SC_OK);
			bos.flush();
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
