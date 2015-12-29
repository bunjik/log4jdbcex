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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * クラスパス上の指定コンテンツを返す
 *
 * @author f.kinoshita
 */
public class ResourceApi extends AbstractApi {

	/** 最終更新日(インスタンス生成時を固定で保持) */
	private static final long LAST_MODIFIED = (System.currentTimeMillis() / 1000) * 1000;

	/** 静的コンテンツを保持するパス(クラスパス) */
	private final String resourceBase;

	/**
	 * コンストラクタ
	 * @param context
	 */
	public ResourceApi(ServletContext context) {
		super(context);
		resourceBase = getClass().getPackage().getName().replace(".", "/") + "/";
		logger.trace(resourceBase);
	}

	/* (非 Javadoc)
	 * @see info.bunji.jdbc.rest.AbsractApi#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		// 更新日時をチェックする
		if (req.getDateHeader("If-Modified-Since") >= LAST_MODIFIED) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}

		InputStream  is = null;
		try {
			String path = getApiPath(req);
			// ファイル名が指定されていない場合、"index.html"を補完
			if (path == null) {
				res.sendRedirect(req.getRequestURI() + "/");
				return;
			} else if (path.isEmpty() || path.endsWith("/")) {
				path += "index.html";
			}
			logger.trace(path);
			is = getResourceStream(path);
			if (is == null) {
				res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else {
				logger.trace(getApiPath((req)));
				res.setDateHeader("Last-modified", LAST_MODIFIED);
				String mimeType = context.getMimeType(getApiPath(req));
				if (mimeType != null) res.setContentType(mimeType);
				res.setCharacterEncoding("utf-8");
				res.setBufferSize(4096);
				OutputStream os = res.getOutputStream();
				byte[] buf = new byte[4096];
				int len;
				while((len = is.read(buf)) > 0) {
					os.write(buf, 0, len);
				}
				res.setStatus(HttpServletResponse.SC_OK);
				os.flush();
			}
		} finally {
			closeQuietly(is);
		}
	}

	protected InputStream getResourceStream(String path) throws IOException {
		logger.trace(resourceBase + path);
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceBase + path);
		if (is != null) {
			return new BufferedInputStream(is);
		}
		return null;
	}
}
