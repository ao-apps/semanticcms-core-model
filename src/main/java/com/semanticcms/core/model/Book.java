/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.collections.AoCollections;
import com.aoapps.lang.Strings;
import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A book contains pages and is the central mechanism for high-level
 * separation of content.  Each book usually has its own code repository
 * and a book can be added to multiple webapps.
 */
public class Book implements Comparable<Book> {

  private static final String PARAM_PREFIX = "param.";

  private final String name;

  private final String pathPrefix;

  private final File cvsworkDirectory;
  private final Set<ParentRef> unmodifiableParentRefs;
  private final String canonicalBase;
  private final PageRef contentRoot;
  private final Copyright copyright;
  private final Set<Author> unmodifiableAuthors;
  private final String title;
  private final boolean allowRobots;
  private final Map<String, String> unmodifiableParam;

  private static String getProperty(Properties bookProps, Set<Object> usedKeys, String key) {
    usedKeys.add(key);
    return bookProps.getProperty(key);
  }

  public Book(String name, String cvsworkDirectory, boolean allowRobots, Set<ParentRef> parentRefs, Properties bookProps) {
    if (!name.startsWith("/")) {
      throw new IllegalArgumentException("Book name must begin with a slash (/): " + name);
    }

    // Tracks each properties key used, will throw exception if any key exists in the properties file that is not used
    final Set<Object> usedKeys = AoCollections.newHashSet(bookProps.size());

    this.name = name;
    this.pathPrefix = "/".equals(name) ? "" : this.name;
    if (cvsworkDirectory.startsWith("~/")) {
      this.cvsworkDirectory = new File(System.getProperty("user.home"), cvsworkDirectory.substring(2));
    } else {
      this.cvsworkDirectory = new File(cvsworkDirectory);
    }
    this.unmodifiableParentRefs = AoCollections.optimalUnmodifiableSet(parentRefs);
    String copyrightRightsHolder = getProperty(bookProps, usedKeys, "copyright.rightsHolder");
    String copyrightRights = getProperty(bookProps, usedKeys, "copyright.rights");
    String copyrightDateCopyrighted = getProperty(bookProps, usedKeys, "copyright.dateCopyrighted");
    if (
        copyrightRightsHolder != null
            || copyrightRights != null
            || copyrightDateCopyrighted != null
    ) {
      this.copyright = new Copyright(
          copyrightRightsHolder    != null ? copyrightRightsHolder    : "",
          copyrightRights          != null ? copyrightRights          : "",
          copyrightDateCopyrighted != null ? copyrightDateCopyrighted : ""
      );
    } else {
      this.copyright = null;
    }
    Set<Author> authors = new LinkedHashSet<>();
    for (int i = 1; i < Integer.MAX_VALUE; i++) {
      String authorName = getProperty(bookProps, usedKeys, "author." + i + ".name");
      String authorHref = getProperty(bookProps, usedKeys, "author." + i + ".href");
      String authorBook = getProperty(bookProps, usedKeys, "author." + i + ".book");
      String authorPage = getProperty(bookProps, usedKeys, "author." + i + ".page");
      if (authorName == null && authorHref == null && authorBook == null && authorPage == null) {
        break;
      }
      // Default to this book if nothing set
      if (authorPage != null && authorBook == null) {
        authorBook = name;
      }
      // Name required when referencing an author outside this book
      if (authorName == null && authorBook != null && !authorBook.equals(name)) {
        throw new IllegalStateException(name + ": Author name required when author is in a different book: " + authorPage);
      }
      Author newAuthor = new Author(
          authorName,
          authorHref,
          authorBook,
          authorPage
      );
      if (!authors.add(newAuthor)) {
        throw new IllegalStateException(name + ": Duplicate author: " + newAuthor);
      }
    }
    this.unmodifiableAuthors = AoCollections.optimalUnmodifiableSet(authors);
    this.title = getProperty(bookProps, usedKeys, "title");
    this.allowRobots = allowRobots;
    Map<String, String> newParam = new LinkedHashMap<>();
    @SuppressWarnings("unchecked")
    Enumeration<String> propertyNames = (Enumeration) bookProps.propertyNames();
    while (propertyNames.hasMoreElements()) {
      String propertyName = propertyNames.nextElement();
      if (propertyName.startsWith(PARAM_PREFIX)) {
        newParam.put(
            propertyName.substring(PARAM_PREFIX.length()),
            getProperty(bookProps, usedKeys, propertyName)
        );
      }
    }
    this.unmodifiableParam = AoCollections.optimalUnmodifiableMap(newParam);
    String cb = Strings.nullIfEmpty(getProperty(bookProps, usedKeys, "canonicalBase"));
    while (cb != null && cb.endsWith("/")) {
      cb = Strings.nullIfEmpty(cb.substring(0, cb.length() - 1));
    }
    this.canonicalBase = cb;
    // Create the page refs once other aspects of the book have already been setup, since we'll be leaking "this"
    this.contentRoot = new PageRef(this, getProperty(bookProps, usedKeys, "content.root"));

    // Make sure all keys used
    Set<Object> unusedKeys = new HashSet<>();
    for (Object key : bookProps.keySet()) {
      if (!usedKeys.contains(key)) {
        unusedKeys.add(key);
      }
    }
    if (!unusedKeys.isEmpty()) {
      throw new IllegalStateException(name + ": Unused keys: " + unusedKeys);
    }
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Book)) {
      return false;
    }
    Book other = (Book) obj;
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public int compareTo(Book o) {
    return name.compareTo(o.name);
  }

  public String getName() {
    return name;
  }

  /**
   * Gets the path prefix for all pages in this book.
   * This will be an empty string for the root book (/).
   */
  public String getPathPrefix() {
    return pathPrefix;
  }

  public File getCvsworkDirectory() {
    return cvsworkDirectory;
  }

  /**
   * Gets the parent pages for this book in the context of the current overall
   * content.
   */
  @SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
  public Set<ParentRef> getParentRefs() {
    return unmodifiableParentRefs;
  }

  /**
   * Gets the configured canonicalBase for this book, or {@code null} if not
   * configured.  Any trailing slash (/) has been stripped from the canonicalBase
   * so can directly concatenate canonicalBase + path
   */
  public String getCanonicalBase() {
    return canonicalBase;
  }

  /**
   * Gets the content root for the book.
   */
  public PageRef getContentRoot() {
    return contentRoot;
  }

  /**
   * Gets the copyright for the book or {@code null} if none declared.
   * As book copyrights are not inherited, all copyright fields will be non-null.
   */
  public Copyright getCopyright() {
    assert copyright == null || !copyright.isEmpty();
    return copyright;
  }

  /**
   * Gets the authors of the book.  Any page without more specific authors
   * in itself or a parent (within the book) will use these authors.
   */
  @SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
  public Set<Author> getAuthors() {
    return unmodifiableAuthors;
  }

  public String getTitle() {
    return title;
  }

  /**
   * Gets the allowRobots setting of the book.  Any page with an "auto"
   * setting and no parents within the book will use this setting.
   */
  public boolean getAllowRobots() {
    return allowRobots;
  }

  @SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
  public Map<String, String> getParam() {
    return unmodifiableParam;
  }
}
