/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2016, 2017, 2019, 2020, 2021  AO Industries, Inc.
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
import com.aoapps.lang.xml.XmlUtils;
import com.aoapps.net.URIDecoder;
import com.aoapps.net.URIEncoder;
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
		if(!XmlUtils.isValidName(id)) throw new IllegalArgumentException("Invalid id: " + id);
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

	private volatile String servletPath;

	/**
	 * Gets the combination of the book, the path, and element anchor that refers to the
	 * element resource within the web application.
	 * <p>
	 * The element anchor is not URL-encoded - Unicode characters are verbatim.
	 * </p>
	 * 
	 * @see #appendServletPath(java.lang.Appendable) 
	 */
	@SuppressWarnings("ReplaceStringBufferByString")
	public String getServletPath() {
		String sp = servletPath;
		if(sp == null) {
			String page = pageRef.getServletPath();
			// TODO: encodeIRIComponent to do this in one shot?
			String idIri = URIDecoder.decodeURI(URIEncoder.encodeURIComponent(id));
			int sbLen =
				page.length()
				+ 1 // '#'
				+ idIri.length();
			StringBuilder sb = new StringBuilder(sbLen)
				.append(page)
				.append('#')
				.append(idIri);
			assert sb.length() == sbLen;
			servletPath = sp = sb.toString();
		}
		return sp;
	}

	/**
	 * Appends the combination of the book, the path, and element anchor that refers to the
	 * element resource within the web application.
	 * <p>
	 * The element anchor is not URL-encoded - Unicode characters are verbatim.
	 * </p>
	 *
	 * @see #getServletPath() 
	 */
	// TODO: Encoder variant, other classes, too, see which uses of getServletPath can be streamed
	public void appendServletPath(Appendable out) throws IOException {
		pageRef.appendServletPath(out);
		out.append('#');
		// TODO: encodeIRIComponent to do this in one shot?
		URIDecoder.decodeURI(
			URIEncoder.encodeURIComponent(id),
			out
		);
	}

	/**
	 * @see  #getServletPath()
	 */
	@Override
	public String toString() {
		return getServletPath();
	}
}
