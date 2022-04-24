/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2016, 2017, 2019, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.net.DomainName;
import com.aoapps.net.Path;
import java.util.Objects;

/**
 * An author of a book, a page, or a set of pages.
 *
 * TODO: Possible Google+ integration: http://www.vervesearch.com/blog/how-to-implement-the-relauthor-tag-a-step-by-step-guide/
 * TODO: And also: https://yoast.com/push-rel-author-head/
 * TODO: This seems killed by Google, any value in it still?
 */
public class Author {

  private final String name;
  private final String href;
  private final DomainName domain;
  private final Path book;
  private final Path page;

  /**
   * Either href may be provided, or domain:/book/page may be provided, but not both.
   * When page provided, domain and book are also required.
   * When name not provided, and page provided, uses page title as the author name.
   */
  public Author(String name, String href, DomainName domain, Path book, Path page) {
    // No empty strings
    if (name != null && name.isEmpty()) {
      throw new IllegalArgumentException("empty name not allowed");
    }
    if (href != null && href.isEmpty()) {
      throw new IllegalArgumentException("empty href not allowed");
    }
    //if (domain != null && domain.isEmpty()) {
    //  throw new IllegalArgumentException("empty domain not allowed");
    //}
    //if (book != null && book.isEmpty()) {
    //  throw new IllegalArgumentException("empty book not allowed");
    //}
    //if (page != null && page.isEmpty()) {
    //  throw new IllegalArgumentException("empty page not allowed");
    //}
    // Other checks
    if (href != null) {
      if (domain != null) {
        throw new IllegalArgumentException("domain may not be provided when href provided");
      }
      if (book != null) {
        throw new IllegalArgumentException("book may not be provided when href provided");
      }
      if (page != null) {
        throw new IllegalArgumentException("page may not be provided when href provided");
      }
    } else {
      if (name == null) {
        if (page == null) {
          throw new IllegalArgumentException("empty author, at least one of name, href, or page required");
        }
      }
      if (page != null) {
        if (domain == null) {
          throw new IllegalArgumentException("page provided without domain");
        }
        if (book == null) {
          throw new IllegalArgumentException("page provided without book");
        }
      } else {
        if (domain != null) {
          throw new IllegalArgumentException("domain provided without page");
        }
        if (book != null) {
          throw new IllegalArgumentException("book provided without page");
        }
      }
    }
    this.name = name;
    this.href = href;
    this.domain = domain;
    this.book = book;
    this.page = page;
  }

  @Override
  public String toString() {
    if (name != null) {
      return name;
    }
    if (href != null) {
      return href;
    }
    assert domain != null;
    assert book != null;
    assert page != null;
    if (book.equals(Path.ROOT)) {
      return domain.toString() + ':' + page.toString();
    } else {
      return domain.toString() + ':' + book.toString() + page.toString();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Author)) {
      return false;
    }
    Author o = (Author) obj;
    return
        Objects.equals(name, o.name)
            && Objects.equals(href, o.href)
            && Objects.equals(domain, o.domain)
            && Objects.equals(book, o.book)
            && Objects.equals(page, o.page)
    ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        href,
        domain,
        book,
        page
    );
  }

  public String getName() {
    return name;
  }

  public String getHref() {
    return href;
  }

  public DomainName getDomain() {
    return domain;
  }

  public Path getBook() {
    return book;
  }

  public Path getPage() {
    return page;
  }
}
