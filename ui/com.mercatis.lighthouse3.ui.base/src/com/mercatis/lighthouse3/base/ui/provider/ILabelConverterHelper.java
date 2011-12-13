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
package com.mercatis.lighthouse3.base.ui.provider;

/**
 * Implement and register to the plugin.xml to provide labels to any kind of entity.
 * 
 */
public interface ILabelConverterHelper {
	
	/**
	 * The LabelConverter will ask this provider to generate a label for the given object.
	 * 
	 * @param obj
	 * @return label
	 */
	public String getLabelForObject(Object obj);
}
