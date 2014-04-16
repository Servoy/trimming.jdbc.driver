package com.servoy.trimming.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public class WrappingDriver implements Driver {
	
    static
    {
        try
        {
            // moved the registerDriver from the constructor to here
            // because some clients call the driver themselves (I know, as
            // my early jdbc work did - and that was based on other examples).
            // Placing it here, means that the driver is registered once only.
            java.sql.DriverManager.registerDriver(new WrappingDriver());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

	private Driver lastUnderlyingDriverRequested;

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		String[] urlAndDriver = getUrlAndDriver(url);
		Driver d = getUnderlyingDriver(urlAndDriver);
		if (d == null) {
			return null;
		}

		lastUnderlyingDriverRequested = d;
		Connection c = d.connect(urlAndDriver[0], info);

		if (c == null) {
			throw new SQLException("invalid or unknown driver url: " + url);
		}
		InvocationHandler connectionInvocationHandler = new ConnectionInvocationHandler(c);
		Connection proxy = (Connection) Proxy.newProxyInstance(WrappingDriver.class.getClassLoader(), new Class[] {Connection.class}, connectionInvocationHandler);
		return proxy;
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		Driver d = getUnderlyingDriver(getUrlAndDriver(url));
		if (d != null) {
			lastUnderlyingDriverRequested = d;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Given a <code>jdbc:log4</code> type URL, find the underlying real driver
	 * that accepts the URL.
	 * 
	 * @param url
	 *            JDBC connection URL.
	 * 
	 * @return Underlying driver for the given URL. Null is returned if the URL
	 *         is not a <code>jdbc:wrapping</code> type URL or there is no
	 *         underlying driver that accepts the URL.
	 * 
	 * @throws SQLException
	 *             if a database access error occurs.
	 */
	private Driver getUnderlyingDriver(String[] urlAndDriver) throws SQLException {
		if (urlAndDriver != null) {
			try {
				Class.forName(urlAndDriver[1]);
			} catch(Exception e) {
				// ignore and just try it.
				System.out.println(e);
			}
			Enumeration<Driver> e = DriverManager.getDrivers();

			while (e.hasMoreElements()) {
				Driver d = e.nextElement();

				if (d.acceptsURL(urlAndDriver[0])) {
					return d;
				}
			}
		}
		return null;
	}
	
	private String[] getUrlAndDriver(String url) {
		if (url.startsWith("jdbc:wrapping")) {
			url = url.substring(14);
			
			int index = url.indexOf(':');
			String driverName = url.substring(0,index);
			url = url.substring(index+1);
			return new String[] {url,driverName};
		}
		return null;
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		String[] urlAndDriver = getUrlAndDriver(url);
		Driver d = getUnderlyingDriver(urlAndDriver);
		if (d == null) {
			return new DriverPropertyInfo[0];
		}

		lastUnderlyingDriverRequested = d;
		return d.getPropertyInfo(urlAndDriver[0], info);
	}

	@Override
	public int getMajorVersion() {
		if (lastUnderlyingDriverRequested == null) {
			return 1;
		} else {
			return lastUnderlyingDriverRequested.getMajorVersion();
		}
	}

	@Override
	public int getMinorVersion() {
		if (lastUnderlyingDriverRequested == null) {
			return 1;
		} else {
			return lastUnderlyingDriverRequested.getMinorVersion();
		}
	}

	@Override
	public boolean jdbcCompliant() {
		return lastUnderlyingDriverRequested != null
				&& lastUnderlyingDriverRequested.jdbcCompliant();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		if (lastUnderlyingDriverRequested == null)
			throw new SQLFeatureNotSupportedException();
		return lastUnderlyingDriverRequested.getParentLogger();
	}

}
