/*
 * (c) 2014 Rothsmith, LLC All Rights Reserved.
 */
package com.rothsmith.dao.dbutils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryLoader;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rothsmith.dao.DaoRuntimeException;
import com.rothsmith.dao.JdbcDao;
import com.rothsmith.utils.database.JDBCServiceLocator;

/**
 * A generic <a href="http://commons.apache.org/proper/commons-dbutils/">Apache
 * DbUtils</a> DAO.
 * 
 * @param <T>
 *            DTO class.
 * @param <P>
 *            Parameter list.
 * 
 * @author drothauser
 */
public class DbUtilsJdbcDao<T, P> implements JdbcDao<T, P> {

	/**
	 * SLF4J Logger for DbUtilsJdbcDao.
	 */
	private static final Logger LOGGER =
	    LoggerFactory.getLogger(DbUtilsJdbcDao.class);

	/**
	 * JDBC {@link DataSource}.
	 */
	private DataSource dataSource;

	/**
	 * The class of the DTO being persisted or null if no DTO used. The way this
	 * class is used depends on the type of DML operation:
	 * <ul>
	 * <li>SELECT - defines the return type
	 * <ul>
	 * <li>Note that if the <code>type</code> field is null, a {@link Map} class
	 * will be used.
	 * </ul>
	 * <li>INSERT, UPDATE, DELETE - defines the parameter type.
	 * </ul>
	 */
	private Class<T> type;

	/**
	 * Instance of {@link QueryRunner}.
	 */
	private QueryRunner queryRunner;

	/**
	 * SQL statement {@link Map}.
	 */
	private Map<String, String> statementMap;

	/**
	 * Construct DAO with DTO type.
	 * 
	 * @param type
	 *            Type of DTO
	 */
	public DbUtilsJdbcDao(final Class<T> type) {
		this.type = type;
	}

