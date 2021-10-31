/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2015, 2016, 2019, 2021  AO Industries, Inc.
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

import java.io.Writer;
import java.util.Map;

/**
 * An element context is able to process includes of other resources during element writing.
 */
@FunctionalInterface
public interface ElementContext {

	/**
	 * Includes the given resource into the given writer with the provided arguments.
	 */
	void include(String resource, Writer out, Map<String, ?> args) throws Exception;
}
