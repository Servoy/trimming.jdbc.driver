package com.servoy.trimming.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Statement;

public class ConnectionInvocationHandler implements InvocationHandler {

	private final Connection connection;

	public ConnectionInvocationHandler(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object retValue = method.invoke(connection, args);
		if (retValue instanceof Statement) {
			StatementInvocationHandler statementInvocationHandler = new StatementInvocationHandler((Statement) retValue);
			retValue = Proxy.newProxyInstance(WrappingDriver.class.getClassLoader(), new Class[] {CallableStatement.class}, statementInvocationHandler);
		}
		return retValue;
	}

}
