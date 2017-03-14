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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RowKeyDeserializer extends JsonDeserializer<RowKey> {

	@Override
	public RowKey deserialize(JsonParser p, DeserializationContext ctx) throws IOException, JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		TreeNode node = om.readTree(p);
		if (node.isArray()) {
			List<Object> rawKeys = om.readValue(node.toString(), new TypeReference<List<Object>>() {});
			return new RowKey(rawKeys);
		} else {
			Object rawKey = om.readValue(node.toString(), Object.class);
			List<Object> rawKeys = new ArrayList<Object>();
			rawKeys.add(rawKey);
			return new RowKey(rawKeys);
		}
	}

}
