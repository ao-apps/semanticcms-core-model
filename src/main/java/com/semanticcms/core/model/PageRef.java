/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2013, 2014, 2015, 2016, 2017  AO Industries, Inc.
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A page reference contains a book, a path, and an optional domain to a page or directory.
 * Any path to a directory must end with a slash (/).
 * 
 * // TODO: Support parameters to a page, child, link, ...
 *          Parameters provided in path/page?, param.* attributes, and nested tags - matching/extending AO taglib.
 */
public class PageRef implements PageReferrer {

	/**
	 * @see  String#intern()  always interned
	 */
	private final String domain;

	/**
	 * @see  String#intern()  always interned
	 */
	private final String bookName;

	/**
	 * @see  String#intern()  always interned
	 */
	private final String path;
	
	/**
	 * TODO: Consider not having reference to book here, it simplifies the book loading order issue
	 *       (see BooksContextListener), and rules about allow missing books can be handled in more
	 *       appropriate places.
	 */
	private final Book book;

	private PageRef(String domain, String bookName, String path, Book book) {
		assert domain == domain.intern();
		assert book != null || bookName == bookName.intern();
		this.domain = domain;
		this.bookName = book != null ? book.getName() : bookName;
		this.path = path.intern();
		if(!path.startsWith("/")) throw new IllegalArgumentException("Path does not begin with a slash: " + path);
		this.book = book;
	}

	public PageRef(String domain, String bookName, String path) {
		this(
			NullArgumentException.checkNotNull(domain, "domain").intern(),
			NullArgumentException.checkNotNull(bookName, "bookName").intern(),
			NullArgumentException.checkNotNull(path, "path"),
			null
		);
	}

	/**
	 * Uses default domain of {@code ""}.
	 *
	 * @see  #PageRef(java.lang.String, java.lang.String, java.lang.String)
	 *
	 * @deprecated  Please provide domain
	 */
	@Deprecated
	public PageRef(String bookName, String path) {
		this(
			"", // All string literals are already interned
			NullArgumentException.checkNotNull(bookName, "bookName").intern(),
			NullArgumentException.checkNotNull(path, "path"),
			null
		);
	}

	public PageRef(String domain, Book book, String path) {
		this(
			NullArgumentException.checkNotNull(domain, "domain").intern(),
			null,
			NullArgumentException.checkNotNull(path, "path"),
			NullArgumentException.checkNotNull(book, "book")
		);
	}

	/**
	 * Uses default domain of {@code ""}.
	 *
	 * @see  #PageRef(java.lang.String, com.semanticcms.core.model.Book, java.lang.String)
	 *
	 * @deprecated  Please provide domain
	 */
	@Deprecated
	public PageRef(Book book, String path) {
		this(
			"", // All string literals are already interned
			null,
			NullArgumentException.checkNotNull(path, "path"),
			NullArgumentException.checkNotNull(book, "book")
		);
	}

	/**
	 * A PageRef is its own referrer.
	 */
	@Override
	public PageRef getPageRef() {
		return this;
	}

	/**
	 * Gets the domain of this book.  Two books are considered equal when they
	 * are in the same domain and have the same name.
	 * <p>
	 * When a domain is not provided, it defaults to {@code ""}, and the usual
	 * rules of equality still apply.  Thus, when writing content for distribution
	 * within a single site, the domain concept may be safely ignored.
	 * </p>
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * The name of the book the page is part of.
	 * This will always begin with a slash (/).
	 * 
	 * @see  String#intern()  always interned
	 */
	public String getBookName() {
		return bookName;
	}

	/**
	 * The prefix of the book the page is part of.
	 * This will be <code>""</code> for the root book <code>"/"</code>.
	 * 
	 * @see  String#intern()  always interned
	 */
	public String getBookPrefix() {
		String bn = bookName;
		return "/".equals(bn) ? "" : bn;
	}

	/**
	 * The book-relative path to the page, always starting with a slash (/).
	 * 
	 * @see  String#intern()  always interned
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 * 
	 * @return  this object if path unchanged or a new object representing the new path
	 */
	public PageRef setPath(String newPath) {
		return
			newPath.equals(path)
			? this
			: new PageRef(this.domain, this.bookName, newPath, this.book)
		;
	}

