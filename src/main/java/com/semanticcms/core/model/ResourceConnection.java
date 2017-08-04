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

import com.aoindustries.lang.NullArgumentException;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * An active connection to a resource within a book.
 *
 * TODO: interface + abstract base, or default interface methods once on Java 1.8?
 *
 * TODO: Is this thread safe?
 *
 * @see  Resource#open
 */
abstract public class ResourceConnection implements Closeable {

	private final Resource resource; // TODO: Worth having this reference back to resource?

	public ResourceConnection(Resource resource) {
		this.resource = NullArgumentException.checkNotNull(resource, "resource");
	}

	@Override
	public String toString() {
		return resource.toString() + " (connection)";
	}

	/**
	 * The {@link Resource} that provides this connection.
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Checks if this resource exists.
	 *
	 * @throws  IOException  if I/O error occurs
	 */
	abstract public boolean exists() throws IOException;

	/**
	 * Gets the length of this resource or {@code -1} if unknown.
	 *
	 * @throws  IOException  if I/O error occurs
	 * @throws  FileNotFoundException  if resource does not exist (see {@link #exists()})
	 */
	abstract public long getLength() throws IOException, FileNotFoundException;

	/**
	 * Gets the last modified time of this resource or {@code 0} if unknown.
	 *
	 * @throws  IOException  if I/O error occurs
	 * @throws  FileNotFoundException  if resource does not exist (see {@link #exists()})
	 */
	abstract public long getLastModified() throws IOException, FileNotFoundException;

	/**
	 * Opens this resource for reading.
	 * TODO: Do we support multiple input streams per connection, or just one?
	 *
	 * @throws  IOException  if I/O error occurs
	 * @throws  FileNotFoundException  if resource does not exist (see {@link #exists()})
	 */
	abstract public InputStream getInputStream() throws IOException, FileNotFoundException;

	/**
	 * Gets a {@link File} for this resource.  When the resource exists locally, this will
	 * be a direct reference to the resource.  When the resource exists remotely, this may
	 * require fetching the resource contents into a temporary file.  To allow prompt cleanup
	 * of any temporary files, {@link ResourceFile#close()} when done with the file.
	 * <p>
	 * Use this when having a {@link File} is a hard requirement, and not merely a convenience
	 * or optimization.  Use {@link #getFile()} when a {@link File} is optional.
	 * </p>
	 *
	 * @throws  IOException  if I/O error occurs
	 * @throws  FileNotFoundException  if resource does not exist (see {@link #exists()})
	 *
	 * @see  #getFile()
	 * @see  #close()
	 */
	abstract public ResourceFile getResourceFile() throws IOException, FileNotFoundException;

	/**
	 * Closes access to this resource.
	 * Also closes any {@link InputStream} or {@link ResourceFile} opened with this resource.
	 *
	 * @see  #getResourceFile()
	 */
	@Override
	abstract public void close() throws IOException;
}
