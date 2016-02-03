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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;
import net.arnx.jsonic.JSON;

/**
 *
 * @author f.kinoshita
 */
class SettingApi extends AbstractApi {

	public SettingApi(ServletContext context) {
		super(context);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.rest.RestApi#getApiName()
	 */
	@Override
	public String getApiName() {
		return "setting";
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
										throws ServletException, IOException {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(res.getOutputStream());
			res.setContentType("application/json; charset=UTF-8");

			Map<String, Map<String, Object>> settings = getSettingData(req.getServerPort());
			//long lastUpdate = 0L;
			//for(Entry<String, Map<String, Object>> entry : settings.entrySet()) {
			//	lastUpdate = Math.max((Long)entry.getValue().remove("lastUpdate"), lastUpdate);
			//}
			//res.setDateHeader("Last-modified", lastUpdate);

			// format strings
			JSON.encode(settings, bos, true);

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
			JdbcLogger logger = JdbcLoggerFactory.getLogger(url);
			if (logger != null) {
				// 設定を反映
				logger.setSetting((Map<String,Object>)entry.getValue());
			}
//			if (JdbcLoggerFactory.hasLogger(url)) {
//				JdbcLogger logger = JdbcLoggerFactory.getLogger(url);
//				// 設定を反映
//				//logger.debug("save :" + JSON.encode(entry.getValue()));
//				logger.setSetting((Map<String,Object>)entry.getValue());
//			}
		}

		// 更新後の設定情報を返す
		res.setContentType("application/json; charset=UTF-8");
		JSON.encode(getSettingData(req.getServerPort()), res.getOutputStream(), true);
		res.setStatus(HttpServletResponse.SC_OK);
	}

	private Map<String, Map<String,Object>> getSettingData(int port) throws IOException {
		Map<String, Map<String, Object>> jsonMap = new HashMap<String, Map<String, Object>>();
		for (JdbcLogger logger : JdbcLoggerFactory.getLoggers()) {
			jsonMap.put(logger.getConnectUrl() , logger.getSetting());
		}
		return jsonMap;
	}

	/**
	 *
	 */
/*
	@Override
	protected Map<String, Collection<Object>> postMergeProcess(Map<String, Collection<Object>> result) {
		// 更新日時が最新のデータを返却します
		for (Entry<String, Collection<Object>> entry : result.entrySet()) {

			Object[] tmp = entry.getValue().toArray();
			Arrays.sort(tmp, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					return (long)((Map)o1).get("lastUpdate"). - (long)((Map)o2).get("lastUpdate");
				}
			});


//			Object[] qiList = JSON.decode(JSON.encode(entry.getValue()), QueryInfo[].class);
//			Arrays.sort(qiList);
//			entry.setValue(Arrays.asList(qiList));
		}
		return result;
	}
*/
}
