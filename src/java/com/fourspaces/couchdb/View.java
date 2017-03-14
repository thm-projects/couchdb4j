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

package com.fourspaces.couchdb;

import net.sf.json.util.JSONUtils;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * The View is the mechanism for performing Querys on a CouchDB instance. The view can be named or ad-hoc (see
 * AdHocView). (Currently [14 Sept 2007] named view aren't working in the mainline CouchDB code... but this _should_
 * work.)
 * <p>
 * The View object exists mainly to apply filtering to the view. Otherwise, views can be called directly from the
 * database object by using their names (or given an ad-hoc query).
 *
 * @author mbreese
 *
 */
public class View {

	public enum StaleMode {
		NONE, OK, UPDATE_AFTER
	}

	protected String key;
	protected String keys;
	protected String startKey;
	protected String endKey;
	protected Integer limit;
	protected Boolean update;
	protected Boolean reverse;
	protected int skip;
	protected Boolean group;
	protected boolean includeDocs = false;
	protected StaleMode stale = StaleMode.NONE;

	protected String name;
	protected Document document;
	protected String function;

	/**
	 * Build a view given a document and a name
	 *
	 * @param doc
	 * @param name
	 */
	public View(Document doc, String name) {
		this.document = doc;
		this.name = name;
	}

	/**
	 * Build a view given only a fullname ex: ("_add_docs", "_temp_view")
	 *
	 * @param fullname
	 */
	public View(String fullname) {
		this.name = fullname;
		this.document = null;
	}

	/**
	 * Builds a new view for a document, a given name, and the function definition. This <i>does not actually add it to
	 * the document</i>. That is handled by Document.addView()
	 * <p>
	 * This constructor should only be called by Document.addView();
	 *
	 * @param doc
	 * @param name
	 * @param function
	 */
	View(Document doc, String name, String function) {
		this.name = name;
		this.document = doc;
		this.function = function;
	}

	/**
	 * Based upon settings, builds the queryString to add to the URL for this view.
	 *
	 *
	 * @return
	 */
	public String getQueryString() {
		String queryString = "";
		if (key != null) {
			if (!queryString.equals("")) {
				queryString += "&";
			}
			queryString += "key=" + key;
		}
		if (startKey != null) {
			if (!queryString.equals("")) {
				queryString += "&";
			}
			queryString += "startkey=" + startKey;
		}
		if (endKey != null) {
			if (!queryString.equals("")) {
				queryString += "&";
			}
			queryString += "endkey=" + endKey;
		}
		if (skip != 0) {
			if (!queryString.equals("")) {
				queryString += "&";
			}
			queryString += "skip=" + skip;
		}
		if (limit != null) {
			if (!queryString.equals("")) {
				queryString += "&";
			}
			queryString += "limit=" + limit;
		}
		if (update != null && update.booleanValue()) {
			if (!queryString.equals("")) {
				queryString += "&";
			}
			queryString += "update=true";
		}
		if (includeDocs != false) {
			if (queryString.length() > 0) {
				queryString += "&";
			}
			queryString += "include_docs=true";
		}
		if (reverse != null && reverse.booleanValue()) {
			if (!queryString.equals("")) {
				queryString += "&";
			}
			queryString += "descending=true";
		}
		if (group != null && group.booleanValue()) {
			if (!queryString.equals("")) {
				queryString += "&";
			}
			queryString += "group=true";
		}
		if (keys != null) {
			if (!queryString.equals("")) {
				queryString += "&";
			}
			queryString += "keys=" + keys;
		}
		if (stale != null && stale != StaleMode.NONE) {
			if (!queryString.equals("")) {
				queryString += "&";
			}
			if (stale == StaleMode.OK) {
				queryString += "stale=ok";
			} else if (stale == StaleMode.UPDATE_AFTER) {
				queryString += "stale=update_after";
			}
		}
		return queryString.equals("") ? null : queryString;

	}

