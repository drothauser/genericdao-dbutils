/*
 * (c) 2015 Rothsmith, LLC All Rights Reserved.
 */
package com.rothsmith.dao.dbutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rothsmith.dao.DaoRuntimeException;
import com.rothsmith.dao.dbutils.DbUtilsJdbcDao;
import com.rothsmith.genericdao.PartyDto;

/**
 * Tests for PartyDao.
 * 
 * @author drothauser
 */
@SuppressWarnings("checkstyle:magicnumber")
public class PartyDaoTest {

	/**
	 * SLF4J Logger for PartyDaoTest.
	 */
	private static final Logger LOGGER =
	    LoggerFactory.getLogger(PartyDaoTest.class);

	/**
	 * {@link DbUtilsJdbcDao} to test.
	 */
	private DbUtilsJdbcDao<PartyDto, PartyDto> partyDao;

	/**
	 * Create database objects for testing.
	 * 
	 * @throws IOException
	 *             possible problem loading the properties file
	 * @throws SQLException
	 *             possible SQL error
	 * @throws NamingException
	 *             thrown if DB JNDI name isn't found
	 */
	@BeforeClass
	public static void setUpBeforeClass()
	        throws IOException, SQLException, NamingException {

		String propsFile = "classpath:derby/daogen-derby.properties";

		DbUtilsTestSetup.setup(propsFile);

	}

