package com.servoy.trimming.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.ResultSet;

public class TrimmingResultSetInvocationHandler implements InvocationHandler {

	private final ResultSet resultSet;
	
	public TrimmingResultSetInvocationHandler(ResultSet resultSet) {
		this.resultSet = resultSet;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object retValue = method.invoke(resultSet, args);
		return retValue instanceof String?((String)retValue).trim():retValue;
	}

}