	/**
	 * The number of entries to return
	 *
	 * @param count
	 * @deprecated CouchDB 0.9 uses limit instead
	 */
	@Deprecated
	public void setCount(Integer count) {
		// this.count = count;
		setLimit(count);
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public void setGroup(Boolean group) {
		this.group = group;
	}

	/**
	 * Reverse the listing
	 *
	 * @param reverse
	 * @deprecated CouchDB 0.9 uses "descending" instead
	 */
	@Deprecated
	public void setReverse(Boolean reverse) {
		this.reverse = reverse;
	}

	public void setDescending(Boolean descending) {
		this.reverse = descending;
	}

	/**
	 * Skip listing these keys (not sure if this works, or the format)
	 *
	 * @param skip
	 */
	public void setSkip(int skip) {
		this.skip = skip;
	}

	/**
	 * Not sure... might be for batch updates, but not sure.
	 *
	 * @param update
	 */
	public void setUpdate(Boolean update) {
		this.update = update;
	}

	/**
	 * @deprecated Use setIncludeDocs instead.
	 * @param withDocs
	 */
	@Deprecated
	public void setWithDocs(Boolean withDocs) {
		this.includeDocs = withDocs;
	}

	public void setIncludeDocs(boolean includeDocs) {
		this.includeDocs = includeDocs;
	}

	/**
	 * The name for this view (w/o doc id)
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * the full name for this view (w/ doc id, if avail) in the form of : "docid:name" or "name"
	 *
	 * @return
	 */
	public String getFullName() {
		return (document == null) ? name : document.getViewDocumentId() + "/" + name;
	}

	/**
	 * The function definition for this view, if it is available.
	 *
	 * @return
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * Sets the 'stale' parameter. StaleMode corresponds to the values 'ok' and 'update_after'.
	 * @param stale
	 */
	public void setStale(StaleMode stale) {
		this.stale = stale;
	}

	public void setStartKey(final String key) {
		startKey = quote(key);
	}

	public void setStartKey(final long key) {
		startKey = String.valueOf(key);
	}

	/**
	 * Use this method if you need to add a single key parameter inside an array.
	 * @param key
	 */
	public void setStartKeyArray(final String key) {
		if (isNumber(key)) {
			startKey = encode("[" + key + "]");
		} else {
			startKey = encode("[" + JSONUtils.quote(key) + "]");
		}
	}

	public void setStartKey(final String... keys) {
		startKey = toJsonArray(keys);
	}

	/**
	 * This is equivalent to calling setStartKey.
	 *
	 * We provide this method for convenience.
	 * @param keys
	 */
	public void setStartKeyArray(final String... keys) {
		this.setStartKey(keys);
	}

	/**
	 * Use this method if you need to add a single key parameter inside an array.
	 * @param key
	 */
	public void setEndKeyArray(final String key) {
		if (isNumber(key)) {
			endKey = encode("[" + key + "]");
		} else {
			endKey = encode("[" + JSONUtils.quote(key) + "]");
		}
	}

	/**
	 * This is equivalent to calling setEndKey.
	 *
	 * We provide this method for convenience.
	 * @param keys
	 */
	public void setEndKeyArray(final String... keys) {
		this.setEndKey(keys);
	}

	public void setEndKey(final String key) {
		endKey = quote(key);
	}

	public void setEndKey(final long key) {
		endKey = String.valueOf(key);
	}

	public void setEndKey(final String... keys) {
		endKey = toJsonArray(keys);
	}

	public void setKey(final String key) {
		this.key = quote(key);
	}

	public void setKey(final String... keys) {
		key = toJsonArray(keys);
	}

	/**
	 * Sets the 'keys' parameter. Instead of a single key, this parameter allows to specify multiple keys at once.
	 * @param keys
	 */
	public void setKeys(List<String> keys) {
		this.keys = toJsonArray(keys.toArray(new String[keys.size()]));
	}

	private String quote(final String string) {
		return encode(JSONUtils.quote(string));
	}

	private String encode(final String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			// Since we're using 'UTF-8', this Exception should never occur.
			return "";
		}
	}

	private String toJsonArray(final String[] strs) {
		final List<String> strings = new ArrayList<String>();
		for (final String string : strs) {
			if (isNumber(string) || isPlaceholder(string) || isArray(string)) {
				strings.add(string);
			} else {
				strings.add(JSONUtils.quote(string));
			}
		}
		return encode("[" + StringUtils.join(strings, ",") + "]");
	}

	private boolean isNumber(final String string) {
		return string.matches("^[0-9]+$");
	}

	private boolean isPlaceholder(final String string) {
		return string.equals("{}");
	}

	private boolean isArray(final String string) {
		return string.startsWith("[") && string.endsWith("]");
	}
}
