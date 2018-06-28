/*
 * Copyright (c) 2015 FCCI Insurance Group, All rights reserved.
 */
package com.rothsmith.genericdao;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * DTO generated from SQL statement.
 *
 * select ID , NAME , FOUNDED_YEAR , END_YEAR from TEST.PARTY .
 *
 * @author drothauser
 */
public final class PartyDto implements Serializable {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -4463657510560773907L;

	/**
	 * Member variable represents database field ID.
	 */
	private Integer id;

	/**
	 * Member variable represents database field NAME.
	 */
	private String name;

	/**
	 * Member variable represents database field FOUNDED_YEAR.
	 */
	private Integer foundedYear;

	/**
	 * Member variable represents database field END_YEAR.
	 */
	private Integer endYear;

	/**
	 * Default constructor.
	 */
	public PartyDto() {
		// Default constructor
	}

	/**
	 * Private constructor used by newInstance factory method to create a copy
	 * of another PartyDto instance.
	 *
	 * @param partyDto
	 *            an instance of another {@link PartyDto} object.
	 */
	private PartyDto(final PartyDto partyDto) {

		id = partyDto.getId();
		name = partyDto.getName();
		foundedYear = partyDto.getFoundedYear();
		endYear = partyDto.getEndYear();

	}

	/**
	 * Factory method to return a copy of the given PartyDto instance.
	 *
	 * @param partyDto
	 *            an instance of another {@link PartyDto} object.
	 *
	 * @return a copy of partyDto.
	 */
	public static PartyDto newInstance(final PartyDto partyDto) {
		return new PartyDto(partyDto);
	}

	/**
	 * Accessor for id.
	 *
	 * @return id Integer to get
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Mutator for id.
	 *
	 * @param varId
	 *            Integer to set
	 */
	public void setId(final Integer varId) {
		id = varId;
	}

	/**
	 * Accessor for name.
	 *
	 * @return name String to get
	 */
	public String getName() {
		return name;
	}

	/**
	 * Mutator for name.
	 *
	 * @param varName
	 *            String to set
	 */
	public void setName(final String varName) {
		name = varName;
	}

	/**
	 * Accessor for foundedYear.
	 *
	 * @return foundedYear Integer to get
	 */
	public Integer getFoundedYear() {
		return foundedYear;
	}

	/**
	 * Mutator for foundedYear.
	 *
	 * @param varFoundedYear
	 *            Integer to set
	 */
	public void setFoundedYear(final Integer varFoundedYear) {
		foundedYear = varFoundedYear;
	}

	/**
	 * Accessor for endYear.
	 *
	 * @return endYear Integer to get
	 */
	public Integer getEndYear() {
		return endYear;
	}

	/**
	 * Mutator for endYear.
	 *
	 * @param varEndYear
	 *            Integer to set
	 */
	public void setEndYear(final Integer varEndYear) {
		endYear = varEndYear;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
		    ToStringStyle.MULTI_LINE_STYLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
