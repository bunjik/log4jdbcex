/**
 *
 */
package info.bunji.jdbc.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import info.bunji.jdbc.AbstractTest;
import net.arnx.jsonic.JSON;

/**
 *
 * @author f.kinoshita
 */
public class RestApiServletTest extends AbstractTest {

	private static ServletRunner sr = new ServletRunner();
	private static ServletUnitClient client;

	@BeforeClass
	public static void beforeClass() throws Exception {
		AbstractTest.setUpBeforeClass();
		// register servlet mapping

		sr.registerServlet("log4jdbcex/*", RestApiServlet.class.getName());
	}

	@AfterClass
	public static void afterClass() throws Exception {
		sr.shutDown();
	}

	@Before
	public void setUp() throws Exception {
		// get client
		client = sr.newClient();
		client.getClientProperties().setAutoRedirect(true);
		client.getClientProperties().setMaxRedirects(3);

		StringBuilder buf = new StringBuilder();
		buf.append("{ 'log4jdbcDs': {")
			.append(" format: false")
			.append("} }");

		WebRequest req = new PutMethodWebRequest(
								"http://localhost/log4jdbcex/setting",
								new ByteArrayInputStream(buf.toString().getBytes()),
								"application/json; charset=UTF-8");
		client.getResponse(req);
	}

//	@Test
//	public void testDestory() {
//		Servlet servlet = new RestApiServlet();
//		try {
//			servlet.destroy();
//
//		} catch (Exception e) {
//			fail("unknown error(" + e.getMessage() + ")");
//		}
//	}

	@Test
	public void testRoot() throws Exception {
		WebRequest req = new GetMethodWebRequest("http://localhost/log4jdbcex/ui/index.html");
		WebResponse res = client.getResource(req);

		assertThat(res.getResponseCode(), is(HttpServletResponse.SC_OK));
	}

	@Test
	public void testRedirect() throws Exception {
		WebRequest req = new GetMethodWebRequest("http://localhost/log4jdbcex/ui");
		WebResponse res = client.getResponse(req);

		assertThat(res.getURL().toString(), is("http://localhost/log4jdbcex/ui/"));
		assertThat(res.getResponseCode(), is(HttpServletResponse.SC_OK));
	}

	@Test
	public void testHistory() throws Exception {
		WebRequest req = new GetMethodWebRequest("http://localhost/log4jdbcex/history");
		WebResponse res = client.getResponse(req);

		assertThat(res.getResponseCode(), is(HttpServletResponse.SC_OK));

		Map<String, List<Map<String, Object>>> results = JSON.decode(res.getInputStream());

		assertThat(results.containsKey("log4jdbcDs"), is(true));

		List<Map<String, Object>> result = results.get("log4jdbcDs");
		Map<String,Object> values = result.get(0);
		assertThat(values.containsKey("dataSource"), is(true));
		assertThat(values.containsKey("elapsed"), is(true));
		assertThat(values.containsKey("error"), is(true));
		assertThat(values.containsKey("errorMsg"), is(true));
		assertThat(values.containsKey("host"), is(true));
		assertThat(values.containsKey("sql"), is(true));

		// TODO:check values

	}

	@Test
	public void testRunning() throws Exception {
		// dummy query
		Runnable r = new Runnable() {
			@Override
			public void run() {
				Connection conn = null;
				try {
					conn = getConnection("log4jdbcDs");
					Statement stmt = conn.createStatement();
					stmt.executeQuery("select waitfunc(aaa) from test");
				} catch (Exception e) {
					// do nothing.
				} finally {
					closeQuietly(conn);
				}
			}
		};
		new Thread(r).start();
		Thread.sleep(500);

		WebRequest req = new GetMethodWebRequest("http://localhost/log4jdbcex/running");
		WebResponse res = client.getResponse(req);

		assertThat(res.getResponseCode(), is(HttpServletResponse.SC_OK));

		Map<String, List<Map<String, Object>>> results = JSON.decode(res.getInputStream());
		assertThat(results.containsKey("log4jdbcDs"), is(true));
		assertThat(results.get("log4jdbcDs").size(), is(1));

		Thread.sleep(2000);

		req = new GetMethodWebRequest("http://localhost/log4jdbcex/running");
		res = client.getResponse(req);
		results = JSON.decode(res.getInputStream());
		assertThat(results.containsKey("log4jdbcDs"), is(true));
		assertThat(results.get("log4jdbcDs").size(), is(0));
	}

	@Test
	public void testGetSetting() throws Exception {
		WebRequest req = new GetMethodWebRequest("http://localhost/log4jdbcex/setting");
		WebResponse res = client.getResponse(req);

		Map<String, Map<String, Object>> settings = JSON.decode(res.getInputStream());
		assertThat(settings.containsKey("log4jdbcDs"), is(true));

		//"_default_": {
		//	"timeThreshold": 0,
		//	"acceptFilter": ".*",
		//	"ignoreFilter": null,
		//	"historyCount": 30,
		//	"format": true
		//}
		Map<String, Object> setting = settings.get("log4jdbcDs");
		assertThat(setting, hasEntry("timeThreshold", (Object)BigDecimal.valueOf(0)));
		assertThat(setting, hasEntry("acceptFilter", (Object)".*"));
		assertThat(setting, hasEntry("ignoreFilter", null));
		assertThat(setting, hasEntry("limitLength", (Object)BigDecimal.valueOf(-1)));
		assertThat(setting, hasEntry("historyCount", (Object)BigDecimal.valueOf(30)));
		assertThat(setting, hasEntry("format", (Object)false));
	}

	@Test
	public void testPutSetting() throws Exception {
		// 変更前データの取得
		WebRequest req = new GetMethodWebRequest("http://localhost/log4jdbcex/setting");
		WebResponse res = client.getResponse(req);

		StringBuilder buf = new StringBuilder();
		buf.append("{ 'log4jdbcDs': {")
			.append(" timeThreshold: 1,")
			.append(" acceptFilter: '^SELECT',")
			.append(" ignoreFilter: '^AAA'")
			.append("} }");

		WebRequest putReq = new PutMethodWebRequest(
								"http://localhost/log4jdbcex/setting",
								new ByteArrayInputStream(buf.toString().getBytes()),
								"application/json; charset=UTF-8");
		WebResponse putRes = client.getResponse(putReq);

		Map<String,Map<String, Object>> settings = JSON.decode(putRes.getText());

		Map<String, Object> setting = settings.get("log4jdbcDs");
		assertThat(setting, hasEntry("timeThreshold", (Object)BigDecimal.valueOf(1)));
		assertThat(setting, hasEntry("acceptFilter", (Object)"^SELECT"));
		assertThat(setting, hasEntry("ignoreFilter", (Object)"^AAA"));
		assertThat(setting, hasEntry("limitLength", (Object)BigDecimal.valueOf(-1)));
		assertThat(setting, hasEntry("historyCount", (Object)BigDecimal.valueOf(30)));
		assertThat(setting, hasEntry("format", (Object)false));

		// 変更前データの復旧
		client.getResponse(new PutMethodWebRequest("http://localhost/log4jdbcex/setting",
								res.getInputStream(),
								"application/json; charset=UTF-8"));
	}

//	@Test
//	public void testBroadcast() throws Exception {
//		WebRequest req = new GetMethodWebRequest("http://localhost/log4jdbcex/history?servers=localhost");
//		WebResponse res = client.getResponse(req);
//
//		System.out.println(res.getText());
//	}

}
