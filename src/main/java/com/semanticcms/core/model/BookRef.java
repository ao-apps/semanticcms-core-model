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
import com.aoindustries.net.Path;

/**
 * A book reference contains a domain and a book path.
 */
public class BookRef {

	/**
	 * The domain used for backward compatibility.
	 *
	 * TODO: Search and use this value.
	 */
	public static final String DEFAULT_DOMAIN = "localhost";

	// TODO: Domain self-validating type?
	private final String domain;

	private final Path path;

	public BookRef(String domain, Path path) {
		this.domain = NullArgumentException.checkNotNull(domain, "domain");
		if(domain.isEmpty()) throw new IllegalArgumentException("domain may not be empty");
		this.path = NullArgumentException.checkNotNull(path, "path");
		String pathStr = path.toString();
		if(!pathStr.equals("/") && pathStr.endsWith("/")) throw new IllegalArgumentException("Book path may not end in a slash: " + this.path);
	}

	/**
	 * Gets the non-empty domain of this book.  Two books are considered equal when they
 are in the same domain and have the same path.
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * The path of the book this refers to.
	 * This will be <code>"/"</code> for the root book,
	 * otherwise matches {@link #getPrefix()}.
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * The prefix of the book this refers to, useful for direct path concatenation.
	 * This will be <code>""</code> for the root book <code>"/"</code>,
	 * otherwise matches {@link #getPath()}.
	 */
	public String getPrefix() {
		String bn = path.toString();
		return "/".equals(bn) ? "" : bn;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof BookRef)) return false;
		BookRef other = (BookRef)obj;
		return
			domain.equals(other.domain)
			&& path.equals(other.path)
		;
	}

	@Override
	public int hashCode() {
		return domain.hashCode() * 31 + path.hashCode();
	}

	/**
	 * Ordered by domain, path.
	 */
	public int compareTo(BookRef o) {
		int diff = domain.compareTo(o.domain);
		if(diff != 0) return diff;
		return path.compareTo(o.path);
	}

	@Override
	public String toString() {
		String bn = path.toString();
		return
			new StringBuilder(
				domain.length()
				+ 1 // ':'
				+ bn.length()
			)
			.append(domain)
			.append(':')
			.append(bn)
			.toString();
	}
}
