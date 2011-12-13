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
package com.mercatis.lighthouse3.base.getterchain;

/**
 * A getter is responsible for retrieving a sought property
 */
public interface Getter {
	
	/**This will initialize the getter with it baseClass and the property that is sought
	 * 
	 *  for the property the must exist a public "getter" in the form of
	 *  
	 *  getPropertyName, e.g. If you're looking for the property
	 *  
	 *  name in the class Person, that class must contain a public method 
	 *  that takes no arguments and has the name getName
	 * 
	 *  The method is called before
	 *  getPropery or getRusultClass
	 *  
	 *  
	 * @param baseClass the class this getter is "working on"
	 * @param propertyName the name of the  property which is sought
	 */
	public void initianlizeGetter(Class<?> baseClass, String propertyName);
	
	/**Get the specified property of the given object instance
	 * according to how the {@link Getter} was initialized
	 * 
	 * @param pObject
	 * @return
	 */
	public Object getProperty(Object pObject);
	
	/**Returns the class of the property
	 * @return
	 */
	public Class<?> getResultClass();
}