	/**
	 * Create a {@link DbUtilsJdbcDao} to test.
	 * 
	 */
	@Before
	public void setUp() {

		partyDao =
		    new DbUtilsJdbcDao<PartyDto, PartyDto>("/partydao.properties");

	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#select(Object)} using a loose SQL
	 * statement.
	 */
	@Test
	public void testSelectSql() {

		List<PartyDto> partyList = partyDao.select("select * from PARTY");
		for (PartyDto partyDto : partyList) {
			LOGGER.info(partyDto.toString());
		}
	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#select(String)} using a DTO with
	 * the ID set. This will test the "query.select" statement in the properties
	 * file.
	 */
	@Test
	public void testSelectDtoParam() {

		PartyDto partyDtoParam = new PartyDto();
		partyDtoParam.setId(1);
		List<PartyDto> partyList = partyDao.select(partyDtoParam);
		assertSame(1, partyList.size());
		LOGGER.info(partyList.get(0).toString());

	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#select(String)} using a loose SQL
	 * statement.
	 */
	@Test(expected = DaoRuntimeException.class)
	public void testSelectBadSql() {

		partyDao.select("select * from BOGUS");

	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#select(String)} using a loose SQL
	 * statement.
	 */
	@Test(expected = DaoRuntimeException.class)
	public void testSelectBadSqlParams() {

		partyDao.select("select * from BOGUS", new PartyDto());

	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#selectAsMap(String)} using a loose
	 * SQL statement and no type. This will cause the DAO to return a Map List.
	 */
	@Test
	public void testSelectSqlAsMap() {

		List<Map<String, Object>> results =
		    partyDao.selectAsMap("select * from PARTY");

		assertFalse(results.isEmpty());

		for (Map<String, Object> map : results) {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				LOGGER.info(entry.getKey() + "=" + entry.getValue());
			}
		}

	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#selectAsMap(String)} using a loose,
	 * invalid SQL statement.
	 */
	@Test(expected = DaoRuntimeException.class)
	public void testSelectBadSqlAsMap() {

		partyDao.selectAsMap("select * from BOGUS");

	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#select(String)} using a loose SQL
	 * statement with parameters.
	 */
	@Test
	public void testSelectSqlParam() {

		PartyDto partyDtoParam = new PartyDto();
		partyDtoParam.setFoundedYear(1789);

		List<PartyDto> partyList = partyDao.select(
		    "select * from PARTY where FOUNDED_YEAR = :foundedYear",
		    partyDtoParam);

		assertFalse(partyList.isEmpty());

		for (PartyDto partyDto : partyList) {
			LOGGER.info(partyDto.toString());
		}
	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#select(String)} using a loose SQL
	 * statement using a DTO as a parameter with 2 values set.
	 */
	@Test
	public void testSelectSqlDto2() {

		PartyDto partyDtoParam = new PartyDto();
		partyDtoParam.setFoundedYear(1790);
		partyDtoParam.setEndYear(1820);

		List<PartyDto> partyList = partyDao.select(
		    "select * from PARTY where "
		        + "FOUNDED_YEAR = :foundedYear and END_YEAR = :endYear",
		    partyDtoParam);

		assertFalse(partyList.isEmpty());

		for (PartyDto partyDto : partyList) {
			LOGGER.info(partyDto.toString());
		}
	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#select(String)} using a loose SQL
	 * statement with parameters.
	 */
	@Test
	public void testSelectSql2ParamsArray() {

		PartyDto partyDtoParam = new PartyDto();
		partyDtoParam.setFoundedYear(1790);
		partyDtoParam.setEndYear(1820);

		Object[] argsArray = { 1790, 1820 };

		DbUtilsJdbcDao<PartyDto, Object> partyDaoArgsArray =
		    new DbUtilsJdbcDao<PartyDto, Object>("/partydao.properties");

		List<PartyDto> partyList = partyDaoArgsArray.select(
		    "select * from PARTY where "
		        + "FOUNDED_YEAR = :foundedYear and END_YEAR = :endYear",
		    argsArray);

		assertFalse(partyList.isEmpty());

		for (PartyDto partyDto : partyList) {
			LOGGER.info(partyDto.toString());
		}
	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#selectByStatement(String)} using a
	 * statement that doesn't require parameters.
	 */
	@Test
	public void testSelectByStatementNoParams() {

		List<PartyDto> partyList =
		    partyDao.selectByStatement("query.selectMaxId");

		assertFalse(partyList.isEmpty());

		for (PartyDto partyDto : partyList) {
			LOGGER.info(partyDto.toString());
		}

	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#selectByStatement(String)} using a
	 * statement that doesn't exist.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSelectBadStatement() {

		partyDao.selectByStatement("query.BOGUS");

	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#selectByStatement(String)} using a
	 * statement id with parameters.
	 */
	@Test
	public void testSelectByStatementIdParams() {

		PartyDto partyDtoParam = new PartyDto();
		partyDtoParam.setId(1);
		List<PartyDto> partyList =
		    partyDao.selectByStatement("query.select", partyDtoParam);

		assertFalse(partyList.isEmpty());

		for (PartyDto partyDto : partyList) {
			LOGGER.info(partyDto.toString());
		}
	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#selectByStatement(String)} using a
	 * statement id with an uninitialized parameter object.
	 */
	@Test
	public void testSelectByStatementIdEmptyParams() {

		List<PartyDto> partyList =
		    partyDao.selectByStatement("query.select", new PartyDto());
		assertTrue(partyList.isEmpty());
	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#insert(Object)}.
	 */
	@Test
	public void testInsert() {
		PartyDto dto = new PartyDto();
		String name = "Progressive Party (Bull Moose Party)";
		dto.setName(name);
		dto.setFoundedYear(1912);
		dto.setEndYear(1914);
		partyDao.insert(dto);

		List<PartyDto> partyList =
		    partyDao.select(
		        "select * from PARTY where "
		            + "FOUNDED_YEAR = :foundedYear and END_YEAR = :endYear",
		        dto);

		assertSame(1, partyList.size());

		for (PartyDto partyDto : partyList) {
			assertEquals(name, partyDto.getName());
			LOGGER.info(partyDto.toString());
		}

	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#update(Object)}.
	 */
	@Test
	public void testUpdate() {
		PartyDto dto = new PartyDto();
		String originalName = "Native American Party";
		dto.setName(originalName);
		dto.setFoundedYear(1845);
		dto.setEndYear(1860);
		int id = partyDao.insert(dto);

		List<PartyDto> partyList =
		    partyDao.select(
		        "select * from PARTY where "
		            + "FOUNDED_YEAR = :foundedYear and END_YEAR = :endYear",
		        dto);

		assertSame(1, partyList.size());

		PartyDto partyDto = partyList.get(0);
		assertEquals(originalName, partyDto.getName());
		LOGGER.info(partyDto.toString());

		dto.setId(id);
		String succecedByName = "Know-Nothing Party";
		dto.setName(succecedByName);
		int recordCount = partyDao.update(dto);
		assertSame(1, recordCount);

		List<PartyDto> updatedPartyList =
		    partyDao.select("select * from PARTY where " + "NAME = :name", dto);

		assertSame(1, updatedPartyList.size());
		assertEquals(succecedByName, updatedPartyList.get(0).getName());
		LOGGER.info(updatedPartyList.get(0).toString());

	}

	/**
	 * Test method for {@link DbUtilsJdbcDao#delete(Object)}.
	 */
	@Test
	public void testDelete() {
		PartyDto dto = new PartyDto();
		String name = "Communist Party USA";
		dto.setName(name);
		dto.setFoundedYear(1919);
		int id = partyDao.insert(dto);

		List<PartyDto> partyList = partyDao.select("select * from PARTY where "
		    + "NAME = :name and FOUNDED_YEAR = :foundedYear", dto);

		assertSame(1, partyList.size());

		dto.setId(id);
		int recordCount = partyDao.delete(dto);
		assertSame(1, recordCount);
	}

}
