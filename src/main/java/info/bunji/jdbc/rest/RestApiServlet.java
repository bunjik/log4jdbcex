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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;
import info.bunji.jdbc.util.ClassScanUtil;

/**
 *
 * @author f.kinoshita
 */
@WebServlet(name="RestApiServlet",urlPatterns={"/log4jdbcex"})
public class RestApiServlet extends HttpServlet {

	protected JdbcLogger logger = JdbcLoggerFactory.getLogger();

	/** 呼び出し可能なAPIクラスのマッピング */
	private Map<String, RestApi> apiMap = new LinkedHashMap<String, RestApi>();

	/*
	 * (非 Javadoc)
	 *
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();

		ServletContext context = getServletContext();

		// データソース初期化のため、一旦コネクションを取得する
		// 未接続のデータソースのタブを表示するための対策
		try {
			String prefix = "java:comp/env/jdbc";
			InitialContext ctx = new InitialContext();
			NamingEnumeration<NameClassPair> ne = ctx.list(prefix);
			while (ne.hasMoreElements()) {
				NameClassPair nc = ne.nextElement();
				//context.log(nc.getName());
				DataSource ds = (DataSource) ctx.lookup(prefix + "/" + nc.getName());
				Connection conn = ds.getConnection();
				if (conn != null) conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// search RestApi classes
			List<Class<?>> classes = ClassScanUtil.findClassesFromPackage(getClass().getPackage().getName());
			for (Class<?> clazz : classes) {
				if (!Modifier.isAbstract(clazz.getModifiers()) &&
						!clazz.isInterface() && RestApi.class.isAssignableFrom(clazz) ) {
					Constructor<?> c = clazz.getConstructor(ServletContext.class);
					RestApi api = (RestApi) c.newInstance(context);
					// 明示的にinit() を呼び出す
					api.init();
					apiMap.put(api.getApiName(), api);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (非 Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		// 明示的にdestroy() を呼び出す
		for (RestApi api : apiMap.values()) {
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
//		ServletContext ctx = getServletContext();
//		try {
//			Enumeration<Driver> drivers = DriverManager.getDrivers();
//			while(drivers.hasMoreElements()) {
//				Driver d = drivers.nextElement();
//				if (d.getClass().getClassLoader() == getClass().getClassLoader()) {
//					// コンテキストクラスローダでロードされたものみアンロードする
//					DriverManager.deregisterDriver(d);
//					ctx.log("deregister driver [" + d.getClass().getName() + "] (from webapp classloader)");
//				}
//			}
//		} catch (Exception e) {
//			ctx.log("driver deregister error [" + e.getMessage() + "]", e);
//		}
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

		// ロギング対象の接続設定がある場合のみ処理する（セキュリティ対策)
		if (JdbcLoggerFactory.hasValidLogger()) {
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
					RestApi api = apiMap.get(params[1]);
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
		}
		// ロギング対象がないか、対象のAPIが見つからない場合は404を返す
		res.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
}
