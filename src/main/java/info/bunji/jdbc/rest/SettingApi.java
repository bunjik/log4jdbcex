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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;

/**
 *
 */
public class SettingApi extends AbstractApi {

	public SettingApi(ServletContext context) {
		super(context);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
										throws ServletException, IOException {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(res.getOutputStream());
			res.setContentType("application/json; charset=UTF-8");

			// 整形して出力
			JSON.encode(getSettingData(req.getServerPort()), bos, true);

			res.setStatus(HttpServletResponse.SC_OK);
			bos.flush();
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
 	protected void doPut(HttpServletRequest req, HttpServletResponse res)
											throws ServletException, IOException {
		@SuppressWarnings("unchecked")
		Map<String,Map<String,Object>> setting = JSON.decode(req.getInputStream(), Map.class);

		// 接続URL単位の処理
		for(Entry<String,Map<String,Object>> entry : setting.entrySet()) {
			String url = entry.getKey().toString();
			if (JdbcLoggerFactory.hasLogger(url)) {
				JdbcLogger logger = JdbcLoggerFactory.getLogger(url);
				// 設定を反映
				//logger.debug("save :" + JSON.encode(entry.getValue()));
				logger.setSetting((Map<String,Object>)entry.getValue());
			}
		}

		// 更新後の設定情報を返す
		res.setContentType("application/json; charset=UTF-8");
		JSON.encode(getSettingData(req.getServerPort()), res.getOutputStream(), true);
		res.setStatus(HttpServletResponse.SC_OK);
	}

	private Map<String, Object> getSettingData(int port) throws IOException {
		List<JdbcLogger> loggers = JdbcLoggerFactory.getLoggers();

		String host = hostName + ":" + port;

		Map<String,Object> settings = new TreeMap<String,Object>();
		for (JdbcLogger logger : loggers) {
			settings.put(logger.getConnectUrl() , logger.getSetting());
		}
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put(host, settings);

		return jsonMap;
	}
}
