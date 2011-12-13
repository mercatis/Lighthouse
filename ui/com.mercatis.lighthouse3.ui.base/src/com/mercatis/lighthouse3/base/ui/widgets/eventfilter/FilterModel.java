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
package com.mercatis.lighthouse3.base.ui.widgets.eventfilter;

import java.util.List;


public interface FilterModel {
	
	/**
	 * @param propertyIndex
	 * @return
	 */
	public boolean canRecur(int propertyIndex);

	/**
	 * @param <T>
	 * @param propertyIndex
	 * @return
	 */
	public <T extends Object> List<T> getChoicesFor(int propertyIndex);
	
	/**
	 * @param propertyIndex
	 * @return
	 */
	public InputControlType getInputControlTypeFor(int propertyIndex);
	
	/**
	 * @return
	 */
	public int getPropertyCount();
	
	/**
	 * @param propertyIndex
	 * @return
	 */
	public String getPropertyName(int propertyIndex);
	
	/**
	 * @param <T>
	 * @param propertyIndex
	 * @return
	 */
	public <T> List<T> getValuesFor(int propertyIndex);
	
	/**
	 * @param propertyIndex
	 * @return
	 */
	public boolean isMandatory(int propertyIndex);
	
	/**
	 * @param <T>
	 * @param values
	 * @param propertyIndex
	 */
	public <T> void setValuesFor(List<T> values, int propertyIndex);
	
	
	/**
	 * @param propertyIndex
	 * @return
	 */
	public <T> void setAllowedDeployments(List<T> deployments);

}
