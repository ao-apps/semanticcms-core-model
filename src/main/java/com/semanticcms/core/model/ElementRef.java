/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2016, 2017, 2019  AO Industries, Inc.
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
import com.aoindustries.xml.XmlUtils;

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
		if(!XmlUtils.isValidId(id)) throw new IllegalArgumentException("Invalid id: " + id);
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

	@Override
	public int hashCode() {
		return pageRef.hashCode() * 31 + id.hashCode();
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

	/**
	 * Gets the combination of the domain, book, the path, and element anchor that refers to the
	 * element resource within the web application.
	 * <p>
	 * The element anchor is not URL-encoded - Unicode characters are verbatim.
	 * </p>
	 */
	@Override
	public String toString() {
		BookRef bookRef = pageRef.getBookRef();
		String domain = bookRef.getDomain().toString();
		String prefix = bookRef.getPrefix();
		String path = pageRef.getPath().toString();
		int sbLen =
			domain.length()
			+ 1 // ':'
			+ prefix.length()
			+ path.length()
			+ 1 // '#'
			+ id.length();
		StringBuilder sb = new StringBuilder(sbLen)
			.append(domain)
			.append(':')
			.append(prefix)
			.append(path)
			.append('#')
			.append(id);
		assert sb.length() == sbLen;
		return sb.toString();
	}
}
