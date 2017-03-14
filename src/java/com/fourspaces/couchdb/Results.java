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
package com.fourspaces.couchdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class Results<T> {

	private int totalRows;

	private int offset;

	private List<RowResult<T>> rows;

	public Results(String body, Class<T> klass) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JavaType rowResultType = mapper.getTypeFactory().constructParametrizedType(RowResult.class, RowResult.class, klass);
			JavaType listType = mapper.getTypeFactory().constructParametrizedType(List.class, List.class, rowResultType);
			JsonNode node = mapper.readTree(body);
			this.totalRows = node.get("total_rows").asInt();
			this.offset = node.get("offset").asInt();
			this.rows = mapper.readValue(node.get("rows").toString(), listType);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int getTotalRows() {
		return this.totalRows;
	}

	public int getOffset() {
		return this.offset;
	}

	public List<RowResult<T>> getRows() {
		return this.rows;
	}

}
