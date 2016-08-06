/*
 * ao-web-page - Java API for modeling web page content and relationships.
 * Copyright (C) 2013, 2014, 2015, 2016  AO Industries, Inc.
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

import com.aoindustries.lang.NullArgumentException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A page reference contains both a book and a path to a page or directory.
 * Any path to a directory must end with a slash (/).
 * 
 * // TODO: Support parameters to a page, child, link, ...
 *          Parameters provided in path/page?, param.* attributes, and nested tags - matching/extending AO taglib.
 */
public class PageRef {

	private final String bookName;
	private final String path;
	
	/**
	 * TODO: Consider not having reference to book here, it simplifies the book loading order issue
	 *       (see BooksContextListener), and rules about allow missing books can be handled in more
	 *       appropriate places.
	 */
	private final Book book;

	private PageRef(String bookName, String path, Book book) {
		this.bookName = NullArgumentException.checkNotNull(bookName, "bookName");
		this.path = NullArgumentException.checkNotNull(path, "path");
		if(!path.startsWith("/")) throw new IllegalArgumentException("Path does not begin with a slash: " + path);
		assert book==null || book.getName().equals(bookName);
		this.book = book;
	}

	public PageRef(String bookName, String path) {
		this(bookName, path, null);
	}

	public PageRef(Book book, String path) {
		this(book.getName(), path, book);
	}

	/**
	 * The name of the book the page is part of.
	 * This will always begin with a slash (/).
	 */
	public String getBookName() {
		return bookName;
	}

	/**
	 * The prefix of the book the page is part of.
	 * This will be <code>""</code> for the root book <code>"/"</code>.
	 */
	public String getBookPrefix() {
		return "/".equals(bookName) ? "" : bookName;
	}

	/**
	 * The book-relative path to the page, always starting with a slash (/).
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
			: new PageRef(this.bookName, newPath, this.book)
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
		if(!(obj instanceof PageRef)) return false;
		PageRef other = (PageRef)obj;
		return
			bookName.equals(other.bookName)
			&& path.equals(other.path)
		;
	}

	private int hash;

	@Override
	public int hashCode() {
		int h = hash;
		if(h == 0) {
			h = bookName.hashCode();
			h = h * 31 + path.hashCode();
			hash = h;
		}
		return h;
	}

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
			sp = ("/".equals(bookName)) ? path : (bookName + path);
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
		if(!"/".equals(bookName)) out.append(bookName);
		out.append(path);
	}

	@Override
	public String toString() {
		return getServletPath();
	}

	private volatile File resourceFile;

	/**
	 * the underlying file, only available when have access to the referenced book
	 * 
	 * @param requireBook when true, will always get a File object back
	 * @param requireFile when true, any File object returned will exist on the filesystem
	 *
	 * @return null if not access to book or File of resource path.
	 */
	public File getResourceFile(boolean requireBook, boolean requireFile) throws IOException {
		if(book==null) {
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
				if(requireFile && !rf.exists()) throw new FileNotFoundException(rf.getPath());
				this.resourceFile = rf;
			}
			return rf;
		}
	}
}
