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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class RowResult<V> {

	private String id;

	@JsonDeserialize(using = RowKeyDeserializer.class)
	private RowKey key;

	private V value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public RowKey getKey() {
		return key;
	}

	public void setKey(RowKey key) {
		this.key = key;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}
}
