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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 */
public class TestQueryApi extends AbstractApi {

//	private DataSource ds;

	private Random rand = new Random();

	List<Connection> connList = new ArrayList<Connection>();

	public TestQueryApi(ServletContext context) {
		super(context);
	}

	@Override
	public void init() {

//		try {
//			conn = ds.getConnection();
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void destroy() {
		try {
			for (Connection conn : connList) {
				conn.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void initDatasource() {
		try {
			InitialContext ctx = new InitialContext();
			NamingEnumeration<NameClassPair> ne = ctx.list("java:comp/env/jdbc");
			while (ne.hasMoreElements()) {
				NameClassPair nc = ne.nextElement();
				DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/" + nc.getName());
				connList.add(ds.getConnection());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
										throws ServletException, IOException {
		if (connList.isEmpty()) {
			initDatasource();
		}

		Statement stmt = null;
		try {
			String[] sql = new String[] {
				//"SELECT count(*) as COUNT FROM JdbcTest",
				"SeleCt 1",
				"Select aaa as item1,\nbbb as item2, ccc as item3 from dual",
				//"ERROR 1 as ABC",
				"SELECT 1 as ABC, 2 as DEF",
			};

			//stmt = conn.createStatement();
			stmt = connList.get(rand.nextInt(connList.size())).createStatement();
			stmt.executeQuery(sql[rand.nextInt(sql.length)]);
//			stmt.executeQuery(sql[rand.nextInt(sql.length)]);
//stmt.addBatch("UPDATE jdbctest SET");
//stmt.addBatch(sql[rand.nextInt(sql.length)]);
//stmt.addBatch(sql[rand.nextInt(sql.length)]);
//stmt.executeBatch();

			res.setStatus(HttpServletResponse.SC_OK);
		} catch(Exception e) {
			logger.error(e.getMessage());
//			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			res.setStatus(HttpServletResponse.SC_OK);
		} finally {
			try {
				if (stmt != null) stmt.close();
			} catch(Exception e) {}
		}
	}
}
