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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Heading extends Element {

	private String label;

	@Override
	public Heading freeze() {
		super.freeze();
		return this;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		checkNotFrozen();
		this.label = label==null || label.isEmpty() ? null : label;
	}

	@Override
	public String getListItemCssClass() {
		return "ao-web-page-list-item-heading";
	}

	@Override
	protected String getDefaultIdPrefix() {
		return "heading";
	}

	@Override
	public String getLinkCssClass() {
		return null;
	}

	// <editor-fold desc="Child Headings">
	private List<Heading> childHeadings;

	public List<Heading> getChildHeadings() {
		if(childHeadings == null) return Collections.emptyList();
		return Collections.unmodifiableList(childHeadings);
	}

	/**
	 * Adds a child heading to this heading.
	 */
	public void addChildHeading(Heading heading) {
		checkNotFrozen();
		if(childHeadings == null) childHeadings = new ArrayList<Heading>();
		childHeadings.add(heading);
	}
	// </editor-fold>
}
