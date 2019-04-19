/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2013, 2014, 2015, 2016, 2019  AO Industries, Inc.
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

import com.aoindustries.util.AoCollections;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;

public class Page extends Node implements Comparable<Page> {

	public static final int MIN_TOC_LEVELS = 1;

	/**
	 * This matches the table of contents implementation in /semanticcms-section-servlet/toc.inc.jspx
	 * and is a result of HTML only having H1 through H6.  H1 is reserved
	 * for the page title and headings become H2 through H6.
	 */
	public static final int MAX_TOC_LEVELS = 5;

	public static final int DEFAULT_TOC_LEVELS = 3;

	private volatile PageRef pageRef;
	private volatile PageRef src;
	private volatile Copyright copyright;
	private Set<Author> authors;
	private volatile DateTime dateCreated;
	private volatile DateTime datePublished;
	private volatile DateTime dateModified;
	private volatile DateTime dateReviewed;
	private volatile String title;
	private volatile String shortTitle;
	private volatile String description;
	private volatile String keywords;
	private volatile Boolean allowRobots;
	private volatile Boolean toc;
	private volatile int tocLevels = DEFAULT_TOC_LEVELS;
	private Set<ParentRef> parentRefs;
	private volatile boolean allowParentMismatch;
	private Set<ChildRef> childRefs;
	private volatile boolean allowChildMismatch;
	private List<Element> elements;
	private Map<String,Element> elementsById;
	// Keeps track of which element Ids are system generated
	private Set<String> generatedIds;

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Page)) return false;
		return getPageRef().equals(((Page)obj).getPageRef());
	}

	@Override
	public int hashCode() {
		return getPageRef().hashCode();
	}

	@Override
	public int compareTo(Page o) {
		return getPageRef().compareTo(o.getPageRef());
	}

	@Override
	public Page freeze() {
		synchronized(lock) {
			if(!frozen) {
				if(authors != null) authors = AoCollections.optimalUnmodifiableSet(authors);
				if(parentRefs != null) parentRefs = AoCollections.optimalUnmodifiableSet(parentRefs);
				if(childRefs != null) childRefs = AoCollections.optimalUnmodifiableSet(childRefs);
				if(elements != null) {
					// Generate any missing IDs and freeze all elements
					for(Element element : elements) {
						// Calling getId causes any missing id to be generated
						element.getId();
						// Freeze it now, nothing else should change
						element.freeze();
					}
					assert elementsById != null;
					assert elements.size() == elementsById.size() : "elements and elementsById are different size: " + elements.size() + " != " + elementsById.size();
					elements = AoCollections.optimalUnmodifiableList(elements);
					elementsById = AoCollections.optimalUnmodifiableMap(elementsById);
				}
				if(generatedIds != null) generatedIds = AoCollections.optimalUnmodifiableSet(generatedIds);
				super.freeze();
			}
		}
		return this;
	}

	/**
	 * The PageRef that refers to this page.
	 */
	public PageRef getPageRef() {
		return pageRef;
	}

	public void setPageRef(PageRef pageRef) {
		checkNotFrozen();
		this.pageRef = pageRef;
	}

	/**
	 * The PageRef of the editable source for this page, if any.
	 */
	public PageRef getSrc() {
		return src;
	}

	public void setSrc(PageRef src) {
		checkNotFrozen();
		this.src = src;
	}

	public Copyright getCopyright() {
		return copyright;
	}

	public void setCopyright(Copyright copyright) {
		checkNotFrozen();
		this.copyright = copyright;
	}

	public Set<Author> getAuthors() {
		synchronized(lock) {
			if(authors == null) return Collections.emptySet();
			if(frozen) return authors;
			return AoCollections.unmodifiableCopySet(authors);
		}
	}

	public void addAuthor(Author author) {
		synchronized(lock) {
			checkNotFrozen();
			if(authors == null) authors = new LinkedHashSet<>();
			if(!authors.add(author)) throw new IllegalStateException("Duplicate author: " + author);
		}
	}

	/**
	 * <a href="https://schema.org/dateCreated">https://schema.org/dateCreated</a>
	 *
	 * @see  #getDatePublished()  When created and published are the same date, prefer
	 *                            published because it seems to have more use overall than created.
	 */
	public DateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(ReadableDateTime dateCreated) {
		checkNotFrozen();
		this.dateCreated = dateCreated==null ? null : dateCreated.toDateTime();
		checkDates();
	}

	/**
	 * <a href="https://schema.org/datePublished">https://schema.org/datePublished</a>
	 *
	 * @see  #getDateCreated()  When created and published are the same date, prefer
	 *                          published because it seems to have more use overall than created.
	 */
	public DateTime getDatePublished() {
		return datePublished;
	}

	public void setDatePublished(ReadableDateTime datePublished) {
		checkNotFrozen();
		this.datePublished = datePublished==null ? null : datePublished.toDateTime();
		checkDates();
	}

	/**
	 * <a href="https://schema.org/dateModified">https://schema.org/dateModified</a>
	 */
	public DateTime getDateModified() {
		return dateModified;
	}

	public void setDateModified(ReadableDateTime dateModified) {
		checkNotFrozen();
		this.dateModified = dateModified==null ? null : dateModified.toDateTime();
		checkDates();
	}

	/**
	 * This has no equivalent in <a href="https://schema.org/">https://schema.org/</a>, however
	 * we feel it is important to actively review content to ensure its accuracy, even when it
	 * has not been modified.
	 */
	public DateTime getDateReviewed() {
		return dateReviewed;
	}

	public void setDateReviewed(ReadableDateTime dateReviewed) {
		checkNotFrozen();
		this.dateReviewed = dateReviewed==null ? null : dateReviewed.toDateTime();
		checkDates();
	}

	/**
	 * Checks the dates for consistency.
	 */
	private void checkDates() {
		DateTime created = this.dateCreated;
		DateTime published = this.datePublished;
		DateTime modified = this.dateModified;
		DateTime reviewed = this.dateReviewed;
		if(
			created != null
			&& published != null
			&& published.compareTo(created) < 0
		) throw new IllegalArgumentException("published may not be before created");
		if(
			created != null
			&& modified != null
			&& modified.compareTo(created) < 0
		) throw new IllegalArgumentException("modified may not be before created");
		if(
			created != null
			&& reviewed != null
			&& reviewed.compareTo(created) < 0
		) throw new IllegalArgumentException("reviewed may not be before created");
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		checkNotFrozen();
		this.title = title;
	}

	/**
	 * A short title is used when the context of a page is well established, such as when
	 * showing a path to the current location in the site.  The short title defaults to <code>getTitle</code>.
	 *
	 * @return  the short page title
	 *
	 * @see  #getTitle
	 */
	public String getShortTitle() {
		String st = shortTitle;
		return st != null ? st : getTitle();
	}

	public void setShortTitle(String shortTitle) {
		checkNotFrozen();
		this.shortTitle = shortTitle;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		checkNotFrozen();
		this.description = description;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		checkNotFrozen();
		this.keywords = keywords;
	}

	/**
	 * Gets the allowRobots setting:
	 * <ul>
	 *   <li>{@literal null} (The default) - Inherit setting from parent(s) within the book or book settings if have no parents within the book</li>
	 *   <li>{@literal true} - Robots allowed</li>
	 *   <li>{@literal false} - Robots not allowed</li>
	 * </ul>
	 */
	public Boolean getAllowRobots() {
		return allowRobots;
	}

	public void setAllowRobots(Boolean allowRobots) {
		checkNotFrozen();
		this.allowRobots = allowRobots;
	}

	/**
	 * Gets the table of contents (toc) setting:
	 * <ul>
	 *   <li>{@literal null} (The default) - Show table of contents depending on number of entries in the table</li>
	 *   <li>{@literal true} - Always show table of contents</li>
	 *   <li>{@literal false} - Never show table of contents</li>
	 * </ul>
	 * TODO: Move this to Section.
	 */
	public Boolean getToc() {
		return toc;
	}

	public void setToc(Boolean toc) {
		checkNotFrozen();
		this.toc = toc;
	}

	/**
	 * TODO: Move this to Section.
	 */
	public int getTocLevels() {
		return tocLevels;
	}

	public void setTocLevels(int tocLevels) {
		checkNotFrozen();
		if(tocLevels < MIN_TOC_LEVELS || tocLevels > MAX_TOC_LEVELS) throw new IllegalArgumentException("tocLevels must be between " + MIN_TOC_LEVELS + " and " + MAX_TOC_LEVELS + ": " + tocLevels);
		this.tocLevels = tocLevels;
	}

	public Set<ParentRef> getParentRefs() {
		synchronized(lock) {
			if(parentRefs == null) return Collections.emptySet();
			if(frozen) return parentRefs;
			return AoCollections.unmodifiableCopySet(parentRefs);
		}
	}

	public void addParentRef(ParentRef parentRef) {
		synchronized(lock) {
			checkNotFrozen();
			if(parentRefs == null) parentRefs = new LinkedHashSet<>();
			if(!parentRefs.add(parentRef)) throw new IllegalStateException("Duplicate parent: " + parentRef);
		}
	}

	public boolean getAllowParentMismatch() {
		return allowParentMismatch;
	}

	public void setAllowParentMismatch(boolean allowParentMismatch) {
		checkNotFrozen();
		this.allowParentMismatch = allowParentMismatch;
	}

	public Set<ChildRef> getChildRefs() {
		synchronized(lock) {
			if(childRefs == null) return Collections.emptySet();
			if(frozen) return childRefs;
			return AoCollections.unmodifiableCopySet(childRefs);
		}
	}

	public void addChildRef(ChildRef childRef) {
		synchronized(lock) {
			checkNotFrozen();
			if(childRefs == null) childRefs = new LinkedHashSet<>();
			if(!childRefs.add(childRef)) throw new IllegalStateException("Duplicate child: " + childRef);
		}
	}

	public boolean getAllowChildMismatch() {
		return allowChildMismatch;
	}

	public void setAllowChildMismatch(boolean allowChildMismatch) {
		checkNotFrozen();
		this.allowChildMismatch = allowChildMismatch;
	}

	/**
	 * Gets all elements in the page (including all child elements), in the
	 * order they were declared in the page.
	 */
	public List<Element> getElements() {
		synchronized(lock) {
			if(elements == null) return Collections.emptyList();
			if(frozen) return elements;
			return AoCollections.unmodifiableCopyList(elements);
		}
	}

	/**
	 * Gets all elements in the page (including all child elements) that are of the
	 * given type, in the order they were declared in the page.
	 */
	public <E extends Element> List<E> filterElements(Class<E> clazz) {
		synchronized(lock) {
			if(elements == null) return Collections.emptyList();
			return AoCollections.filter(elements, clazz);
		}
	}

	/**
	 * Gets the elements indexed by id, in no particular order.
	 * Note, while the page is being created, elements with automatic IDs will not be in
	 * this map.  However, once frozen, every element will have an ID.
	 * 
	 * @see  #freeze()
	 */
	public Map<String,Element> getElementsById() {
		synchronized(lock) {
			if(elementsById == null) return Collections.emptyMap();
			if(frozen) return elementsById;
			return AoCollections.unmodifiableCopyMap(elementsById);
		}
	}

	/**
	 * Gets which element IDs were generated.
	 */
	public Set<String> getGeneratedIds() {
		synchronized(lock) {
			if(generatedIds == null) return Collections.emptySet();
			if(frozen) return generatedIds;
			return AoCollections.unmodifiableCopySet(generatedIds);
		}
	}

	/**
	 * Adds an element to this page.
	 */
	public void addElement(Element element) {
		synchronized(lock) {
			checkNotFrozen();
			element.setPage(this);
			// elements
			if(elements == null) elements = new ArrayList<>();
			elements.add(element);
			// elementsById
			addToElementsById(element, false);
		}
	}

	private void addToElementsById(Element element, boolean generated) {
		assert Thread.holdsLock(lock);
		String id = element.getIdNoGen();
		if(id != null) {
			if(elementsById == null) elementsById = new HashMap<>();
			if(elementsById.put(id, element) != null) throw new AssertionError("Duplicate id: " + id);
			if(generated) {
				if(generatedIds == null) generatedIds = new HashSet<>();
				if(!generatedIds.add(id)) throw new AssertionError("Duplicate id: " + id);
			}
		}
	}

	/**
	 * Called when an element within this page has its id set.
	 * This will only be called at most once, when the element's id goes from null to non-null.
	 *
	 * @see  Element#setId(java.lang.String)
	 */
	void onElementIdSet(Element element, boolean generated) {
		synchronized(lock) {
			addToElementsById(element, generated);
		}
	}

	/**
	 * The page label is its short title.
	 *
	 * @see  #getShortTitle()
	 */
	@Override
	public String getLabel() {
		return getShortTitle();
	}
}
