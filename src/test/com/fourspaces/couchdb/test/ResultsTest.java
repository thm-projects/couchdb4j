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
import java.util.List;

import net.sf.json.JSONObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Results;
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
		String json = "{\"_id\": \"_design/test\", \"language\": \"javascript\", \"views\": {\"complex_keys\": {\"map\":\" function (doc) { emit([doc.aString, doc.aNumber], doc); }\" } } }";
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
		db.saveDocument(d);
		db.saveDocument(d);

		View view = new View("test/complex_keys");
		Results<Object> actual = db.queryView(view);
		for (RowResult<Object> row : actual.getRows()) {
			/*
			 * The idea is to generate either a list of keys or a single key
			 * based on the json content of the view.
			 */
			List<Object> key = row.getKey();
			assertEquals(key.get(0), "this is a string");
			assertEquals(key.get(1), 42);
		}
	}
}
