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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourspaces.couchdb.structure.DocumentResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

/**
 * The CouchResponse parses the HTTP response returned by the CouchDB server. This is almost never called directly by
 * the user, but indirectly through the Session and Database objects.
 * <p>
 * Given a CouchDB response, it will determine if the request was successful (status 200,201,202), or was an error. If
 * there was an error, it parses the returned json error message.
 *
 * @author mbreese
 *
 */
public class CouchResponse {
	Log log = LogFactory.getLog(CouchResponse.class);

	private String body;
	private String path;
	private Header[] headers;
	private int statusCode;
	private String methodName;
	boolean ok = false;

	private String error_id;
	private String error_reason;

	/**
	 * C-tor parses the method results to build the CouchResponse object. First, it reads the body (hence the
	 * IOException) from the method Next, it checks the status codes to determine if the request was successful. If
	 * there was an error, it parses the error codes.
	 *
	 * @param method
	 * @throws IOException
	 */
	CouchResponse(HttpRequestBase req, HttpResponse response) throws IOException {
		headers = response.getAllHeaders();
		HttpEntity entity = response.getEntity();
		body = EntityUtils.toString(entity);
		path = req.getURI().getPath();
		statusCode = response.getStatusLine().getStatusCode();

		boolean isGet = (req instanceof HttpGet);
		boolean isPut = (req instanceof HttpPut);
		boolean isPost = (req instanceof HttpPost);
		boolean isDelete = (req instanceof HttpDelete);

		boolean okay = statusCode == 200;
		boolean created = statusCode == 201;
		boolean accepted = statusCode == 202;
		boolean notfound = statusCode == 404;
		boolean conflict = statusCode == 409;

		ObjectMapper mapper = new ObjectMapper();
		if ((isGet && notfound) || (isPut && conflict) || (isPost && notfound) || (isDelete && notfound)) {
			DocumentResponse error = mapper.readValue(body, DocumentResponse.class);
			error_id = error.getError();
			error_reason = error.getReason();
		} else if ((isPut && created) || (isPost && created) || (isDelete && accepted) || (isDelete && okay)) {
			if (path.endsWith("_bulk_docs")) {
				// Handle bulk doc update differently
				@SuppressWarnings("unchecked")
				List<DocumentResponse> bulk = mapper.readValue(body, List.class);
				ok = bulk.size() > 0;
			} else {
				DocumentResponse modification = mapper.readValue(body, DocumentResponse.class);
				ok = modification.isOk();
			}
		} else if (isGet || (isPost && okay)) {
			ok = true;
		}
		log.debug(toString());
	}

	@Override
	/**
	 * A better toString for this object... can be very verbose though.
	 */
	public String toString() {
		return "[" + methodName + "] " + path + " [" + statusCode + "] " + " => " + body;
	}

	/**
	 * Retrieves the body of the request as a JSONArray object. (such as listing database names)
	 *
	 * @return
	 * @deprecated Use getBodyAsJsonNode instead
	 */
	@Deprecated
	public JSONArray getBodyAsJSONArray() {
		if (body == null) {
			return null;
		}
		return JSONArray.fromObject(body);
	}

	/**
	 * Was the request successful?
	 *
	 * @return
	 */
	public boolean isOk() {
		return ok;
	}

	/**
	 * What was the error id?
	 *
	 * @return
	 */
	public String getErrorId() {
		if (error_id != null) {
			return error_id;
		}
		return null;
	}

	/**
	 * what was the error reason given?
	 *
	 * @return
	 */
	public String getErrorReason() {
		if (error_reason != null) {
			return error_reason;
		}
		return null;
	}

	/**
	 * Returns the body of the response as a JSON Object (such as for a document)
	 *
	 * @return
	 * @deprecated Use getBodyAsJsonNode instead
	 */
	@Deprecated
	public JSONObject getBodyAsJSONObject() {
		if (body == null) {
			return null;
		}
		return JSONObject.fromObject(body);
	}

	/**
	 * Returns the body of the response as a Jackson JsonNode (such as for a document)
	 *
	 * @return
	 */
	public JsonNode getBodyAsJsonNode() throws IOException {
		if (body == null) {
			return null;
		}

		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(body);
	}

	/**
	 * Retrieves a specific header from the response (not really used anymore)
	 *
	 * @param key
	 * @return
	 */
	public String getHeader(String key) {
		for (Header h : headers) {
			if (h.getName().equals(key)) {
				return h.getValue();
			}
		}
		return null;
	}

	public String getBody() {
		return body;
	}
}
