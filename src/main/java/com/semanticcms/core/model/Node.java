/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2015, 2016, 2017, 2019, 2020, 2021  AO Industries, Inc.
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
import com.aoapps.io.buffer.BufferResult;
import com.aoapps.io.buffer.EmptyResult;
import com.aoapps.lang.Freezable;
import com.aoapps.lang.FrozenException;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A node contains elements, but is not necessarily an element itself.
 * A node can also have references to pages.
 * A node can have a captured body.
 */
public abstract class Node implements Freezable<Node> {

	/**
	 * Should be kept as a thread-local, is not thread safe.
	 */
	static class IdGenerator {

		/**
		 * Random numbers for node keys.
		 * <p>
		 * Note: This is not a {@linkplain SecureRandom#getInstanceStrong() strong instance} to avoid blocking.
		 * </p>
		 */
		private static final SecureRandom secureRandom = new SecureRandom();

		/**
		 * The number of consecutive values to use per thread before getting a new random start.
		 * To reduce the lock contention and load on the random number generator, each thread will use a range of values
		 * started at a value provided by this random.  TODO: Watch for and avoid wraparound.
		 */
		private static final int CONSECUTIVE_COUNT = 16384;

		private long nextVal;
		private long rangeEnd;

		long getNextId() {
			long c = nextVal++;
			if(c >= rangeEnd) {
				// Start new range
				c = secureRandom.nextLong();
				// Avoid wraparound
				if(c > (Long.MAX_VALUE - CONSECUTIVE_COUNT)) c -= CONSECUTIVE_COUNT;
				nextVal = c + 1;
				rangeEnd = c + CONSECUTIVE_COUNT;
			}
			return c;
		}

		/**
		 * Resets the idGenerator to start a new random range on next id.
		 * This should be called when an id collision is detected.
		 */
		void reset() {
			nextVal = 0;
			rangeEnd = 0;
		}
	}

	private static final ThreadLocal<IdGenerator> idGenerators = ThreadLocal.withInitial(IdGenerator::new);

	private static class Lock {/* Empty lock class to help heap profile */}
	protected final Object lock = new Lock();
	protected volatile boolean frozen; // Accessed without lock, only updated under lock
	private Map<String, Object> properties;
	private List<Element> childElements;
	private Map<Long, ElementWriter> elementWriters;
	private Set<PageRef> pageLinks;
	private volatile BufferResult body;

	/**
	 * The toString calls {@link #getLabel()}
	 */
	@Override
	public String toString() {
		return getLabel();
	}

	@Override
	public Node freeze() {
		synchronized(lock) {
			if(!frozen) {
				properties = AoCollections.optimalUnmodifiableMap(properties);
				childElements = AoCollections.optimalUnmodifiableList(childElements);
				elementWriters = AoCollections.optimalUnmodifiableMap(elementWriters);
				pageLinks = AoCollections.optimalUnmodifiableSet(pageLinks);
				frozen = true;
			}
		}
		return this;
	}

	// TODO: Should all uses of checkNotFrozen be synchronized on lock?
	// TODO: As-is, there are many small race conditions between checkNotFrozen and setting the value.
	protected void checkNotFrozen() throws FrozenException {
		if(frozen) throw new FrozenException();
	}

	/**
	 * Gets an unmodifiable snapshot of all properties associated with a node.
	 * The returned map will not change, even if properties are added to the node.
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
	public Map<String, Object> getProperty() {
		// TODO: If we have properties volatile, could do the null and frozen cases outside the synchronized block
		// TODO: Would this be faster?
		synchronized(lock) {
			if(properties == null) return Collections.emptyMap();
			if(frozen) return properties;
			return AoCollections.unmodifiableCopyMap(properties);
		}
	}

	/**
	 * Sets a property on this node, if the property has not already been set.
	 * Setting a property to {@code null} still marks the property as used and associates it with {@code null}.
	 *
	 * @return  {@code true} when the property was added or {@code false} when the property already existed
	 *                       (including possibly with a {@code null} value).
	 *
	 * @throws  FrozenException  properties may not be set once the node is frozen
	 */
	public boolean setProperty(String name, Object value) throws FrozenException {
		synchronized(lock) {
			checkNotFrozen();
			if(properties == null) {
				properties = new LinkedHashMap<>();
			} else if(properties.containsKey(name)) {
				return false;
			}
			properties.put(name, value);
			return true;
		}
	}

	/**
	 * Every node may potentially have child elements.
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
	public List<Element> getChildElements() {
		synchronized(lock) {
			if(childElements == null) return Collections.emptyList();
			if(frozen) return childElements;
			return AoCollections.unmodifiableCopyList(childElements);
		}
	}

	/**
	 * Adds a child element to this node.
	 */
	public Long addChildElement(Element childElement, ElementWriter elementWriter) {
		synchronized(lock) {
			checkNotFrozen();
			if(childElements == null) childElements = new ArrayList<>();
			childElements.add(childElement);
			if(elementWriters == null) elementWriters = new HashMap<>();
			IdGenerator idGenerator = idGenerators.get();
			while(true) {
				Long elementKey = idGenerator.getNextId();
				if(!elementWriters.containsKey(elementKey)) {
					elementWriters.put(elementKey, elementWriter);
					return elementKey;
				} else {
					// Reset generator when duplicate found (this should be extremely rare)
					idGenerator.reset();
				}
			}
		}
	}