	/**
	 * Construct DAO initializing it with parameters from a properties file.
	 * 
	 * @param propsFile
	 *            Properties file
	 */
	@SuppressWarnings("unchecked")
	public DbUtilsJdbcDao(final String propsFile) {

		QueryLoader loader = QueryLoader.instance();
		try {
			statementMap = loader.load(propsFile);

			String dto = statementMap.get("dto");
			type = (Class<T>) Class.forName(dto);

			String jndiName = statementMap.get("dataSource");
			dataSource =
			    JDBCServiceLocator.getInstance().getDataSource(jndiName);

			boolean pmdKnownBroken = false;
			try (Connection conn = dataSource.getConnection();) {
				String dbProductName =
				    conn.getMetaData().getDatabaseProductName();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Database production name: " + dbProductName);
				}
				pmdKnownBroken =
				    StringUtils.equalsIgnoreCase(dbProductName, "Oracle");
			} catch (SQLException e) {
				LOGGER.warn(
				    "Couldn't get database product name from connection: " + e);
			}

			this.queryRunner = new QueryRunner(dataSource, pmdKnownBroken);

			statementMap.remove("type");
			statementMap.remove("datasource");

		} catch (ClassNotFoundException | IOException | NamingException e) {
			String msg = "Constructor Error: " + e;
			LOGGER.error(msg, e);
			throw new DaoRuntimeException(msg, e);
		}

	}

	/**
	 * Default Constructor.
	 */
	public DbUtilsJdbcDao() {
		// for constructing DAO with no DTO type.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> select(P params) {

		return selectByStatement("query.select", params);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int insert(T dto) {

		String statementId = "query.insert";

		String sql = statementMap.get(statementId);
		if (StringUtils.isEmpty(sql)) {
			throw new IllegalArgumentException(String.format(
			    "No sql statement found for statement \"%s\"", statementId));
		}

		BigDecimal id = BigDecimal.ZERO;
		try {

			@SuppressWarnings("unchecked")
			Object[] paramArray = fetchParamValues(sql, (P) dto);

			String statement = sql.replaceAll(":(\\w+)", "?");

			if (queryRunner.isPmdKnownBroken()) {
				queryRunner.update(statement, paramArray);
			} else {
				id = queryRunner.insert(statement,
				    new ScalarHandler<BigDecimal>(), paramArray);
			}

			LOGGER.info("newkey = " + id);

		} catch (SQLException e) {
			String msg = "SQLException caught: " + e;
			LOGGER.error(msg, e);
			throw new DaoRuntimeException(msg, e);
		}

		return id.intValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int update(T dto) {
		String statementId = "query.update";

		String sql = statementMap.get(statementId);
		if (StringUtils.isEmpty(sql)) {
			throw new IllegalArgumentException(String.format(
			    "No sql statement found for statement \"%s\"", statementId));
		}

		int recordCount = 0;
		try {

			@SuppressWarnings("unchecked")
			Object[] paramArray = fetchParamValues(sql, (P) dto);

			String statement = sql.replaceAll(":(\\w+)", "?");

			recordCount = queryRunner.update(statement, paramArray);

		} catch (SQLException e) {
			String msg = "SQLException caught: " + e;
			LOGGER.error(msg, e);
			throw new DaoRuntimeException(msg, e);
		}

		return recordCount;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(T dto) {

		String statementId = "query.delete";

		String sql = statementMap.get(statementId);
		if (StringUtils.isEmpty(sql)) {
			throw new IllegalArgumentException(String.format(
			    "No sql statement found for statement \"%s\"", statementId));
		}

		int recordCount = 0;
		try {

			@SuppressWarnings("unchecked")
			Object[] paramArray = fetchParamValues(sql, (P) dto);

			String statement = sql.replaceAll(":(\\w+)", "?");

			recordCount = queryRunner.update(statement, paramArray);

		} catch (SQLException e) {
			String msg = "SQLException caught: " + e;
			LOGGER.error(msg, e);
			throw new DaoRuntimeException(msg, e);
		}

		return recordCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> select(String sql, P params) {

		List<T> results = null;
		try {

			Object[] paramArray = fetchParamValues(sql, params);

			String query = sql.replaceAll(":(\\w+)", "?");

			BeanListHandler<T> rsh = new BeanListHandler<T>(type,
			    new BasicRowProcessor(new GenerousBeanProcessor()));
			results = queryRunner.query(query, rsh, paramArray);

		} catch (SQLException e) {
			String msg = "SQLException caught: " + e;
			LOGGER.error(msg, e);
			throw new DaoRuntimeException(msg, e);
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> select(String sql) {

		List<T> results = null;

		try {

			BeanListHandler<T> rsh = new BeanListHandler<T>(type,
			    new BasicRowProcessor(new GenerousBeanProcessor()));
			results = queryRunner.query(sql, rsh);

		} catch (SQLException e) {
			String msg = "SQLException caught: " + e;
			LOGGER.error(msg, e);
			throw new DaoRuntimeException(msg, e);
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> selectAsMap(String sql) {

		List<Map<String, Object>> results = null;

		try {

			MapListHandler rsh = new MapListHandler();
			results = queryRunner.query(sql, rsh);

		} catch (SQLException e) {
			String msg = "SQLException caught: " + e;
			LOGGER.error(msg, e);
			throw new DaoRuntimeException(msg, e);
		}

		return results;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> getType() {

		return this.type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setType(Class<T> type) {

		this.type = type;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataSource(DataSource dataSource) {

		this.dataSource = dataSource;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getStatementMap() {
		return statementMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStatementMap(Map<String, String> statementMap) {
		this.statementMap = statementMap;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> selectByStatement(String statementId, P params) {

		String sql = statementMap.get(statementId);
		if (StringUtils.isEmpty(sql)) {
			throw new IllegalArgumentException(String.format(
			    "No sql statement found for statement \"%s\"", statementId));
		}

		List<T> results = null;
		try {

			Object[] paramArray = fetchParamValues(sql, params);

			String query = sql.replaceAll(":(\\w+)", "?");

			BeanListHandler<T> rsh = new BeanListHandler<T>(type,
			    new BasicRowProcessor(new GenerousBeanProcessor()));
			results = queryRunner.query(query, rsh, paramArray);

		} catch (SQLException e) {
			String msg = "SQLException caught: " + e;
			LOGGER.error(msg, e);
			throw new DaoRuntimeException(msg, e);
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> selectByStatement(String statementId) {

		String sql = statementMap.get(statementId);
		if (StringUtils.isEmpty(sql)) {
			throw new IllegalArgumentException(String.format(
			    "No sql statement found for statement \"%s\"", statementId));
		}

		LOGGER.info("Running:\n" + sql);

		List<T> results = null;
		try {

			BeanListHandler<T> rsh = new BeanListHandler<T>(type,
			    new BasicRowProcessor(new GenerousBeanProcessor()));
			results = queryRunner.query(sql, rsh);

		} catch (SQLException e) {
			String msg = "SQLException caught: " + e;
			LOGGER.error(msg, e);
			throw new DaoRuntimeException(msg, e);
		}

		return results;
	}

	/**
	 * This method parses the parameter fields from the query (identified by
	 * :string pattern) and returns an array from their values in the given
	 * parameter object.
	 * 
	 * @param query
	 *            the SQL query string
	 * @param params
	 *            the bean containing the parameters
	 * @return an array of query parameter values
	 */
	private Object[] fetchParamValues(String query, P params) {

		Object[] paramArray = null;

		if (params instanceof Object[]) {

			paramArray = (Object[]) params;

		} else {

			Pattern pattern = Pattern.compile(":(\\w+)");
			Matcher matcher = pattern.matcher(query);

			PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

			List<Object> paramList = new ArrayList<Object>();
			try {
				while (matcher.find()) {
					String paramName = matcher.group(1);
					Object param =
					    propertyUtilsBean.getProperty(params, paramName);
					paramList.add(param);
				}
				paramArray = paramList.toArray();
			} catch (IllegalAccessException | InvocationTargetException
			        | NoSuchMethodException e) {
				String msg = "Error with PropertyUtilsBean: " + e;
				LOGGER.error(msg, e);
				throw new DaoRuntimeException(msg, e);
			}
		}

		return paramArray;
	}

}
