/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2013, 2014, 2015, 2016, 2017  AO Industries, Inc.
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
import java.io.IOException;

/**
 * A page reference contains a domain, a book, and a path to a page or directory.
 * Any path to a directory must end with a slash (/).
 *
 * TODO: Separate class for ResourceRef?
 *
 * // TODO: Support parameters to a page, child, link, ...
 *          Parameters provided in path/page?, param.* attributes, and nested tags - matching/extending AO taglib.
 */
public class PageRef implements PageReferrer {

	private final BookRef bookRef;

	private final String path;

	public PageRef(BookRef bookRef, String path) {
		this.bookRef = NullArgumentException.checkNotNull(bookRef, "bookRef");
		this.path = NullArgumentException.checkNotNull(path, "path");
		if(!this.path.startsWith("/")) throw new IllegalArgumentException("Path does not begin with a slash: " + this.path);
	}

	/**
	 * A PageRef is its own referrer.
	 */
	@Override
	public PageRef getPageRef() {
		return this;
	}

	public BookRef getBookRef() {
		return bookRef;
	}

	/**
	 * The book-relative path to the page, always starting with a slash (/).
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 * 
	 * @return  this object if path unchanged or a new object representing the new path
	 */
	public PageRef setPath(String newPath) {
		if(newPath.equals(path)) {
			return this;
		} else {
			return new PageRef(bookRef, newPath);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof PageRef)) return false;
		PageRef other = (PageRef)obj;
		return
			bookRef.equals(other.bookRef)
			&& path.equals(other.path)
		;
	}

	private int hash;

	@Override
	public int hashCode() {
		int h = hash;
		if(h == 0) {
			h = bookRef.hashCode();
			h = h * 31 + path.hashCode();
			hash = h;
		}
		return h;
	}

	/**
	 * Ordered by bookRef, servletPath.
	 *
	 * @see  BookRef#compareTo(com.semanticcms.core.model.BookRef)
	 * @see  #getServletPath()
	 */
	public int compareTo(PageRef o) {
		int diff = bookRef.compareTo(o.bookRef);
		if(diff != 0) return diff;
		return getServletPath().compareTo(o.getServletPath());
	}

	@Override
	public int compareTo(PageReferrer o) {
		return compareTo(o.getPageRef());
	}

	// Would it be faster to just create this in the constructor and avoid volatile here? (Micro optimization here)
	private volatile String servletPath;

	/**
	 * Gets the combination of the book and the path that refers to the
	 * page resource within the web application.
	 * 
	 * @see #appendServletPath(java.lang.Appendable) 
	 */
	public String getServletPath() {
		String sp = servletPath;
		if(sp == null) {
			String bn = bookRef.getName();
			sp = ("/".equals(bn)) ? path : (bn + path);
			servletPath = sp;
		}
		return sp;
	}

	/**
	 * Appends the combination of the book and the path that refers to the
	 * page resource within the web application.
	 *
	 * @see #getServletPath() 
	 */
	public void appendServletPath(Appendable out) throws IOException {
		String bn = bookRef.getName();
		if(!"/".equals(bn)) out.append(bn);
		out.append(path);
	}

	@Override
	public String toString() {
		String sp = getServletPath();
		String domain = bookRef.getDomain();
		return
			new StringBuilder(
				domain.length()
				+ 1 // ':'
				+ sp.length()
			)
			.append(domain)
			.append(':')
			.append(sp)
			.toString();
	}
}
