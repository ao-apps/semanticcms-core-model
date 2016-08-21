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

import com.aoindustries.io.buffer.BufferResult;
import com.aoindustries.io.buffer.EmptyResult;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A node contains elements, but is not necessarily an element itself.
 * A node can also have references to pages.
 * A node can have a captured body.
 */
abstract public class Node implements Freezable<Node> {

	/**
	 * Random numbers for node keys.
	 */
	private static final SecureRandom random = new SecureRandom();

	private boolean frozen;
	private List<Element> childElements;
	private Map<Long,ElementWriter> elementWriters;
	private Set<PageRef> pageLinks;
	private BufferResult body;

	/**
	 * The toString calls {@link #getLabel()}
	 */
	@Override
	public String toString() {
		return getLabel();
	}

	@Override
	public Node freeze() {
		frozen = true;
		return this;
	}

	protected void checkNotFrozen() throws FrozenException {
		if(frozen) throw new FrozenException();
	}

	/**
	 * Every node may potentially have child elements.
	 */
	public List<Element> getChildElements() {
		if(childElements == null) return Collections.emptyList();
		return Collections.unmodifiableList(childElements);
	}

	/**
	 * Adds a child element to this node.
	 */
	public Long addChildElement(Element childElement, ElementWriter elementWriter) {
		checkNotFrozen();
		if(childElements == null) childElements = new ArrayList<Element>();
		childElements.add(childElement);
		if(elementWriters == null) elementWriters = new HashMap<Long,ElementWriter>();
		while(true) {
			Long elementKey = random.nextLong();
			if(!elementWriters.containsKey(elementKey)) {
				elementWriters.put(elementKey, elementWriter);
				return elementKey;
			}
		}
	}

	ElementWriter getElementWriter(long elementKey) {
		return elementWriters==null ? null : elementWriters.get(elementKey);
	}

	/**
	 * Gets the set of all pages this node directly links to; this does not
	 * include pages linked to by child elements.
	 */
	public Set<PageRef> getPageLinks() {
		if(pageLinks == null) return Collections.emptySet();
		return Collections.unmodifiableSet(pageLinks);
	}

	public void addPageLink(PageRef pageLink) {
		checkNotFrozen();
		if(pageLinks == null) pageLinks = new LinkedHashSet<PageRef>();
		pageLinks.add(pageLink);
	}

	/**
	 * Every node may potentially have HTML body.
	 */
	public BufferResult getBody() {
		if(body == null) return EmptyResult.getInstance();
		return body;
	}

	public void setBody(BufferResult body) {
		checkNotFrozen();
		try {
			assert body.getLength()==body.trim().getLength() : "body must have already been trimmed";
		} catch(IOException e) {
			throw new AssertionError(e);
		}
		this.body = body;
	}

	/**
	 * Gets a short description, useful for links and lists, for this node.
	 * 
	 * This default implementation calls {@link #appendLabel(java.lang.Appendable)}, thus you *must*
	 * override at least this method or {@link #appendLabel(java.lang.Appendable)}.
	 * 
	 * @throws StackOverflowError if {@link #appendLabel(java.lang.Appendable)} not overridden.
	 */
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		try {
			appendLabel(sb);
		} catch(IOException e) {
			// Java 1.7: new constructor
			AssertionError ae = new AssertionError("Should not happen because using StringBuilder");
			ae.initCause(e);
			throw ae;
		}
		return sb.toString();
	}

	/**
	 * Appends a short description, useful for links and lists, for this node.
	 *
	 * This default implementation calls {@link #getLabel()}, thus you *must*
	 * override at least this method or {@link #getLabel()}.
	 * 
	 * @throws StackOverflowError if {@link #getLabel()} not overridden.
	 */
	public void appendLabel(Appendable out) throws IOException {
		out.append(getLabel());
	}

	/**
	 * <p>
	 * Looks for the nearest nested elements of the given class.  These elements
	 * may be direct descendents or descendents further down the tree, but only
	 * the top-most descendents are returned.
	 * </p>
	 * <p>
	 * If the node is a page, its elements are checked, but the elements of its child pages are not.
	 * </p>
	 *
	 * @return   The unmodifiable list of top-level matches, in the order they were declared in the page, or empty list if none found.
	 */
	public <E extends Element> List<E> findTopLevelElements(Class<E> elementType) {
		List<E> matches = findTopLevelElementsRecurse(elementType, this, null);
		if(matches == null) return Collections.emptyList();
		return Collections.unmodifiableList(matches);
	}

	/**
	 * Recursive component of findTopLevelElements.
	 * 
	 * @see  #findTopLevelElements
	 */
	private static <E extends Element> List<E> findTopLevelElementsRecurse(Class<E> elementType, Node node, List<E> matches) {
		for(Element elem : node.getChildElements()) {
			if(elementType.isInstance(elem)) {
				// Found match
				if(matches == null) matches = new ArrayList<E>();
				matches.add(elementType.cast(elem));
			} else {
				// Look further down the tree
				matches = findTopLevelElementsRecurse(elementType, elem, matches);
			}
		}
		return matches;
	}
}
