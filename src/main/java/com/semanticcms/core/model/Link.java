/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2013, 2014, 2015, 2016, 2017, 2019, 2020, 2021  AO Industries, Inc.
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

import static com.aoapps.lang.Strings.nullIfEmpty;
import com.aoapps.net.DomainName;
import com.aoapps.net.Path;
import com.aoapps.net.URIParameters;
import com.aoapps.net.UnmodifiableURIParameters;

// TODO: Support optional renderer before view, defaulting to current renderer
public class Link extends Element {

	/**
	 * The default view is the content view and will have the empty view name.
	 */
	public static final String DEFAULT_VIEW_NAME = "content";

	private volatile DomainName domain;
	private volatile Path book;
	private volatile String page;
	private volatile String element;
	private volatile boolean allowGeneratedElement;
	private volatile String anchor;
	private volatile String view = DEFAULT_VIEW_NAME;
	private volatile boolean small;
	private volatile URIParameters params;
	private volatile boolean absolute;
	private volatile boolean canonical;
	private volatile String clazz;

	@Override
	public Link freeze() throws IllegalStateException {
		synchronized(lock) {
			if(!frozen) {
				params = UnmodifiableURIParameters.wrap(params);
				super.freeze();
			}
		}
		return this;
	}

	@Override
	public String getLabel() {
		return "Link";
	}

	/**
	 * Link elements are hidden from tree views.
	 */
	@Override
	public boolean isHidden() {
		return true;
	}

	public DomainName getDomain() {
		return domain;
	}

	public void setDomain(DomainName domain) {
		checkNotFrozen();
		this.domain = domain;
	}

	public Path getBook() {
		return book;
	}

	public void setBook(Path book) {
		checkNotFrozen();
		this.book = book;
	}

	public String getPagePath() {
		return page;
	}

	// TODO: Path page?
	public void setPagePath(String page) {
		checkNotFrozen();
		this.page = nullIfEmpty(page);
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		checkNotFrozen();
		this.element = nullIfEmpty(element);
	}

	public boolean getAllowGeneratedElement() {
		return allowGeneratedElement;
	}

	public void setAllowGeneratedElement(boolean allowGeneratedElement) {
		checkNotFrozen();
		this.allowGeneratedElement = allowGeneratedElement;
	}

	public String getAnchor() {
		return anchor;
	}

	public void setAnchor(String anchor) {
		checkNotFrozen();
		this.anchor = nullIfEmpty(anchor);
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		checkNotFrozen();
		view = nullIfEmpty(view);
		this.view = view == null ? DEFAULT_VIEW_NAME : view;
	}

	public boolean getSmall() {
		return small;
	}

	public void setSmall(boolean small) {
		checkNotFrozen();
		this.small = small;
	}

	public URIParameters getParams() {
		return params;
	}

	public void setParams(URIParameters params) {
		synchronized(lock) {
			checkNotFrozen();
			this.params = params;
		}
	}

	public boolean getAbsolute() {
		return absolute;
	}

	public void setAbsolute(boolean absolute) {
		checkNotFrozen();
		this.absolute = absolute;
	}

	public boolean getCanonical() {
		return canonical;
	}

	public void setCanonical(boolean canonical) {
		checkNotFrozen();
		this.canonical = canonical;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		checkNotFrozen();
		this.clazz = nullIfEmpty(clazz);
	}

	@Override
	protected String getDefaultIdPrefix() {
		return "link";
	}
}
