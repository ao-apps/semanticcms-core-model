/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2016, 2022  AO Industries, Inc.
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

/**
 * Something that has a reference to a page.
 */
public interface PageReferrer extends Comparable<PageReferrer> {

	/**
	 * Gets the page reference.
	 */
	PageRef getPageRef();

	/**
	 * @see  PageRef#compareTo(com.semanticcms.core.model.PageRef)
	 *
	 * Java 1.8: Implement this as a default method
	 */
	@Override
	int compareTo(PageReferrer o);
	//	return getPageRef().compareTo(o.getPageRef());
	//}
}
