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
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
	private final List<PageRef> unmodifiableParentPages;
	private final PageRef contentRoot;
	private final String title;
	private final String pageHeader;
	private final int navigationFrameWidth;
	private final String logoSrc;
	private final int logoWidth;
	private final int logoHeight;
	private final String logoAlt;
	private final Map<String,String> unmodifiableParam;

	public Book(String name, String cvsworkDirectory, List<PageRef> parentPages, Properties properties) {
		if(!name.startsWith("/")) throw new IllegalArgumentException("Book name must begin with a slash (/): " + name);
		this.name = name;
		this.pathPrefix = "/".equals(name) ? "" : name;
		if(cvsworkDirectory.startsWith("~/")) {
			this.cvsworkDirectory = new File(System.getProperty("user.home"), cvsworkDirectory.substring(2));
		} else {
			this.cvsworkDirectory = new File(cvsworkDirectory);
		}
		this.unmodifiableParentPages = AoCollections.optimalUnmodifiableList(parentPages);
		this.title = properties.getProperty("title");
		this.pageHeader = properties.getProperty("pageHeader");
		this.navigationFrameWidth = Integer.parseInt(properties.getProperty("navigationFrameWidth"));
		this.logoSrc = properties.getProperty("logoSrc");
		this.logoWidth = Integer.parseInt(properties.getProperty("logoWidth"));
		this.logoHeight = Integer.parseInt(properties.getProperty("logoHeight"));
		this.logoAlt = properties.getProperty("logoAlt");
		Map<String,String> newParam = new LinkedHashMap<>(properties.size() * 4/3 + 1);
		@SuppressWarnings("unchecked")
		Enumeration<String> propertyNames = (Enumeration)properties.propertyNames();
		while(propertyNames.hasMoreElements()) {
			String propertyName = propertyNames.nextElement();
			if(propertyName.startsWith(PARAM_PREFIX)) {
				newParam.put(
					propertyName.substring(PARAM_PREFIX.length()),
					properties.getProperty(propertyName)
				);
			}
		}
		this.unmodifiableParam = Collections.unmodifiableMap(newParam);
		// Create the page refs once other aspects of the book have already been setup, since we'll be leaking "this"
		this.contentRoot = new PageRef(this, properties.getProperty("content.root"));
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
	public List<PageRef> getParentPages() {
		return unmodifiableParentPages;
	}

	/**
	 * Gets the content root for the book.
	 */
	public PageRef getContentRoot() {
		return contentRoot;
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
