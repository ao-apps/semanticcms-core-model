/*
 * semanticcms-core-model - Java API for modeling web page content and relationships.
 * Copyright (C) 2017  AO Industries, Inc.
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

import java.io.IOException;

/**
 * Gets {@link Resource resources} given their {@link ResourceRef references}.
 *
 * @see  Resource
 * @see  ResourceRef
 */
public interface ResourceStore {

	/**
	 * Gets a {@link Resource} for the given {@link ResourceRef}.
	 * The resource may or may not {@link Resource#exists() exist}.
	 *
	 * @throws  IOException  if I/O error occurs
	 */
	Resource getResource(ResourceRef resourceRef) throws IOException;
}
