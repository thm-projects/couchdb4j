CouchDB4J
---------

This is a fork of the original [CouchDB4J library](https://github.com/mbreese/couchdb4j).
Since the old project has not seen any activity, we will update this library with the goal of introducing some new features.

CouchDB4J is originally developed by:

	Marcus R. Breese
	Fourspaces Consulting, LLC.
	mbreese@gmail.com

Introduction
------------
After looking into CouchDB, I attempted to use the existing couchdb-lib project from
egor.margineanu on GoogleCode.  However, the original CouchDB used an XML document 
format, but the latest uses JSON for it's document format.  The URL schemes have also 
changed since then.

So, since that wasn't going to work, I set about to write my own.

Usage
-----
Like CouchDB itself, the API is fairly simple.  There are 5 main objects:
Session, Database, Document, View, and ViewResult.  The session is the main connection 
to the CouchDB server.  You retrieve database instances from the Session.  You can
get/create/update Documents to a Database.  You can execute a View on a Database
to get your results in the form of a ViewResult (which is actually a type of Document).

Session
-------
Using a Session, you can list, get, create, and delete databases.

Database
--------
Using a Database, you can create, get, delete, and update Documents.  You can also
use them to perform View operations that return ViewResults.

Documents
---------
Documents are JSONObject backed.  They contain a JSONObject, and that is how all of their
properties are stored.  JSON-lib.sf.net's JSONObject implementation is quite nice in that
it implements java.util.Map, so it is easy to .put(key,value), and .get(key) properties.

Additionally, you can transfer data to and from JavaBeans with the JSON-lib.sf.net library.
(see: http://json-lib.sourceforge.net/usage.html)

View
----
A view is a javascript function that is executed to filter your documents on the server.  Since
they don't have to return full documents, the View returns a ViewResult.  From the ViewResult, you can
retrieve a List<Document> of what the view returns.  From these Documents, you can retrieve the full 
Document from your database, if you wish.

To explain it in SQL terms, you can retrieve as much or as little of a row as you want in each query.
A Document is like the complete row of a table, and the View is like a SQL SELECT. (This is a gross
over simplification, but you get the idea.



Example
-------
```
Session s = new Session("localhost",5984);
Database db = s.getDatabase("foodb");

Document doc = db.getDocument("documentid1234");
doc.put("foo","bar");
db.saveDocument(doc);

Document newdoc = new Document();
newdoc.put("foo","baz");
db.saveDocument(newdoc); // auto-generated id given by the database

// Running a view that returns results of the expected type
View view = new View("design_doc/view_name");
// The view returns contents that resemble the structure of a class named `Entity`
Results<Entity> result = db.queryView(view, Entity.class);
for (RowResult<Entity> row : result.getRows()) {
	Entity e = row.getValue();
	// Do something with `e` ...
}

// Running a view (legacy method)
ViewResult result = db.getAllDocuments(); // same as db.view("_all_dbs");
for (Document d: result.getResults()) {
	System.out.println(d.getId());
	/*
		ViewResults don't actually contain the full document, only what the view
		returned.  So, in order to get the full document, you need to request a
		new copy from the database.
	*/
	Document full = db.getDocument(d.getId());
}

// Ad-Hoc view
ViewResult resultAdHoc = db.adhoc("function (doc) { if (doc.foo=='bar') { emit(null, doc); }}");
```

License
-------
CouchDB4J is licensed under the terms of the Apache 2.0 license as listed in the 
LICENSE.TXT file.

Testing
---------
If you are running tests against a CouchDB instance that isn't on `localhost:5984`, you need to copy the
contents of the `src/test/couchdb-test.properties.default` file and save it without the `.default` suffix.
This file is kept out of the repository, so you can safely make changes to it without affecting other developers.
