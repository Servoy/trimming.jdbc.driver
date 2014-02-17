package com.servoy.trimming.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;

public class StatementInvocationHandler implements InvocationHandler
{
	private final Statement statement;

	public StatementInvocationHandler(Statement statement) {
		this.statement = statement;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object retValue = method.invoke(statement, args);
		if (retValue instanceof ResultSet) {
			InvocationHandler resultSetHandler = new TrimmingResultSetInvocationHandler((ResultSet) retValue);
			retValue = Proxy.newProxyInstance(WrappingDriver.class.getClassLoader(), new Class[] {ResultSet.class}, resultSetHandler);
		}
		return retValue;
	}

}
