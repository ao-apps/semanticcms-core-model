/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2016, 2017  AO Industries, Inc.
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
 * An element reference contains a book, a path, and an element id.
 * Any path to a directory must end with a slash (/).
 * 
 * // TODO: Support parameters to a page, child, link, ...
 *          Parameters provided in path/page?, param.* attributes, and nested tags - matching/extending AO taglib.
 */
public class ElementRef implements Comparable<ElementRef> {

	private final PageRef pageRef;

	private final String id;

	public ElementRef(PageRef pageRef, String id) {
		this.pageRef = NullArgumentException.checkNotNull(pageRef, "pageRef");
		this.id = NullArgumentException.checkNotNull(id, "id");
		if(!Element.isValidId(id)) throw new IllegalArgumentException("Invalid id: " + id);
	}

	/**
	 * The reference to the page this element is part of.
	 */
	public PageRef getPageRef() {
		return pageRef;
	}

	/**
	 * The id of the element within the page.
	 */
	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof ElementRef)) return false;
		ElementRef other = (ElementRef)obj;
		return
			pageRef.equals(other.pageRef)
			&& id.equals(other.id)
		;
	}

	private int hash;

	@Override
	public int hashCode() {
		int h = hash;
		if(h == 0) {
			h = pageRef.hashCode();
			h = h * 31 + id.hashCode();
			hash = h;
		}
		return h;
	}

	/**
	 * Orders by page then id.
	 * 
	 * @see  PageRef#compareTo(com.semanticcms.core.model.PageRef)
	 */
	@Override
	public int compareTo(ElementRef o) {
		int diff = pageRef.compareTo(o.pageRef);
		if(diff != 0) return diff;
		return id.compareTo(o.id);
	}

	private volatile String servletPath;

	/**
	 * Gets the combination of the book, the path, and element anchor that refers to the
	 * element resource within the web application.
	 * 
	 * @see #appendServletPath(java.lang.Appendable) 
	 */
	public String getServletPath() {
		String sp = servletPath;
		if(sp == null) {
			// TODO: Should "id" allow all characters and automatically encode them?  Would require knowledge of response encoding here.
			servletPath = sp = pageRef.getServletPath() + '#' + id;
		}
		return sp;
	}

	/**
	 * Appends the combination of the book, the path, and element anchor that refers to the
	 * element resource within the web application.
	 *
	 * @see #getServletPath() 
	 */
	public void appendServletPath(Appendable out) throws IOException {
		pageRef.appendServletPath(out);
		out.append('#');
		out.append(id);
	}

	@Override
	public String toString() {
		String sp = getServletPath();
		String domain = pageRef.getBookRef().getDomain();
		int domainLen = domain.length();
		if(domainLen == 0) {
			return sp;
		} else {
			return
				new StringBuilder(
					domainLen
					+ 1 // ':'
					+ sp.length()
				)
				.append(domain)
				.append(':')
				.append(sp)
				.toString();
		}
	}
}
