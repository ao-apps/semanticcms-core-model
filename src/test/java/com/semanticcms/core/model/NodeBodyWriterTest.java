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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class NodeBodyWriterTest {

	private static final String TEST_BODY_PREFIX = "<TestNode>Test body <";
	private static final String TEST_ELEMENT_BODY = "<TestElement />";
	private static final String TEST_BODY_SUFFIX =
		"<" + NodeBodyWriter.MARKER_PREFIX + "ffffffffffffffff" + NodeBodyWriter.MARKER_SUFFIX
		+ "</TestNode>"
		+ NodeBodyWriter.MARKER_PREFIX + "ffffffff";

	private static final String TEST_EXPECTED_RESULT = TEST_BODY_PREFIX + TEST_ELEMENT_BODY + TEST_BODY_SUFFIX;

	private static Node testNode;
	private static String testNodeBody;

	// Java 1.8: Use lambda
	private static final ElementContext nullElementContext = new ElementContext() {
		@Override
		public void include(String resource, Writer out, Map<String,?> args) {
			// Do nothing
		}
	};

	@BeforeClass
	public static void setUpClass() throws IOException {
		testNode = new Node() {
			@Override
			public String getLabel() {
				return "Test Node";
			}
		};
		Long elementKey = testNode.addChildElement(
			new Element() {
				@Override
				public String getLabel() {
					return "Test Element";
				}
				@Override
				protected String getDefaultIdPrefix() {
					return "test";
				}
			},
			// Java 1.8: (out, context) -> out.write(TEST_ELEMENT_BODY)
			new ElementWriter() {
				@Override
				public void writeTo(Writer out, ElementContext context) throws IOException {
					out.write(TEST_ELEMENT_BODY);
				}
			}
		);
		StringBuilder SB = new StringBuilder();
		SB.append(TEST_BODY_PREFIX);
		NodeBodyWriter.writeElementMarker(elementKey, SB);
		SB.append(TEST_BODY_SUFFIX);
		testNodeBody = SB.toString();
	}

	@AfterClass
	public static void tearDownClass() {
		testNode = null;
	}

	@Test
	public void testWriteElementMarker() throws Exception {
		//System.out.println(testNodeBody);
		//System.out.flush();
		final char[] testNodeBodyChars = testNodeBody.toCharArray();
		final int testNodeBodyLen = testNodeBody.length();
		for(int writeLen = 1; writeLen <= testNodeBodyLen; writeLen++) {
			for(int off = 0; off < writeLen; off++) {
				StringWriter out = new StringWriter(TEST_EXPECTED_RESULT.length());
				try {
					NodeBodyWriter writer = new NodeBodyWriter(testNode, out, nullElementContext);
					try {
						writer.write(testNodeBodyChars, 0, off);
						for(int pos = off; pos < testNodeBodyLen; pos += writeLen) {
							int end = pos + writeLen;
							if(end > testNodeBodyLen) end = testNodeBodyLen;
							int len = end - pos;
							assertTrue(len >= 0);
							assertTrue((pos + len) <= testNodeBodyLen);
							writer.write(testNodeBodyChars, pos, len);
						}
					} finally {
						writer.close();
					}
				} finally {
					out.close();
				}
				assertEquals(TEST_EXPECTED_RESULT, out.toString());
			}
		}
	}
}
