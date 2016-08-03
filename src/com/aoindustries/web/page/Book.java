/*
 * ao-web-page - Java API for modeling web page content and relationships.
 * Copyright (C) 2014, 2015, 2016  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-web-page.
 *
 * ao-web-page is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-web-page is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-web-page.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.web.page;

import com.aoindustries.util.AoCollections;
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
	private final Set<PageRef> unmodifiableParentPages;
	private final PageRef contentRoot;
	private final Copyright copyright;
	private final Set<Author> unmodifiableAuthors;
	private final String title;
	private final String pageHeader;
	private final int navigationFrameWidth;
	private final String logoSrc;
	private final int logoWidth;
	private final int logoHeight;
	private final String logoAlt;
	private final Map<String,String> unmodifiableParam;

	private static String getProperty(Properties bookProps, Set<Object> usedKeys, String key) {
		usedKeys.add(key);
		return bookProps.getProperty(key);
	}

	public Book(String name, String cvsworkDirectory, Set<PageRef> parentPages, Properties bookProps) {
		if(!name.startsWith("/")) throw new IllegalArgumentException("Book name must begin with a slash (/): " + name);

		// Tracks each properties key used, will throw exception if any key exists in the properties file that is not used
		Set<Object> usedKeys = new HashSet<>(bookProps.size() * 4/3 + 1);

		this.name = name;
		this.pathPrefix = "/".equals(name) ? "" : name;
		if(cvsworkDirectory.startsWith("~/")) {
			this.cvsworkDirectory = new File(System.getProperty("user.home"), cvsworkDirectory.substring(2));
		} else {
			this.cvsworkDirectory = new File(cvsworkDirectory);
		}
		this.unmodifiableParentPages = AoCollections.optimalUnmodifiableSet(parentPages);
		String copyrightRightsHolder = getProperty(bookProps, usedKeys, "copyright.rightsHolder");
		String copyrightRights = getProperty(bookProps, usedKeys, "copyright.rights");
		String copyrightDateCopyrighted = getProperty(bookProps, usedKeys, "copyright.dateCopyrighted");
		if(
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
		for(int i=1; i<Integer.MAX_VALUE; i++) {
			String authorName = getProperty(bookProps, usedKeys, "author." + i + ".name");
			String authorHref = getProperty(bookProps, usedKeys, "author." + i + ".href");
			String authorBook = getProperty(bookProps, usedKeys, "author." + i + ".book");
			String authorPage = getProperty(bookProps, usedKeys, "author." + i + ".page");
			if(authorName==null && authorHref==null && authorBook==null && authorPage==null) break;
			// Default to this book if nothing set
			if(authorPage != null && authorBook == null) authorBook = name;
			// Name required when referencing an author outside this book
			if(authorName == null && authorBook != null && !authorBook.equals(name)) {
				throw new IllegalStateException(name + ": Author name required when author is in a different book: " + authorPage);
			}
			Author newAuthor = new Author(
				authorName,
				authorHref,
				authorBook,
				authorPage
			);
			if(!authors.add(newAuthor)) throw new IllegalStateException(name + ": Duplicate author: " + newAuthor);
		}
		this.unmodifiableAuthors = AoCollections.optimalUnmodifiableSet(authors);
		this.title = getProperty(bookProps, usedKeys, "title");
		this.pageHeader = getProperty(bookProps, usedKeys, "pageHeader");
		this.navigationFrameWidth = Integer.parseInt(getProperty(bookProps, usedKeys, "navigationFrameWidth"));
		this.logoSrc = getProperty(bookProps, usedKeys, "logoSrc");
		this.logoWidth = Integer.parseInt(getProperty(bookProps, usedKeys, "logoWidth"));
		this.logoHeight = Integer.parseInt(getProperty(bookProps, usedKeys, "logoHeight"));
		this.logoAlt = getProperty(bookProps, usedKeys, "logoAlt");
		Map<String,String> newParam = new LinkedHashMap<>();
		@SuppressWarnings("unchecked")
		Enumeration<String> propertyNames = (Enumeration)bookProps.propertyNames();
		while(propertyNames.hasMoreElements()) {
			String propertyName = propertyNames.nextElement();
			if(propertyName.startsWith(PARAM_PREFIX)) {
				newParam.put(
					propertyName.substring(PARAM_PREFIX.length()),
					getProperty(bookProps, usedKeys, propertyName)
				);
			}
		}
		this.unmodifiableParam = AoCollections.optimalUnmodifiableMap(newParam);
		// Create the page refs once other aspects of the book have already been setup, since we'll be leaking "this"
		this.contentRoot = new PageRef(this, getProperty(bookProps, usedKeys, "content.root"));

		// Make sure all keys used
		Set<Object> unusedKeys = new HashSet<>();
		for(Object key : bookProps.keySet()) {
			if(!usedKeys.contains(key)) unusedKeys.add(key);
		}
		if(!unusedKeys.isEmpty()) throw new IllegalStateException(name + ": Unused keys: " + unusedKeys);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Book)) return false;
		Book other = (Book)obj;
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
	public Set<PageRef> getParentPages() {
		return unmodifiableParentPages;
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
		return copyright;
	}

	/**
	 * Gets the authors of the book.  Any page without more specific authors
	 * in itself or a parent (within the book) will use these authors.
	 */
	public Set<Author> getAuthors() {
		return unmodifiableAuthors;
	}

	public String getTitle() {
		return title;
	}

	public String getPageHeader() {
		return pageHeader;
	}

	public int getNavigationFrameWidth() {
		return navigationFrameWidth;
	}

	public String getLogoSrc() {
		return logoSrc;
	}

	public int getLogoWidth() {
		return logoWidth;
	}

	public int getLogoHeight() {
		return logoHeight;
	}

	public String getLogoAlt() {
		return logoAlt;
	}

	public Map<String,String> getParam() {
		return unmodifiableParam;
	}
}
