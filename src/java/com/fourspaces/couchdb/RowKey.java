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

import java.util.List;

public class RowKey {

	private final List<Object> key;

	public RowKey(List<Object> rawKeys) {
		this.key = rawKeys;
	}

	public boolean getBoolean(int index) {
		return (Boolean) this.key.get(index);
	}

	public boolean getBoolean() {
		return this.getBoolean(0);
	}

	public int getInt(int index) {
		return (Integer) this.key.get(index);
	}

	public int getInt() {
		return this.getInt(0);
	}

	public long getLong(int index) {
		return (Long) this.key.get(index);
	}

	public long getLong() {
		return this.getLong(0);
	}

	public double getDouble(int index) {
		return (Double) this.key.get(index);
	}

	public double getDouble() {
		return this.getDouble(0);
	}

	public String getString(int index) {
		return (String) this.key.get(index);
	}

	public String getString() {
		return this.getString(0);
	}
}
