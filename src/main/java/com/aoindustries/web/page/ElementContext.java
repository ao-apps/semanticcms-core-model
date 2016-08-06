/*
 * ao-web-page - Java API for modeling web page content and relationships.
 * Copyright (C) 2015, 2016  AO Industries, Inc.
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

import java.io.IOException;
import java.io.Writer;

/**
 * An element context is able to process includes of other resources during element writing.
 */
public interface ElementContext {

	/**
	 * Includes the given resource into the given writer
	 */
	void include(String resource, Writer out) throws IOException;
}