	ElementWriter getElementWriter(long elementKey) {
		synchronized(lock) {
			return elementWriters==null ? null : elementWriters.get(elementKey);
		}
	}

	/**
	 * Gets the set of all pages this node directly links to; this does not
	 * include pages linked to by child elements.
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
	public Set<PageRef> getPageLinks() {
		synchronized(lock) {
			if(pageLinks == null) return Collections.emptySet();
			if(frozen) return pageLinks;
			return AoCollections.unmodifiableCopySet(pageLinks);
		}
	}

	public void addPageLink(PageRef pageLink) {
		synchronized(lock) {
			checkNotFrozen();
			if(pageLinks == null) pageLinks = new LinkedHashSet<>();
			pageLinks.add(pageLink);
		}
	}

	/**
	 * Every node may potentially have HTML body.
	 */
	public BufferResult getBody() {
		BufferResult b = body;
		if(b == null) return EmptyResult.getInstance();
		return b;
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
	 */
	public abstract String getLabel();

	/**
	 * Appends a short description, useful for links and lists, for this node.
	 *
	 * @deprecated  Please use {@link #getLabel()}
	 */
	@Deprecated
	public final void appendLabel(Appendable out) throws IOException {
		out.append(getLabel());
	}

	/**
	 * <p>
	 * Looks for the nearest nested elements of the given class.  These elements
	 * may be direct descendants or descendants further down the tree, but only
	 * the top-most descendants are returned.
	 * </p>
	 * <p>
	 * If the node is a page, its elements are checked, but the elements of its child pages are not.
	 * </p>
	 *
	 * @return   The unmodifiable list of top-level matches, in the order they were declared in the page, or empty list if none found.
	 */
	public <E> List<E> findTopLevelElements(Class<E> elementType) {
		List<E> matches = findTopLevelElementsRecurse(elementType, this, null);
		if(matches == null) return Collections.emptyList();
		return Collections.unmodifiableList(matches);
	}

	/**
	 * Recursive component of findTopLevelElements.
	 *
	 * @see  #findTopLevelElements(java.lang.Class)
	 */
	private static <E> List<E> findTopLevelElementsRecurse(Class<E> elementType, Node node, List<E> matches) {
		for(Element elem : node.getChildElements()) {
			if(elementType.isInstance(elem)) {
				// Found match
				if(matches == null) matches = new ArrayList<>();
				matches.add(elementType.cast(elem));
			} else {
				// Look further down the tree
				matches = findTopLevelElementsRecurse(elementType, elem, matches);
			}
		}
		return matches;
	}

	/**
	 * <p>
	 * Finds the first descendant element of the given class or interface and matching the given {@link Predicate},
	 * with a depth-first traversal.
	 * </p>
	 * <p>
	 * If the node is a page, its elements are checked, but the elements of its child pages are not.
	 * </p>
	 *
	 * @return   The element or {@link Optional#empty()} when not found.
	 */
	public <E> Optional<E> findChildElement(Class<E> elementType, Predicate<? super E> filter) {
		return Optional.ofNullable(findChildElementRecurse(elementType, filter, this));
	}

	/**
	 * Recursive component of findChild.
	 *
	 * @see  #findChildElement(java.lang.Class, java.util.function.Predicate)
	 */
	private static <E> E findChildElementRecurse(Class<E> elementType, Predicate<? super E> filter, Node node) {
		for(Element elem : node.getChildElements()) {
			if(elementType.isInstance(elem)) {
				// Found matching type
				E e = elementType.cast(elem);
				if(filter.test(e)) {
					// Found match
					return e;
				}
			}
			// Look further down the tree
			E match = findChildElementRecurse(elementType, filter, elem);
			if(match != null) return match;
		}
		return null;
	}

	/**
	 * <p>
	 * Finds the first descendant element of the given class or interface,
	 * with a depth-first traversal.
	 * </p>
	 * <p>
	 * If the node is a page, its elements are checked, but the elements of its child pages are not.
	 * </p>
	 *
	 * @return   The element or {@link Optional#empty()} when not found.
	 */
	public <E> Optional<E> findChildElement(Class<E> elementType) {
		return findChildElement(elementType, elem -> true);
	}

	/**
	 * <p>
	 * Finds the first descendant element of the given class or interface and matching the given {@link Predicate},
	 * with a depth-first traversal.
	 * </p>
	 * <p>
	 * If the node is a page, its elements are checked, but the elements of its child pages are not.
	 * </p>
	 *
	 * @return   The element.
	 *
	 * @throws  NoSuchElementException  when not found.
	 */
	public <E> E requireChildElement(Class<E> elementType, Predicate<? super E> filter) throws NoSuchElementException {
		return findChildElement(elementType, filter).orElseThrow(
			() -> new NoSuchElementException(elementType.toGenericString())
		);
	}

	/**
	 * <p>
	 * Finds the first descendant element of the given class or interface,
	 * with a depth-first traversal.
	 * </p>
	 * <p>
	 * If the node is a page, its elements are checked, but the elements of its child pages are not.
	 * </p>
	 *
	 * @return   The element.
	 *
	 * @throws  NoSuchElementException  when not found.
	 */
	public <E> E requireChildElement(Class<E> elementType) throws NoSuchElementException {
		return requireChildElement(elementType, elem -> true);
	}
}
