/*
 * Copyright 2011 mercatis Technologies AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package com.mercatis.lighthouse3.ui.operations.base.model;

import java.util.ArrayList;
import java.util.List;

import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class Category<T> implements Comparable<Category<T>> {
	
	private LighthouseDomain lighthouseDomain;
	private String category;
	private List<T> operations;
	
	public Category(LighthouseDomain lighthouseDomain, String category) {
		this.lighthouseDomain = lighthouseDomain;
		this.category = category;
	}
	
	public Category(LighthouseDomain lighthouseDomain, String category, List<T> operations) {
		this(lighthouseDomain, category);
		this.operations = operations;
	}
	
	public LighthouseDomain getLighthouseDomain() {
		return this.lighthouseDomain;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public List<T> getOperations() {
		if (operations == null) {
			operations = new ArrayList<T>();
		}
		return operations;
	}
	
	public void addOperation(T operation) {
		this.getOperations().add(operation);
	}

	public int compareTo(Category<T> o) {
		return this.getCategory().compareTo(o.getCategory());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof Category<?>)) {
			return false;
		}
		return ((Category<?>)obj).category.equals(this.category);
	}
}
