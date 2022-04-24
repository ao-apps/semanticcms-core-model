/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2016, 2021, 2022  AO Industries, Inc.
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
 * A parent reference also includes an optional shortTitle for when this page
 * is listed within the context of this specific parent.
 */
public class ParentRef implements PageReferrer {

  private final PageRef pageRef;

  private final String shortTitle;

  public ParentRef(PageRef pageRef, String shortTitle) {
    this.pageRef = NullArgumentException.checkNotNull(pageRef, "pageRef");
    this.shortTitle = shortTitle;
  }

  /**
   * The reference to the parent page.
   */
  @Override
  public PageRef getPageRef() {
    return pageRef;
  }

  /**
   * A short title is used when the context of a page is well established, such as when
   * showing a path to the current location in the site.
   *
   * @return  the short page title for the page when in the context of this parent.
   *
   * @see  Page#getShortTitle()  When the shortTitle of this ParentRef is null,
   *                             the shortTitle of the page itself is used.
   */
  public String getShortTitle() {
    return shortTitle;
  }

  /**
   * Equality is determined by pageRef only, short title not a factor.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ParentRef)) {
      return false;
    }
    ParentRef other = (ParentRef) obj;
    return pageRef.equals(other.pageRef);
  }

  /**
   * Hash is based on pageRef only, short title is not a factor.
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
    if (shortTitle == null) {
      return pageRef.toString();
    } else {
      return shortTitle + " -> " + pageRef.toString();
    }
  }
}
