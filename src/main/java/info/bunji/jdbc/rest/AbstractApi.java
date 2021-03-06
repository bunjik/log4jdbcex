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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;
import net.arnx.jsonic.JSON;

/**
 *
 * @author f.kinoshita
 */
abstract class AbstractApi extends HttpServlet implements RestApi {

	protected JdbcLogger logger = JdbcLoggerFactory.getLogger();

	protected final String hostName;

	protected ServletContext context;

	private final ExecutorService executor;

	public AbstractApi(ServletContext context) {
		this.context = context;

		InetAddress ia = null;
		try {
			ia = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		hostName = (ia != null ? ia.getHostName() : "unknownHost");

		executor = Executors.newCachedThreadPool();
	}

	/**
	 * get API path
	 * @param req the HttpServletRequest object that contains the request the client made of the servlet
	 * @return API path
	 */
	protected String getApiPath(HttpServletRequest req) {
		return (String)req.getAttribute("API_PATH");
	}

	/**
	 * Processing to be executed after the merge.
	 *
	 * The default does nothing
	 *
	 * @param result merged result
	 * @return modified merged result
	 */
	protected Map<String, List<Object>> postMergeProcess(Map<String, List<Object>> result) {
		return result;
	}

	/*
	 * (非 Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
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

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.rest.RestApi#destory()
	 */
	@Override
	public void destroy() {
		try {
			executor.shutdownNow();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// do nothing.
		}
		super.destroy();
	}

	/**
	 *
	 * @param req
	 * @return
	 */
	private Map<String, List<Object>> broadcastRequest(HttpServletRequest req) {
		Map<String, List<Object>> resultMap = new TreeMap<String, List<Object>>();
		String serversParam = req.getParameter("servers");
		String[] servers = serversParam.split(",");

		String scheme = req.getScheme() + "://";
		String uri = req.getRequestURI();
		String method = req.getMethod();

		// リクエストURL一覧を生成(サーバの重複を除外)
		Set<String> sendList = new TreeSet<String>();
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
		String cookieStr = null;
		if (cookies != null && cookies.length > 0) {
			StringBuilder cookieBuf = new StringBuilder();
			for (Cookie cookie : cookies) {
				cookieBuf.append(cookie.getName()).append("=").append(cookie.getValue());
				cookieBuf.append("; ");
				if (cookie.getPath() != null) cookieBuf.append("PATH=").append(cookie.getPath());
			}
			cookieStr = cookieBuf.toString();
			logger.trace("Cookie:" + cookieStr);
		}

		List<Future<Map<String, List<Object>>>> futures = new ArrayList<Future<Map<String, List<Object>>>>();
		for (String s : sendList) {
			try {
				logger.trace("(" + method + ") " + s);
				futures.add(executor.submit(new InternalRequest(method, s, baos.toByteArray(), cookieStr)));
			} catch (Exception e) {
				logger.warn(e.toString(), e);
			}
		}

		for (Future<Map<String, List<Object>>> f : futures) {
			try {
				Map<String, List<Object>> o = f.get();

				// mapのマージ
				for (Entry<String, List<Object>> entry : o.entrySet()) {
					String key = entry.getKey();
					if (!resultMap.containsKey(key)) {
						resultMap.put(key, o.get(key));
					} else {
						resultMap.get(key).addAll(o.get(key));
					}
				}
			} catch (Exception e) {
				logger.warn(e.toString(), e);
			}
		}
		return resultMap;
	}

	/**
	 * スレッドでリクエストを発行するためのクラス
	 */
	class InternalRequest implements Callable<Map<String, List<Object>>> {
		private String url;
		private String method;
		private byte[] body;
		private String cookies;

		public InternalRequest(String method, String url, byte[] body, String cookies) {
			this.url = url;
			this.method = method;
			this.body = body;
			this.cookies = cookies;
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
		@Override
		public Map<String, List<Object>> call() throws Exception {
			HttpURLConnection conn = null;
			OutputStream os = null;
			InputStream is = null;
			try {
				URL requestUrl = new URL(url);

				// 接続情報を設定する
				conn = (HttpURLConnection) requestUrl.openConnection();
				conn.setRequestMethod(method);
				// cookieが指定されていたら、送信ヘッダに追加する
				if (cookies != null && cookies.length() > 0) {
					conn.setRequestProperty("Cookie", cookies);
				}
				// 転送先で再送が起こらないようヘッダを付与する?
				//conn.setRequestProperty("X-Transfered", "true");
				// タイムアウトの設定
				conn.setConnectTimeout(3000);
				conn.setReadTimeout(3000);

				// 接続
				if (body != null && body.length > 0) {
					conn.setDoOutput(true);
					os = conn.getOutputStream();
					os.write(body);
					os.flush();
				} else {
					conn.connect();
				}
				is = conn.getInputStream();
				return JSON.decode(is);
			} catch (Exception e) {
				logger.debug(e.toString(), e);
				return Collections.emptyMap();
			} finally {
				closeQuietly(os);
				closeQuietly(is);
				if (conn != null) conn.disconnect();
			}
		}
	}

	/**
	 *
	 * @param closable closable object
	 */
	void closeQuietly(Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (Exception e) {}
		}
	}
}
