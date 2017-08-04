/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2014, 2015, 2016, 2017  AO Industries, Inc.
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
 * along with semanticcms-core-model.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.semanticcms.core.model;

import com.aoindustries.lang.NullArgumentException;

/**
 * A book reference contains a domain and a book name.
 */
public class BookRef {

	private final String domain;

	private final String name;

	public BookRef(String domain, String name) {
		this.domain = NullArgumentException.checkNotNull(domain, "domain");
		if(domain.isEmpty()) throw new IllegalArgumentException("domain may not be empty");
		this.name = NullArgumentException.checkNotNull(name, "name");
		if(!name.startsWith("/")) throw new IllegalArgumentException("Book name does not begin with a slash: " + this.name);
	}

	/**
	 * Gets the non-empty domain of this book.  Two books are considered equal when they
	 * are in the same domain and have the same name.
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * The name of the book this refers to.
	 * This will always begin with a slash (/).
	 */
	public String getName() {
		return name;
	}

	/**
	 * The prefix of the book this refers to.
	 * This will be <code>""</code> for the root book <code>"/"</code>.
	 */
	public String getPrefix() {
		String bn = name;
		return "/".equals(bn) ? "" : bn;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof BookRef)) return false;
		BookRef other = (BookRef)obj;
		return
			domain.equals(other.domain)
			&& name.equals(other.name)
		;
	}

	@Override
	public int hashCode() {
		return domain.hashCode() * 31 + name.hashCode();
	}

	/**
	 * Ordered by domain, name.
	 */
	public int compareTo(BookRef o) {
		int diff = domain.compareTo(o.domain);
		if(diff != 0) return diff;
		return name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return
			new StringBuilder(
				domain.length()
				+ 1 // ':'
				+ name.length()
			)
			.append(domain)
			.append(':')
			.append(name)
			.toString();
	}
}
