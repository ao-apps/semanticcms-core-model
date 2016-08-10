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

import com.aoindustries.util.StringUtility;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes node body content, while replacing nested child element markers with
 * child element content.
 *
 * TODO: Performance: It may be faster to deal with strings and char[] instead of sending
 *                    everything through as single characters.
 */
public class NodeBodyWriter extends Writer {

	private static final Logger logger = Logger.getLogger(NodeBodyWriter.class.getName());

	static final String MARKER_PREFIX        = "<A<O<ELEMENT ";
	private static final int    MARKER_PREFIX_LEN    = MARKER_PREFIX.length();
	private static final char[] MARKER_PREFIX_CHARS  = MARKER_PREFIX.toCharArray();
	static final String MARKER_SUFFIX        = ">O>A>";
	private static final int    MARKER_SUFFIX_LEN    = MARKER_SUFFIX.length();
	private static final char[] MARKER_SUFFIX_CHARS  = MARKER_SUFFIX.toCharArray();

	/** The number of hex characters in the 64-bit element key */
	private static final int ELEMENT_KEY_LEN = 16;
	
	public static void writeElementMarker(long elementKey, Appendable out) throws IOException {
		out.append(MARKER_PREFIX);
		StringUtility.convertToHex(elementKey, out);
		out.append(MARKER_SUFFIX);
	}

	private final Node node;
	private final Writer out;
	private final ElementContext context;
	private final char[] elementKeyBuffer = new char[ELEMENT_KEY_LEN];
	private int markerPos = 0;

	public NodeBodyWriter(Node node, Writer out, ElementContext context) {
		this.node = node;
		this.out = out;
		this.context = context;
	}

	/**
	 * Writes characters, ignores calls with {@code len < 0}
	 */
	private void writeCharsToOut(char[] cbuf, int len) throws IOException {
		if(len > 0) {
			if(len == 1) {
				out.write(cbuf[0]);
			} else {
				out.write(cbuf, 0, len);
			}
		}
	}

	@Override
	public void write(final int c) throws IOException {
		while(true) {
			if(markerPos < MARKER_PREFIX_LEN) {
				// Is in marker prefix
				if((char)c == MARKER_PREFIX_CHARS[markerPos]) {
					// Matches
					markerPos++;
					return;
				} else {
					// Mismatch
					if(markerPos > 0) {
						writeCharsToOut(MARKER_PREFIX_CHARS, markerPos);
						markerPos = 0;
					} else {
						out.write(c);
						return;
					}
				}
			} else if(markerPos < (MARKER_PREFIX_LEN + ELEMENT_KEY_LEN)) {
				// Is in element key
				int elementKeyPos = markerPos - MARKER_PREFIX_LEN;
				if(
					(c >= '0' && c <= '9')
					|| (c >= 'a' && c <= 'f')
				) {
					// Matches potential element key
					elementKeyBuffer[elementKeyPos] = (char)c;
					markerPos++;
					return;
				} else {
					// Mismatch
					out.write(MARKER_PREFIX_CHARS, 0, MARKER_PREFIX_LEN);
					writeCharsToOut(elementKeyBuffer, elementKeyPos);
					markerPos = 0;
				}
			} else {
				// Is in marker suffix
				int markerSuffixPos = markerPos - (MARKER_PREFIX_LEN + ELEMENT_KEY_LEN);
				if((char)c == MARKER_SUFFIX_CHARS[markerSuffixPos]) {
					// Matches
					markerPos++;
					if(markerPos == (MARKER_PREFIX_LEN + ELEMENT_KEY_LEN + MARKER_SUFFIX_LEN)) {
						// Entire marker found
						long elementKey = StringUtility.convertLongArrayFromHex(elementKeyBuffer);
						ElementWriter elementWriter = node.getElementWriter(elementKey);
						if(elementWriter != null) {
							// Substitute child element
							elementWriter.writeTo(out, context);
						} else {
							if(logger.isLoggable(Level.WARNING)) {
								logger.warning("ElementWriter not found by key: " + String.valueOf(elementKeyBuffer) + " in " + node);
							}
							// Mismatch
							out.write(MARKER_PREFIX_CHARS, 0, MARKER_PREFIX_LEN);
							out.write(elementKeyBuffer, 0, ELEMENT_KEY_LEN);
							out.write(MARKER_SUFFIX_CHARS, 0, MARKER_SUFFIX_LEN);
						}
						markerPos = 0;
					}
					return;
				} else {
					// Mismatch
					out.write(MARKER_PREFIX_CHARS, 0, MARKER_PREFIX_LEN);
					out.write(elementKeyBuffer, 0, ELEMENT_KEY_LEN);
					writeCharsToOut(elementKeyBuffer, markerSuffixPos);
					markerPos = 0;
				}
			}
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		while(len > 0) {
			write(cbuf[off]);
			off++;
			len--;
		}
}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		if(markerPos > 0) {
			// Flush any unwritten
			writeCharsToOut(MARKER_PREFIX_CHARS, Math.min(markerPos, MARKER_PREFIX_LEN));
			writeCharsToOut(elementKeyBuffer,    Math.min(markerPos - MARKER_PREFIX_LEN, ELEMENT_KEY_LEN));
			writeCharsToOut(MARKER_SUFFIX_CHARS,          markerPos - (MARKER_PREFIX_LEN + ELEMENT_KEY_LEN));
			markerPos = 0;
			out.flush();
		}
		out.close();
	}
}
