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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;

/**
 *
 */
class TestQueryApi extends AbstractApi {

	protected JdbcLogger logger = JdbcLoggerFactory.getLogger();

	private Random rand = new Random();

	List<Connection> connList = new ArrayList<Connection>();

	public TestQueryApi() {
		this(null);
	}

	public TestQueryApi(ServletContext context) {
		super(context);
	}

	/*
	 * (非 Javadoc)
	 * @see info.bunji.jdbc.rest.RestApi#getApiName()
	 */
	@Override
	public String getApiName() {
		return "test";
	}

	@Override
	public void init() throws ServletException {
		super.init();

		if (connList.isEmpty()) {
			initDatasource();

			Statement stmt = null;
			for (Connection conn : connList) {
				try {
					stmt = conn.createStatement();
					stmt.execute("create table test(aaa int)");
				} catch (Exception e) {
					// do nothing.
				} finally {
					if(stmt != null) {
						try { stmt.close(); } catch(Exception e) {}
					}
				}
			}
		}
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
		Statement stmt = null;
		try {
			if(rand.nextInt(2) == 0) {
//if(false) {
			String[] sql = new String[] {
					"SELECT count(*) as COUNT FROM JdbcTest",
					"Select aaa as item1,\nbbb as item2, ccc as item3 from dual",
					"SELECT 1 as ABC, 2 as DEF",
					//"ERROR 1 as ABC",
					//"SeleCt 1",
				};

				//stmt = conn.createStatement();
				stmt = connList.get(rand.nextInt(connList.size())).createStatement();
				stmt.executeQuery(sql[rand.nextInt(sql.length)]);
//			stmt.executeQuery(sql[rand.nextInt(sql.length)]);
//stmt.addBatch("UPDATE jdbctest SET");
//stmt.addBatch(sql[rand.nextInt(sql.length)]);
//stmt.addBatch(sql[rand.nextInt(sql.length)]);
//stmt.executeBatch();
			} else {
				String sql = "SELECT * from test where aaa=?";
				PreparedStatement pstmt = connList.get(rand.nextInt(connList.size())).prepareStatement(sql);
				pstmt.setInt(1, 1);
//				pstmt.setString(1, "1");
//				pstmt.setBinaryStream(1, new ByteArrayInputStream(new byte[0]));
ResultSet rs = pstmt.executeQuery();
rs.close();
			}
			res.setStatus(HttpServletResponse.SC_OK);
		} catch(Exception e) {
//logger.error("error in TestQueryAPI(" + e.getMessage() + ")");
//			logger.error(e.getMessage(), e);
//			logger.error(e.getMessage());
//			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			res.setStatus(HttpServletResponse.SC_OK);
		} finally {
			try {
				if (stmt != null) stmt.close();
			} catch(Exception e) {}
		}
	}
}
