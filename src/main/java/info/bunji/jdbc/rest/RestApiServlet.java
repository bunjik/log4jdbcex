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

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public class RestApiServlet extends HttpServlet {

	/** 呼び出し可能なAPIクラスのマッピング */
	private Map<String, AbstractApi> apiMap = new LinkedHashMap<String, AbstractApi>();

	/*
	 * (非 Javadoc)
	 *
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();

		// データソース初期化のため、一旦コネクションを取得する
		try {
			InitialContext ctx = new InitialContext();
			NamingEnumeration<NameClassPair> ne = ctx.list("java:comp/env/jdbc");
			while (ne.hasMoreElements()) {
				NameClassPair nc = ne.nextElement();
				DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/" + nc.getName());
				Connection conn = ds.getConnection();
				if (conn != null) conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		ServletContext context = getServletContext();
		apiMap.put("ui", new ResourceApi(context));
		apiMap.put("setting", new SettingApi(context));
		apiMap.put("history", new HistoryApi(context));
		apiMap.put("running", new RunningQueriesApi(context));

		// for Test
		apiMap.put("test", new TestQueryApi(context));

		// 明示的にinit() を呼び出す
		for (AbstractApi api : apiMap.values()) {
			api.init();
		}
	}

	/* (非 Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		// 明示的にdestroy() を呼び出す
		for (AbstractApi api : apiMap.values()) {
			api.destroy();
		}

		//===========================================================
		// コンテキストのリロード対策
		// TomcatのバージョンによってはWEB-INF/lib 配下のドライバが
		// DBCPのバグにより、リロード時にメモリリークする
		// そのため、Servletのdestroy時に、コンテキストのクラスローダ
		// でロードされたJDBCドライバのみ明示的にアンロードする
		// ※この機能はREST APIのマッピングがある場合のみ有効
		//===========================================================
		ServletContext ctx = getServletContext();
		try {
			Enumeration<Driver> drivers = DriverManager.getDrivers();
			while(drivers.hasMoreElements()) {
				Driver d = drivers.nextElement();
				if (d.getClass().getClassLoader() == getClass().getClassLoader()) {
					// コンテキストクラスローダでロードされたものみアンロードする
					DriverManager.deregisterDriver(d);
					ctx.log("deregister driver [" + d.getClass().getName() + "] (from webapp classloader)");
				}
			}
		} catch (Exception e) {
			ctx.log("driver deregister error [" + e.getMessage() + "]", e);
		}
		//===========================================================

		super.destroy();
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see
	 * javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		String pathInfo = req.getPathInfo();
		if (pathInfo == null) pathInfo = "/";

		// API名及びパラメータを抽出
		// [0] empty
		// [1] api名
		// [2] api名以降のURI
		String[] params = pathInfo.split("/", 3);
		if (params.length > 1) {
			if (!params[1].isEmpty()) {
				req.setAttribute("API_PATH", params.length > 2 ? params[2] : null);
				AbstractApi api = apiMap.get(params[1]);
				if (api != null) {
					// 対象が見つかった場合、処理を委譲する
					api.service(req, res);
					return;
				}
			} else {
				// API未指定時
				String uri = req.getRequestURI();
				if (!uri.endsWith("/")) uri += "/";
				res.sendRedirect(uri + "ui/");
			}
		}

		// 対象のAPIが見つからない場合
		res.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
}

