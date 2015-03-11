/*
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

import static org.junit.Assert.*;

import java.io.IOException;
import net.sf.json.JSONObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Results;
import com.fourspaces.couchdb.RowKey;
import com.fourspaces.couchdb.RowResult;
import com.fourspaces.couchdb.Session;
import com.fourspaces.couchdb.View;

public class ResultsTest {

	private final Session session = TestSession.getTestSession();
	private Database db;

	@Before
	public void createTestDB() throws Exception {
		session.createDatabase("foo");
		db = session.getDatabase("foo");
		String json = "{\"_id\": \"_design/test\", \"language\": \"javascript\", \"views\": {\"array_keys\": {\"map\":\" function (doc) { emit([doc.aString, doc.aNumber], doc); }\" }, \"single_keys\": {\"map\":\" function (doc) { emit(doc.aString, doc); }\" } } } }";
		JSONObject obj = JSONObject.fromObject(json);
		db.saveDocument(new Document(obj));
	}

	@After
	public void removeTestDB() {
		session.deleteDatabase("foo");
	}

	@Test
	public void shouldHandleArrayKeys() throws IOException {
		Document d = new Document();
		d.put("aString", "this is a string");
		d.put("aNumber", 42);
		d.put("other", "stuff");
		db.saveDocument(d);

		View view = new View("test/array_keys");
		Results<Object> result = db.queryView(view, Object.class);
		for (RowResult<Object> row : result.getRows()) {
			RowKey key = row.getKey();
			assertEquals(key.getString(0), "this is a string");
			assertEquals(key.getInt(1), 42);
		}
	}

	@Test
	public void shouldHandleSingleKey() throws IOException {
		Document d = new Document();
		d.put("aString", "this is a string");
		d.put("aNumber", 42);
		d.put("other", "stuff");
		db.saveDocument(d);

		View view = new View("test/single_keys");
		Results<Object> result = db.queryView(view, Object.class);
		for (RowResult<Object> row : result.getRows()) {
			RowKey key = row.getKey();
			assertEquals(key.getString(), "this is a string");
		}
	}

	@Test
	public void shouldDeserializeValueToGivenObjectType() throws IOException {
		Document d = new Document();
		d.put("aString", "this is a string");
		d.put("aNumber", 42);
		d.put("other", "stuff");
		db.saveDocument(d);

		View view = new View("test/single_keys");
		Results<Entity> result = db.queryView(view, Entity.class);
		for (RowResult<Entity> row : result.getRows()) {
			Entity e = row.getValue();
			assertEquals(e.aString, "this is a string");
		}
	}

	@Test
	public void shouldSetViewMetadata() throws IOException {
		db.saveDocument(new Document());
		db.saveDocument(new Document());
		db.saveDocument(new Document());
		db.saveDocument(new Document());
		db.saveDocument(new Document());

		View view = new View("test/single_keys");
		view.setSkip(2);
		Results<Object> actual = db.queryView(view, Object.class);
		assertEquals(5, actual.getTotalRows());
		assertEquals(2, actual.getOffset());
	}

	public static class Entity {

		public String _id;

		public String _rev;

		public String aString;

		public int aNumber;

		public String other;
	}
}
