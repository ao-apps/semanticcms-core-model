/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2015, 2016, 2019  AO Industries, Inc.
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

import com.aoindustries.xml.XmlUtils;
import java.util.Map;
import java.util.Objects;

/**
 * A page may contain any number of elements (along with arbitrary textual data
 * and other content).  Each element may also contain other elements.
 */
abstract public class Element extends Node {

	/**
	 * Makes sure an ID is valid.
	 *
	 * @deprecated  Please use {@link XmlUtils#isValidId(java.lang.String)} directly.
	 */
	@Deprecated
	public static boolean isValidId(String id) {
		return XmlUtils.isValidId(id);
	}

	/**
	 * Generates a valid ID from an arbitrary string.
	 *
	 * @deprecated  Please use {@link XmlUtils#generateId(java.lang.String, java.lang.String)} directly.
	 */
	@Deprecated
	public static StringBuilder generateIdPrefix(String template, String prefix) {
		return XmlUtils.generateId(template, prefix);
	}

	private volatile Page page;
	private volatile String id;
	private volatile Element parentElement;

	/**
	 * Two elements are equal when they are the same object or when they are
	 * on the same page and have the same ID.  If either one does not have a page
	 * or does not (yet) have an ID, they will not be equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof Element)) return false;
		Page p = page;
		String i = id;
		if(p == null || i == null) return false;
		Element other = (Element)obj;
		return
			p.equals(other.page)
			&& i.equals(other.id)
		;
	}

	@Override
	public int hashCode() {
		int hash = Objects.hashCode(page);
		hash = hash * 31 + Objects.hashCode(id);
		return hash;
	}

	/**
	 * Gets the element ID template for generating IDs.
	 * 
	 * @see  #getLabel()  Defaults to getLabel()
	 */
	protected String getElementIdTemplate() {
		return getLabel();
	}

	/**
	 * Gets the default element ID prefix for this type of element.
	 */
	abstract protected String getDefaultIdPrefix();

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
		synchronized(lock) {
			checkNotFrozen();
			if(this.page != null) throw new IllegalStateException("element already has a page: " + this);
			this.page = page;
		}
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
			synchronized(lock) {
				if(id == null) {
					if(page != null) {
						Map<String,Element> elementsById = page.getElementsById();
						// Generate the ID now
						String template = getElementIdTemplate();
						if(template == null) {
							throw new IllegalStateException("null from getElementIdTemplate");
						}
						StringBuilder possId = XmlUtils.generateId(template, getDefaultIdPrefix());
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
			}
		}
		return id;
	}

	public void setId(String id) {
		setId(id, false);
	}

	void setId(String id, boolean generated) {
		synchronized(lock) {
			checkNotFrozen();
			if(this.id != null) throw new IllegalStateException("id already set");
			if(id != null && !id.isEmpty()) {
				if(!XmlUtils.isValidId(id)) throw new IllegalArgumentException("Invalid id: " + id);
				this.id = id;
				if(page != null) page.onElementIdSet(this, generated);
			}
		}
	}

	private volatile ElementRef elementRef;

	/**
	 * Gets an ElementRef for this element.
	 * Must have a page set.
	 * If id has not yet been set, one will be generated.
	 *
	 * @throws  IllegalStateException  if page not set
	 */
	public ElementRef getElementRef() throws IllegalStateException {
		Page p = page;
		if(p == null) throw new IllegalStateException("page not set");
		PageRef pageRef = p.getPageRef();
		String i = getId();
		if(i == null) throw new IllegalStateException("page not set so no id generated");
		ElementRef er = elementRef;
		if(
			er == null
			// Make sure object still valid
			|| !er.getPageRef().equals(pageRef)
			|| !er.getId().equals(i)
		) {
			er = new ElementRef(pageRef, i);
			elementRef = er;
		}
		return er;
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
		synchronized(lock) {
			checkNotFrozen();
			if(this.parentElement != null) throw new IllegalStateException("parentElement already set");
			this.parentElement = parentElement;
		}
		assert checkPageAndParentElement();
	}

	private boolean checkPageAndParentElement() {
		Page p = this.page;
		Element pe = this.parentElement;
		if(
			p != null
			&& pe != null
		) {
			if(!p.equals(pe.getPage())) throw new IllegalArgumentException("parentElement is not on the same page");
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

	/**
	 * When hidden, an element is not added to common elements like navigation trees.
	 * By default, elements are not hidden.
	 */
	public boolean isHidden() {
		return false;
	}
}
