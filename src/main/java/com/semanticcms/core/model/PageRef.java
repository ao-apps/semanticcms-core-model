/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2013, 2014, 2015, 2016, 2017, 2021, 2022  AO Industries, Inc.
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
 * A page reference contains a domain, a book, and a path to a page.
 *
 * TODO: Support parameters to a page, child, link, ...
 *       Parameters provided in path/page?, param.* attributes, and nested tags - matching/extending AO taglib.
 *
 * @see  ResourceRef  to refer to a non-page resource
 */
public class PageRef implements PageReferrer {

  private final BookRef bookRef;

  private final Path path;

  public PageRef(BookRef bookRef, Path path) {
    this.bookRef = NullArgumentException.checkNotNull(bookRef, "bookRef");
    this.path = NullArgumentException.checkNotNull(path, "path");
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
   * <p>
   * The path is to the external, abstract name of the page, which can be independent
   * of the page implementation.  For example, the page at path <code>/example/page</code>
   * might be implemented in JSP by a file at <code>/example/page.jsp</code>, but its
   * path remains <code>/example/page</code>.  This allows the implementation to be
   * changed on a per-page basis, such as switching from JSP to JSPX, without requiring
   * updates to references to the page.
   * </p>
   */
  public Path getPath() {
    return path;
  }

  /**
   * Sets the path.
   *
   * @return  this object if path unchanged or a new object representing the new path
   */
  public PageRef setPath(Path newPath) {
    if (newPath.equals(path)) {
      return this;
    } else {
      return new PageRef(bookRef, newPath);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PageRef)) {
      return false;
    }
    PageRef other = (PageRef) obj;
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
  public int compareTo(PageRef o) {
    int diff = bookRef.compareTo(o.bookRef);
    if (diff != 0) {
      return diff;
    }
    return getPath().compareTo(o.getPath());
  }

  @Override
  public int compareTo(PageReferrer o) {
    return compareTo(o.getPageRef());
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
