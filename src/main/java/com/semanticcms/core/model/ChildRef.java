/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2016, 2021  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of semanticcms-core-model.
 *
 * semanticcms-core-model is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * semanticcms-core-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with semanticcms-core-model.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.semanticcms.core.model;

import com.aoapps.lang.NullArgumentException;

/**
 * A child reference.
 */
public class ChildRef implements PageReferrer {

	private final PageRef pageRef;

	public ChildRef(PageRef pageRef) {
		this.pageRef = NullArgumentException.checkNotNull(pageRef, "pageRef");
	}

	/**
	 * The reference to the child page.
	 */
	@Override
	public PageRef getPageRef() {
		return pageRef;
	}

	/**
	 * Equality is determined by pageRef only.
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof ChildRef)) return false;
		ChildRef other = (ChildRef)obj;
		return pageRef.equals(other.pageRef);
	}

	/**
	 * Hash is based on pageRef only.
	 */
	@Override
	public int hashCode() {
		return pageRef.hashCode();
	}

	/**
	 * Orders by pageRef only.
	 * 
	 * @see  #getPageRef()
	 */
	@Override
	public int compareTo(PageReferrer o) {
		return getPageRef().compareTo(o.getPageRef());
	}

	@Override
	public String toString() {
		return pageRef.toString();
	}
}
