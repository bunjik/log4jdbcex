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
package info.bunji.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 **********************************************************
 * implements Connection Wrapper.
 **********************************************************
 */
public class ConnectionProxy implements InvocationHandler {

	/** real Connection */
	private Connection _conn;

	private String _url;

	/**
	 **********************************************
	 * constractor
	 * @param conn real connection
	 * @param url connection url
	 **********************************************
	 */
	ConnectionProxy(Connection conn, String url) {
		//super(url);
		_url = url;
		_conn = conn;
	}

	/**
	 **********************************************
	 * get connection url.
	 *
	 * @return connection url
	 **********************************************
	 */
	String getUrl() {
		return _url;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			String name = method.getName();

			Object ret;
			if (name.equals("createStatement")) {
				Statement stmt = (Statement) method.invoke(_conn, args);
				ret = ProxyFactory.wrapStatement(stmt, _url);
			} else if (name.equals("prepareStatement")) {
				PreparedStatement stmt = (PreparedStatement) method.invoke(_conn, args);
				ret = ProxyFactory.wrapPreparedStatement(stmt, _url, args[0].toString());
			} else if (name.equals("prepareCall")) {
				CallableStatement stmt = (CallableStatement) method.invoke(_conn, args);
				ret = ProxyFactory.wrapCallableStatement(stmt, _url, args[0].toString());
			} else {
				ret = method.invoke(_conn, args);
			}
			return ret;
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
