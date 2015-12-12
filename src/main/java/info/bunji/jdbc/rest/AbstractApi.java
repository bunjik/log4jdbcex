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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;
import net.arnx.jsonic.JSON;

public abstract class AbstractApi extends HttpServlet {

	protected JdbcLogger logger = JdbcLoggerFactory.getLogger();

	protected final String hostName;

	protected ServletContext context;

	public AbstractApi(ServletContext context) {
		this.context = context;

		InetAddress ia = null;
		try {
			ia = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		hostName = ia != null ? ia.getHostName() : "unknownHost";
	}

	/* (非 Javadoc)
	 * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/* (非 Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/* (非 Javadoc)
	 * @see javax.servlet.http.HttpServlet#doHead(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/* (非 Javadoc)
	 * @see javax.servlet.http.HttpServlet#doOptions(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/* (非 Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/* (非 Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/* (非 Javadoc)
	 * @see javax.servlet.http.HttpServlet#doTrace(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * API名を取得する
	 * @param req
	 * @return
	 */
	protected String getApiName(HttpServletRequest req) {
		return (String)req.getAttribute("API");
	}

	protected String getApiPath(HttpServletRequest req) {
		return (String)req.getAttribute("API_PATH");
	}

	/**
	 * 複数サーバのデータマージ後に呼び出される
	 *
	 * マージ後のデータに加工が必要な場合に実装します。
	 * デフォルトは何も行いません。
	 *
	 * @param result
	 * @return
	 */
	protected Map<String, Object> postMergeProcess(Map<String, Object> result) {
		return result;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
										throws ServletException, IOException {

		// 有効なserversパラメータが指定されている場合は指定サーバすべてに
		// リクエストを発行する(自分自身も含む)
		String servers = req.getParameter("servers");
		if (servers != null && !servers.trim().isEmpty()) {
			OutputStream os = res.getOutputStream();
			res.setContentType("application/json; charset=UTF-8");
			res.setDateHeader("Last-modified", System.currentTimeMillis());
			JSON.encode(postMergeProcess(broadcastRequest(req)), os);
			os.flush();
		} else {
			super.service(req, res);
		}
	}

	/**
	 *
	 * @param req
	 * @return
	 */
	private Map<String, Object> broadcastRequest(HttpServletRequest req) {
		Map<String, Object> resultMap = new TreeMap<String, Object>();
		String serversParam = req.getParameter("servers");
		String[] servers = serversParam.split(",");

		String scheme = req.getScheme() + "://";
		String uri = req.getRequestURI();
		String method = req.getMethod();

		// リクエストURL一覧を生成(サーバの重複を除外)
//		Set<String> sendList = new TreeSet<String>();
List<String> sendList = new ArrayList<String>();
		for (String server : servers) {
			String requestUrl = scheme + server.trim() + uri;
			sendList.add(requestUrl);
		}

		// body部にデータがあれば各サーバに転送
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(req.getInputStream());
			byte[] buf = new byte[4096];
			int len;
			while ((len = bis.read(buf)) > 0) {
				baos.write(buf, 0, len);
			}
		} catch (IOException ioe) {
			logger.warn(ioe.getMessage(), ioe);
		} finally {
			closeQuietly(bis);
		}

		// Cookieの取得
		Cookie[] cookies = req.getCookies();

		for (String s : sendList) {
			try {
				logger.trace("(" + method + ") " + s);
				Map<String, Object> o = sendRequest(method, s, baos, cookies);

				// mapのマージ
				for (String key : o.keySet()) {
					if (!resultMap.containsKey(key)) {
						resultMap.put(key, o.get(key));
					} else {
						if (o instanceof Collection) {
							((Collection)resultMap.get(key)).addAll((Collection)o.get(key));
						} else {
							((Map)resultMap.get(key)).putAll((Map)o.get(key));
						}
					}
				}
			} catch (Exception e) {
				logger.warn(e.toString(), e);
			}
		}
		return resultMap;
	}

	/**
	 * 指定されたURLにリクエストを送信する
	 *
	 * ※REST APIを想定しているため、元のリクエストに付与された
	 * パラメータは送信されません
	 *
	 * @param method
	 * @param requestUrl
	 * @return
	 */
	private Map<String, Object> sendRequest(String method, String requestUrl,
						ByteArrayOutputStream baos, Cookie[] cookies) throws Exception {

		HttpURLConnection conn = null;
		OutputStream os = null;
		InputStream is = null;
		try {
			URL url = new URL(requestUrl);

			// 接続情報を設定する
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);

			// cookieが指定されていたら、送信ヘッダに追加する
			if (cookies != null && cookies.length > 0) {
				StringBuilder cookieBuf = new StringBuilder();
				for (Cookie cookie : cookies) {
					cookieBuf.append(cookie.getName()).append("=").append(cookie.getValue());
					cookieBuf.append("; ");
					if (cookie.getPath() != null) cookieBuf.append("PATH=").append(cookie.getPath());
				}
				logger.trace("Cookie:" + cookieBuf.toString());

				// 元のリクエストに付与されているcookie情報をヘッダに設定する
				conn.setRequestProperty("Cookie", cookieBuf.toString());
			}

			// 転送先で再送が起こらないようヘッダを付与する?
			//conn.setRequestProperty("X-Transfered", "true");
			conn.setConnectTimeout(5000);

			// 接続
			if (baos.size() > 0) {
				conn.setDoOutput(true);
				os = conn.getOutputStream();
				baos.writeTo(os);
				os.flush();
			} else {
				conn.connect();
			}
			is = conn.getInputStream();
			return JSON.decode(is);
		} finally {
			closeQuietly(os);
			closeQuietly(is);
			conn.disconnect();
		}
	}

	/**
	 *
	 * @param closeable
	 */
	protected void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {}
		}
	}
}
