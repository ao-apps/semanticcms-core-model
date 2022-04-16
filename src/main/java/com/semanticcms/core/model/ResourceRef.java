/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2017, 2021, 2022  AO Industries, Inc.
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
import com.aoapps.net.Path;

/**
 * A reference to a non-page resource within a book.
 * A resource reference contains a domain, a book, and a path to a file or directory.
 * Any path to a directory must end with a slash (/).
 *
 * @see  PageRef  to refer to a page
 */
public class ResourceRef implements Comparable<ResourceRef> {

	private final BookRef bookRef;

	private final Path path;

	public ResourceRef(BookRef bookRef, Path path) {
		this.bookRef = NullArgumentException.checkNotNull(bookRef, "bookRef");
		this.path = NullArgumentException.checkNotNull(path, "path");
	}

	public BookRef getBookRef() {
		return bookRef;
	}

	/**
	 * The book-relative path to the resource, always starting with a slash (/).
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 * 
	 * @return  this object if path unchanged or a new object representing the new path
	 */
	public ResourceRef setPath(Path newPath) {
		if(newPath.equals(path)) {
			return this;
		} else {
			return new ResourceRef(bookRef, newPath);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof ResourceRef)) return false;
		ResourceRef other = (ResourceRef)obj;
		return
			bookRef.equals(other.bookRef)
			&& path.equals(other.path)
		;
	}

	@Override
	public int hashCode() {
		return bookRef.hashCode() * 31 + path.hashCode();
	}

	/**
	 * Ordered by bookRef, path.
	 *
	 * @see  BookRef#compareTo(com.semanticcms.core.model.BookRef)
	 * @see  #getPath()
	 */
	@Override
	public int compareTo(ResourceRef o) {
		int diff = bookRef.compareTo(o.bookRef);
		if(diff != 0) return diff;
		return getPath().compareTo(o.getPath());
	}

	@Override
	public String toString() {
		String domain = bookRef.getDomain().toString();
		String prefix = bookRef.getPrefix();
		String pathStr = path.toString();
		return
			new StringBuilder(
				domain.length()
				+ 1 // ':'
				+ prefix.length()
				+ pathStr.length()
			)
			.append(domain)
			.append(':')
			.append(prefix)
			.append(pathStr)
			.toString();
	}
}
