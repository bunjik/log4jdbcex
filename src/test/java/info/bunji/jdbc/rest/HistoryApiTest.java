/**
 *
 */
package info.bunji.jdbc.rest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.junit.Test;

import info.bunji.jdbc.AbstractTest;
import info.bunji.jdbc.logger.impl.QueryInfo;

/**
 * @author f.kinoshita
 *
 */
public class HistoryApiTest extends  AbstractTest {

	/**
	 * {@link info.bunji.jdbc.rest.HistoryApi#getApiName()} のためのテスト・メソッド。
	 */
	@Test
	public void testGetApiName() {
		ServletContext context = mock(ServletContext.class);
		RestApi api = new HistoryApi(context);

		assertThat(api.getApiName(), is("history"));
	}

	/**
	 * {@link info.bunji.jdbc.rest.HistoryApi#postMergeProcess(java.util.Map)} のためのテスト・メソッド。
	 */
	@Test
	public void testPostMergeProcess() throws Exception {
		ServletContext context = mock(ServletContext.class);
		AbstractApi api = new HistoryApi(context);

		List<Object> orgList = new ArrayList<Object>();
		for (long l = 1; l <= 3L; l++) {
			QueryInfo qi1 = newInstance(QueryInfo.class, l, 1L, "sql", "ID_"+l, new Throwable());
			orgList.add(qi1);
		}

		Map<String, List<Object>> result = new HashMap<String, List<Object>>();
		result.put("host1", orgList);

		api.postMergeProcess(result);

		List<Object> resList = result.get("host1");
		assertThat(resList.size(), is(3));

		long time1 = ((QueryInfo) resList.get(0)).getTime();
		for(Object qi : resList) {
			long time2 = ((QueryInfo)qi).getTime();
			assertThat(time1, greaterThanOrEqualTo(time2));
		}
	}
}
