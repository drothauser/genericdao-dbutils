/*
 * (c) 2015 FCCI Insurance Group All Rights Reserved.
 */
package com.rothsmith.dao.dbutils;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rothsmith.utils.database.JDBCServiceLocator;

/**
 * Setup Derby database for testing.
 * 
 * @author drothauser
 *
 */
public final class DbUtilsTestSetup {

	/**
	 * SLF4J Logger for DbUtilsTestSetup.
	 */
	private static final Logger LOGGER =
	    LoggerFactory.getLogger(DbUtilsTestSetup.class);

	/**
	 * private constructor to thwart instantiation.
	 */
	private DbUtilsTestSetup() {
		// Utility class should not be instantiated.
	}

	/**
	 * Create test Derby database for testing.
	 * 
	 * @param propsFile
	 *            derby/daogen.properties
	 * 
	 * @throws IOException
	 *             thrown if error accessing properties file.
	 * @throws NamingException
	 *             thrown if problems connecting with JNDI datasource.
	 * @throws SQLException
	 *             thrown if there's a database connection issue.
	 */
	public static void setup(String propsFile)
	        throws IOException, NamingException, SQLException {

		Properties props = new Properties();
		if (StringUtils.startsWithIgnoreCase(propsFile, "classpath:")) {
			props.load(Thread.currentThread().getContextClassLoader()
			    .getResourceAsStream(StringUtils
			        .removeStartIgnoreCase(propsFile, "classpath:")));
		} else {
			props.load(new FileInputStream(new File(
			    StringUtils.removeStartIgnoreCase(propsFile, "file://"))));
		}

		String jndiName = props.getProperty("db.jndi");
		DataSource dataSource =
		    JDBCServiceLocator.getInstance().getDataSource(jndiName);

		try (Connection conn = dataSource.getConnection()) {

			String createSql = fetchSql(props, "db.create.sql");

			String populateSql = fetchSql(props, "db.populate.sql");

			String initSql = createSql + populateSql;

			String[] sqlStmts = initSql.split("(?<!\\-{2}.{0,100});");
			for (String sql : sqlStmts) {
				try (Statement stmt = conn.createStatement()) {
					LOGGER.info(String.format("%nExecuting %s", sql));
					stmt.executeUpdate(sql);
					LOGGER.info("\nDone!");
				} catch (SQLException e) {
					String msg = String.format(
					    "Initialization error running SQL statement: %s: %s",
					    sql, e);
					LOGGER.error(msg, e);
					fail("Initialization error running SQL statements: "
					    + e.getMessage());
				}
			}
		}

	}

	/**
	 * Method to read the sql files specified in the given property of the
	 * properties file and concatenate their contents into a string.
	 * 
	 * @param props
	 *            {@link Properties} object that contains sql parameters
	 * @param sqlProperty
	 *            Name of the property in the property file that contains names
	 *            of SQL files.
	 * @return String containing the contents of all the SQL files
	 * @throws IOException
	 *             thrown if there is problem accessing a SQL file
	 */
	private static String fetchSql(Properties props, String sqlProperty)
	        throws IOException {

		String sqlFileNames =
		    StringUtils.defaultString(props.getProperty(sqlProperty));

		StringBuilder sb = new StringBuilder();

		for (String sqlFileName : StringUtils
		    .split(StringUtils.deleteWhitespace(sqlFileNames), ',')) {

			URL sqlFileURL = Thread.currentThread().getContextClassLoader()
			    .getResource(sqlFileName);
			if (sqlFileURL == null) {
				throw new IOException("Couldn't find: " + sqlFileName);
			}

			String sql = StringUtils.trim(IOUtils.toString(sqlFileURL));

			sb.append(sql);

		}

		return sb.toString();

	}

}
