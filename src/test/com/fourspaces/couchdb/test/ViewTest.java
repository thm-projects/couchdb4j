/*
   Copyright 2007 Fourspaces Consulting, LLC
   Copyright 2015 The ARSnova Team

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.fourspaces.couchdb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fourspaces.couchdb.AdHocView;
import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import com.fourspaces.couchdb.View;
import com.fourspaces.couchdb.View.StaleMode;
import com.fourspaces.couchdb.ViewResults;

public class ViewTest {

	Log log = LogFactory.getLog(getClass());
	Session sess = TestSession.getTestSession();
	Database foo;

	@Before
	public void createTestDB() throws Exception {
		sess.createDatabase("foo");
		foo = sess.getDatabase("foo");
		Document d = new Document();
		d.put("foo", "bar");
		foo.saveDocument(d);
		log.debug("known id:" + d.getId());
		log.debug(foo.getDocument(d.getId()));

		foo.saveDocument(new Document());
		foo.saveDocument(new Document());
		foo.saveDocument(new Document());
		foo.saveDocument(new Document());
	}

	@After
	public void removeTestDB() {
		sess.deleteDatabase("foo");
	}

	@Test
	public void adhoc() {
		int all = foo.getAllDocuments().getResults().size();
		ViewResults results = foo.adhoc("function (doc) {emit(null, doc);}");
		assertNotNull(results);
		int adhoc = foo.adhoc("function (doc) {emit(null, doc);}").getResults().size();
		assertEquals(all, adhoc);
	}

	/**
	 * Test to demonstrate that the query string isn't being passed for adhoc views.
	 */
	@Test
	public void adhoc_query_string() {
		// Establish that all results are returned with our view
		int all = foo.getAllDocuments().getResults().size();
		AdHocView view = new AdHocView("function (doc) { emit(null, doc);}");
		ViewResults results = foo.adhoc(view);
		assertNotNull(results);
		assertEquals("Expected results to return all records", all, results.getResults().size());

		// Now set a limit on the number of results that may be returned
		// and verify that only that number of results are returned.
		view.setLimit(2);
		results = foo.adhoc(view);

		assertNotNull(results);
		assertEquals("Expected a subset of records to be returned", 2, results.getResults().size());
	}

	@Test
	public void adhoc2() {
		int adhoc = foo.adhoc("function (doc){ if (doc.foo=='bar'){ emit(doc, doc)}}").getResults().size();
		assertEquals(1, adhoc);
	}

	@Test
	public void addNamed() throws Exception {
		Document d = new Document();
		d.put("foo", "bar");

		log.debug("Saving d");
		foo.saveDocument(d);

		Document d2 = new Document();
		// d2.put("foo","baz");
		// d2.addView("all_documents", "function (doc){ return doc; }");
		d2.addView("viewfoobar", "testview", "function (doc){ if (doc.foo=='bar'){ emit(null, doc); }}");
		log.debug("Saving d2 - " + d2.getId() + " - " + d2.toString());
		foo.saveDocument(d2);
		log.debug("Saved d2  - " + d2.getId() + " - " + d2.toString());
		System.err.println("Saved d2  - " + d2.getId() + " - " + d2.toString());
		Document d2_2 = foo.getDocument(d2.getId());
		log.debug("Saved d2_2 - " + d2_2.toString());

		assertNotNull(d2_2.getView("testview"));
		assertEquals(2, foo.view(d2.getView("testview")).getResults().size());

		foo.deleteDocument(d);
		foo.deleteDocument(d2);
	}

	@Test
	public void setKeyShouldAcceptSingleArgument() {
		final View v = new View(null);
		v.setKey("foo");
		assertEncodedEquals("key", "\"foo\"", v.getQueryString());
	}

	@Test
	public void setKeyShouldAcceptMultipleArgument() {
		final View v = new View(null);
		v.setKey("foo", "bar", "baz");
		assertEncodedEquals("key", "[\"foo\",\"bar\",\"baz\"]", v.getQueryString());
	}

	@Test
	public void setStartKeyShouldAcceptSingleArgument() {
		final View v = new View(null);
		v.setStartKey("foo");
		assertEncodedEquals("startkey", "\"foo\"", v.getQueryString());
	}

	@Test
	public void setStartKeyShouldAcceptSingleArgumentArray() {
		final View v = new View(null);
		v.setStartKeyArray("foo");
		assertEncodedEquals("startkey", "[\"foo\"]", v.getQueryString());
	}

	@Test
	public void setEndKeyShouldAcceptSingleArgumentArray() {
		final View v = new View(null);
		v.setEndKeyArray("foo");
		assertEncodedEquals("endkey", "[\"foo\"]", v.getQueryString());
	}

	@Test
	public void setEndKeyShouldAcceptSingleArgument() {
		final View v = new View(null);
		v.setEndKey("foo");
		assertEncodedEquals("endkey", "\"foo\"", v.getQueryString());
	}

	@Test
	public void setStartKeyShouldAcceptMultipleArgument() {
		final View v = new View(null);
		v.setStartKey("foo", "bar", "baz");
		assertEncodedEquals("startkey", "[\"foo\",\"bar\",\"baz\"]", v.getQueryString());
	}

	@Test
	public void setEndKeyShouldAcceptMultipleArgument() {
		final View v = new View(null);
		v.setEndKey("foo", "bar", "baz");
		assertEncodedEquals("endkey", "[\"foo\",\"bar\",\"baz\"]", v.getQueryString());
	}

	@Test
	public void keysShouldSupportEmptyObject() {
		final View v = new View(null);
		v.setKey("foo", "bar", "{}");
		assertEncodedEquals("key", "[\"foo\",\"bar\",{}]", v.getQueryString());
	}

	@Test
	public void arrayKeysShouldNotEnquoteNumbers() {
		final View v = new View(null);
		v.setKey("foo", "bar", "2");
		assertEncodedEquals("key", "[\"foo\",\"bar\",2]", v.getQueryString());
	}

	@Test
	public void singleArrayKeysShouldNotEnquoteNumbers() {
		final View v1 = new View(null);
		final View v2 = new View(null);
		v1.setStartKeyArray("2");
		v2.setEndKeyArray("2");
		assertEncodedEquals("startkey", "[2]", v1.getQueryString());
		assertEncodedEquals("endkey", "[2]", v2.getQueryString());
	}

	@Test
	public void shouldSupportAddingKeysParameter() {
		String[] stringKeys = new String[] { "foo", "bar" };
		String[] numberKeys = new String[] { "123", "456" };
		String[] mixedKeys = new String[] { "foo", "123" };
		String[] arrayKeys = new String[] { "[\"foo\",123]", "[456,\"bar\"]" };
		String[] emptyKeys = new String[0];
		final View v1 = new View(null);
		final View v2 = new View(null);
		final View v3 = new View(null);
		final View v4 = new View(null);
		final View v5 = new View(null);
		v1.setKeys(Arrays.asList(stringKeys));
		v2.setKeys(Arrays.asList(numberKeys));
		v3.setKeys(Arrays.asList(mixedKeys));
		v4.setKeys(Arrays.asList(arrayKeys));
		v5.setKeys(Arrays.asList(emptyKeys));
		assertEncodedEquals("keys", "[\"foo\",\"bar\"]", v1.getQueryString());
		assertEncodedEquals("keys", "[123,456]", v2.getQueryString());
		assertEncodedEquals("keys", "[\"foo\",123]", v3.getQueryString());
		assertEncodedEquals("keys", "[[\"foo\",123],[456,\"bar\"]]", v4.getQueryString());
		assertEncodedEquals("keys", "[]", v5.getQueryString());
	}

	@Test
	public void shouldSupportStaleViews() {
		final View v1 = new View(null);
		final View v2 = new View(null);
		final View v3 = new View(null);
		final View v4 = new View(null);
		v1.setStale(StaleMode.NONE);
		v2.setStale(StaleMode.OK);
		v3.setStale(StaleMode.UPDATE_AFTER);
		assertNull(v1.getQueryString());
		assertEncodedEquals("stale", "ok", v2.getQueryString());
		assertEncodedEquals("stale", "update_after", v3.getQueryString());
		assertNull(v4.getQueryString());
	}

	@Test
	public void shouldSupportIncludeDocsParameter() {
		final View v1 = new View(null);
		final View v2 = new View(null);
		final View v3 = new View(null);
		v1.setIncludeDocs(true);
		v2.setIncludeDocs(false);
		assertEncodedEquals("include_docs", "true", v1.getQueryString());
		assertNull(v2.getQueryString());
		assertNull(v3.getQueryString());
	}

	@Test
	public void shouldQuoteKeyParameters() {
		final String aStringWithQuotes = "\"test\"";
		final String aStringWithOneQuote = "te\"st";
		final View v1 = new View(null);
		final View v2 = new View(null);
		final View v3 = new View(null);
		final View v4 = new View(null);
		v1.setKey(aStringWithQuotes);
		v2.setKey(aStringWithOneQuote, aStringWithQuotes);
		v3.setStartKeyArray(aStringWithQuotes);
		v4.setEndKeyArray(aStringWithQuotes);
		assertEncodedEquals("key", "\"\\\"test\\\"\"", v1.getQueryString());
		assertEncodedEquals("key", "[\"te\\\"st\",\"\\\"test\\\"\"]", v2.getQueryString());
		assertEncodedEquals("startkey", "[\"\\\"test\\\"\"]", v3.getQueryString());
		assertEncodedEquals("endkey", "[\"\\\"test\\\"\"]", v4.getQueryString());
	}

	private void assertEncodedEquals(final String key, final String expected, final String actual) {
		try {
			assertEquals(key + "=" + URLEncoder.encode(expected, "UTF-8"), actual);
		} catch (final UnsupportedEncodingException e) {
			fail(e.getLocalizedMessage());
		}
	}

}
