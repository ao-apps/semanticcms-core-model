/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2015, 2016  AO Industries, Inc.
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

import com.aoindustries.lang.ObjectUtils;
import java.util.Map;

/**
 * A page may contain any number of elements (along with arbitrary HTML
 * and JSP content).  Each element may also contain other elements.
 */
abstract public class Element extends Node {

	/**
	 * Makes sure an ID is valid.
	 * Must match [A-Za-z][A-Za-z0-9:_.-]*
	 *
	 * @see <a href="http://www.w3.org/TR/2002/REC-xhtml1-20020801/#C_8">http://www.w3.org/TR/2002/REC-xhtml1-20020801/#C_8</a>
	 */
	public static boolean isValidId(String id) {
		if(id == null) return false;
		int len = id.length();
		if(len == 0) return false;
		// First character must be [A-Za-z]
		char ch = id.charAt(0);
		if(
			(ch<'A' || ch>'Z')
			&& (ch<'a' || ch>'z')
		) {
			return false;
		}
		// Remaining characters must match [A-Za-z0-9:_.-]
		for(int i = 1; i < len; i++) {
			if(
				(ch < 'A' || ch > 'Z')
				&& (ch < 'a' || ch > 'z')
				&& (ch < '0' || ch > '9')
				&& ch != ':'
				&& ch != '_'
				&& ch != '.'
				&& ch != '-'
			) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Generates a valid ID from an arbitrary string.
	 *
	 * Strip all character not matching [A-Za-z][A-Za-z0-9:_.-]*
	 *
	 * Also converts characters to lower case.
	 *
	 * @param  template The preferred text to base the id on
	 * @param  prefix   The base used when template is unusable (must be a valid id or invalid ID's may be generated)
	 *
	 * @see <a href="http://www.w3.org/TR/2002/REC-xhtml1-20020801/#C_8">http://www.w3.org/TR/2002/REC-xhtml1-20020801/#C_8</a>
	 */
	public static StringBuilder generateIdPrefix(String template, String prefix) {
		assert isValidId(prefix);
		final int len = template.length();
		// First character must be [A-Za-z]
		int pos = 0;
		while(pos < len) {
			char ch = template.charAt(pos);
			if(
				(ch >= 'A' && ch <= 'Z')
				|| (ch >= 'a' && ch <= 'z')
			) {
				break;
			}
			pos++;
		}
		StringBuilder idPrefix;
		if(pos == len) {
			// No usable characters from label
			idPrefix = new StringBuilder(prefix);
		} else {
			// Get remaining usable characters from label
			idPrefix = new StringBuilder(len - pos);
			//idPrefix.append(template.charAt(pos));
			//pos++;
			// Remaining must match [A-Za-z0-9:_.-]
			while(pos < len) {
				char ch = template.charAt(pos);
				pos++;
				// Convert space to '-'
				if(ch == ' ') {
					idPrefix.append('-');
				} else if(
					(ch >= 'A' && ch <= 'Z')
					|| (ch >= 'a' && ch <= 'z')
					|| (ch >= '0' && ch <= '9')
					|| ch == ':'
					|| ch == '_'
					|| ch == '.'
					|| ch == '-'
				) {
					if(ch >= 'A' && ch <= 'Z') {
						// Works since we're only using ASCII range:
						ch = (char)(ch + ('a' - 'A'));
						// Would support Unicode, but id's don't have Unicode:
						// ch = Character.toLowerCase(ch);
					}
					idPrefix.append(ch);
				}
			}
		}
		assert isValidId(idPrefix.toString());
		return idPrefix;
	}

	private Page page;
	private String id;
	private Element parentElement;

	/**
	 * Two elements are equal when they are the same object or are on the same page
	 * and have the same ID.  If either one does not have a page
	 * or does not (yet) have an ID, they will not be equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Element)) return false;
		if(this == obj) return true;
		Element other = (Element)obj;
		return
			page != null
			&& id != null
			&& page.equals(other.page)
			&& id.equals(other.id)
		;
	}

	@Override
	public int hashCode() {
		int hash = ObjectUtils.hashCode(page);
		hash = hash * 31 + ObjectUtils.hashCode(id);
		return hash;
	}

	/**
	 * Gets the default element ID prefix for this type of element.
	 */
	abstract protected String getDefaultIdPrefix();

	/**
	 * Gets the CSS class used to stylize links to elements of this type
	 * or {@code null} if not supported.
	 */
	abstract public String getLinkCssClass();

	@Override
	public Element freeze() {
		super.freeze();
		return this;
	}

	/**
	 * Every element may (and usually will) exist within a page.
	 */
	public Page getPage() {
		return page;
	}

	/**
	 * This is set when the element is associated with the page.
	 *
	 * @see Page#addElement(com.aoindustries.docs.Element)
	 */
	void setPage(Page page) {
		checkNotFrozen();
		if(this.page != null) throw new IllegalStateException("element already has a page: " + this);
		this.page = page;
		assert checkPageAndParentElement();
	}

	/**
	 * Gets the ID, without generating it if missing.
	 */
	String getIdNoGen() {
		return id;
	}

	/**
	 * When inside a page, every element must have a per-page unique ID, when one is not provided, it will be generated.
	 * When not inside a page, no missing ID is generated and it will remain null.
	 */
	public String getId() {
		if(id == null) {
			if(page != null) {
				Map<String,Element> elementsById = page.getElementsById();
				// Generate the ID now
				StringBuilder possId = Element.generateIdPrefix(getLabel(), getDefaultIdPrefix());
				int possIdLen = possId.length();
				// Find an unused identifier
				for(int i=1; i<=Integer.MAX_VALUE; i++) {
					if(i == Integer.MAX_VALUE) throw new IllegalStateException("ID not generated");
					if(i>1) possId.append('-').append(i);
					String newId = possId.toString();
					if(
						elementsById == null
						|| !elementsById.containsKey(newId)
					) {
						setId(newId, true);
						break;
					}
					// Reset for next element number to check
					possId.setLength(possIdLen);
				}
			}
		}
		return id;
	}

	public void setId(String id) {
		setId(id, false);
	}

	void setId(String id, boolean generated) {
		checkNotFrozen();
		if(this.id != null) throw new IllegalStateException("id already set");
		if(id != null && !id.isEmpty()) {
			if(!isValidId(id)) throw new IllegalArgumentException("Invalid id: " + id);
			this.id = id;
			if(page != null) page.onElementIdSet(this, generated);
		}
	}

	/**
	 * Elements may be nested, gets the parent Element above this element.
	 * Top-level parents within a page, or standalone elements, will not have
	 * any parent.
	 */
	public Element getParentElement() {
		return parentElement;
	}

	/**
	 * Sets the parent element of this element.
	 */
	private void setParentElement(Element parentElement) {
		checkNotFrozen();
		if(this.parentElement != null) throw new IllegalStateException("parentElement already set");
		this.parentElement = parentElement;
		assert checkPageAndParentElement();
	}

	private boolean checkPageAndParentElement() {
		if(
			page != null
			&& parentElement != null
		) {
			if(!page.equals(parentElement.page)) throw new IllegalArgumentException("parentElement is not on the same page");
		}
		return true;
	}

	/**
	 * Adds a child element to this element.
	 */
	@Override
	public Long addChildElement(Element childElement, ElementWriter elementWriter) {
		Long elementKey = super.addChildElement(childElement, elementWriter);
		childElement.setParentElement(this);
		return elementKey;
	}
}