	/**
	 * the book itself, only available when have access to the referenced book.
	 */
	public Book getBook() {
		return book;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof PageRef)) return false;
		PageRef other = (PageRef)obj;
		assert domain == domain.intern() : "Interned inside constructor";
		assert bookName == bookName.intern() : "Interned inside constructor";
		assert path == path.intern() : "Interned inside constructor";
		return
			domain == other.domain
			&& bookName == other.bookName
			&& path == other.path
		;
	}

	private int hash;

	@Override
	public int hashCode() {
		int h = hash;
		if(h == 0) {
			h = domain.hashCode();
			h = h * 31 + bookName.hashCode();
			h = h * 31 + path.hashCode();
			hash = h;
		}
		return h;
	}

	/**
	 * Ordered by domain, servletPath.
	 *
	 * @see  #getServletPath()
	 */
	public int compareTo(PageRef o) {
		int diff = (domain == o.domain) ? 0 : domain.compareTo(o.domain);
		if(diff != 0) return diff;
		return getServletPath().compareTo(o.getServletPath());
	}

	@Override
	public int compareTo(PageReferrer o) {
		return compareTo(o.getPageRef());
	}

	// Would it be faster to just create this in the constructor and avoid volatile here? (Micro optimization here)
	private volatile String servletPath;

	/**
	 * Gets the combination of the book and the path that refers to the
	 * page resource within the web application.
	 * 
	 * @see #appendServletPath(java.lang.Appendable) 
	 */
	public String getServletPath() {
		String sp = servletPath;
		if(sp == null) {
			String bn = bookName;
			sp = ("/".equals(bn)) ? path : (bn + path);
			servletPath = sp;
		}
		return sp;
	}

	/**
	 * Appends the combination of the book and the path that refers to the
	 * page resource within the web application.
	 *
	 * @see #getServletPath() 
	 */
	public void appendServletPath(Appendable out) throws IOException {
		String bn = bookName;
		if(!"/".equals(bn)) out.append(bn);
		out.append(path);
	}

	@Override
	public String toString() {
		String servletPath = getServletPath();
		int domainLen = domain.length();
		if(domainLen == 0) {
			return servletPath;
		} else {
			return
				new StringBuilder(
					domainLen
					+ 1 // ':'
					+ servletPath.length()
				)
				.append(domain)
				.append(':')
				.append(servletPath)
				.toString();
		}
	}

	private volatile File resourceFile;
	// TODO: Is this cached too long now that we have higher-level caching strategies?
	private volatile Boolean exists;

	/**
	 * the underlying file, only available when have access to the referenced book
	 * 
	 * @param requireBook when true, will always get a File object back
	 * @param requireFile when true, any File object returned will exist on the filesystem
	 *
	 * @return null if not access to book or File of resource path.
	 */
	public File getResourceFile(boolean requireBook, boolean requireFile) throws IOException {
		if(book == null) {
			if(requireBook) throw new IOException("Book not found: " + bookName);
			return null;
		} else {
			File rf = resourceFile;
			if(rf == null) {
				File cvsworkDirectory = book.getCvsworkDirectory();
				// Skip past first slash
				assert path.charAt(0) == '/';
				int start = 1;
				// Skip past any trailing slashes
				int end = path.length();
				while(end > start && path.charAt(end - 1) == '/') end--;
				String subPath = path.substring(start, end);
				// Combine paths
				rf = subPath.isEmpty() ? cvsworkDirectory : new File(cvsworkDirectory, subPath);
				// The canonical file must be in the cvswork directory
				String canonicalPath = rf.getCanonicalPath();
				if(
					!canonicalPath.startsWith(
						cvsworkDirectory.getCanonicalPath() + File.separatorChar
					)
				) {
					throw new SecurityException();
				}
				this.resourceFile = rf;
			}
			if(requireFile) {
				Boolean e = this.exists;
				if(e == null) {
					e = rf.exists();
					this.exists = e;
				}
				if(!e) throw new FileNotFoundException(rf.getPath());
			}
			return rf;
		}
	}
}
