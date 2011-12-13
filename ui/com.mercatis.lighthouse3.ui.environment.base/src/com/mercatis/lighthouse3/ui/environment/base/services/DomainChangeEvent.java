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
package com.mercatis.lighthouse3.ui.environment.base.services;

import java.util.EventObject;

import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class DomainChangeEvent extends EventObject {

	private static final long serialVersionUID = -8943608070832478011L;
	
	private String property;
	
	private Object oldValue;
	
	private Object newValue;

	public DomainChangeEvent(LighthouseDomain lighthouseDomain, Object source, String property, Object oldValue, Object newValue) {
		super(source);
		this.property = property;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * @return the property
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * @return the oldValue
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * @return the newValue
	 */
	public Object getNewValue() {
		return newValue;
	}

}
