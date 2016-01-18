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

import info.bunji.jdbc.util.LoggerHelper;

/**
 **********************************************************
 * Connectionのwrapper実装
 **********************************************************
 */
public class ConnectionProxy extends LoggerHelper implements InvocationHandler {

	/** 実際のConnectionインスタンス */
	private Connection _instance;

	/**
	 **********************************************
	 * コンストラクタ
	 * @param instance
	 * @param url
	 **********************************************
	 */
	ConnectionProxy(Connection instance, String url) {
		super(url);
		_instance = instance;
	}

	/**
	 **********************************************
	 * dbの接続urlを取得する
	 * @return 接続url
	 **********************************************
	 */
	public String getUrl() {
		return url;
	}

	/**
	 **********************************************
	 *
	 **********************************************
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			String name = method.getName();

			Object ret;
			if (name.equals("createStatement")) {
				Statement stmt = (Statement) method.invoke(_instance, args);
				ret = ProxyFactory.wrapStatement(stmt, url);
			} else if (name.equals("prepareStatement")) {
				PreparedStatement stmt = (PreparedStatement) method.invoke(_instance, args);
				ret = ProxyFactory.wrapPreparedStatement(stmt, url, args[0].toString());
			} else if (name.equals("prepareCall")) {
				CallableStatement stmt = (CallableStatement) method.invoke(_instance, args);
				ret = ProxyFactory.wrapCallableStatement(stmt, url, args[0].toString());
			} else {
				ret = method.invoke(_instance, args);
			}
			return ret;
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
