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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.mercatis.lighthouse3.base.UIBase;

/**
 * A GetterChain which allows to retrieve 
 * properties form objects by specifying their relative path
 */
public class GetterChain {
	private Class<?> baseClass;
	private List<Getter> getterChain;
	private Map<Class<?>, Class< ? extends Getter>> getterRegistry;
	
	/**
	 * 
	 */
	public GetterChain() {
		getterRegistry = new HashMap<Class<?>, Class< ? extends Getter>>();
		getterChain = new LinkedList<Getter>();
	}
	
	/**Register a class <i>getterClass</i> that implements {@link Getter} which will 
	 * be used to retrieve properties in instances of class <i>baseClass</i>
	 *
	 * You must register getter for each class which is in the property-path
	 * before you call {@link buildGetterChain}
	 * 
	 * See {@link buildGetterChain} for more details on the property-path
	 * 
	 * @param baseClass
	 * @param getterClass
	 */
	public void registerGetter(Class<?> baseClass, Class< ? extends Getter> getterClass) {
		getterRegistry.put(baseClass, getterClass);
	}
	
	/**This method build up the actual getter chain.
	 * 
	 * Given a <i>baseClass</i> and a <i>propertyPath</i> the GetterChain is build.
	 * 
	 * The property path is separated by "." 
	 * For example. Let's assume you have an object Person with two properties called name and address
	 * The property address itself is another object which has two properties; city and street 
	 *  
	 * Person
	 * 		name
	 * 		address
	 * Address
	 * 		city
	 * 		street
	 * 
	 * If you want to retrieve the street a person lives in, the 
	 * 
	 * <i>baseClass</i> = Person.class
	 * and the
	 * <i>propertyPath</> = address.street
	 * 
	 * If you want to retrieve the name of a person, the
	 * 
	 * <i>baseClass</i> = Person.class
	 * and the
	 * <i>propertyPath</> = name
	 * 
	 * Before you call this method {@link Getter} have to be registered for the Person and Address class
	 * 
	 * @param baseClass
	 * @param propertyPath
	 */
	public void buildGetterChain(Class<?> baseClass, String propertyPath) {
		getterChain.removeAll(getterChain);
		this.baseClass = baseClass;
		Class<?> tBase = baseClass;
		String tRemainingAccessPath = propertyPath;
		String tPropName = "";
		while ( tRemainingAccessPath != null ) {
			if(tRemainingAccessPath.contains(".")) {
				int tDotIndex = tRemainingAccessPath.indexOf(".");
				tPropName = tRemainingAccessPath.substring(0, tDotIndex);
				tRemainingAccessPath = tRemainingAccessPath.substring(tDotIndex+1);
			} else {
				tPropName = tRemainingAccessPath;
				tRemainingAccessPath = null;
			}
	
			Getter tGetter = getGetter(tBase, tPropName);
			if(tGetter == null)
				throw new RuntimeException("Found no getter for class: " + tBase);
			getterChain.add(tGetter);
			tBase = tGetter.getResultClass();
		}
	}

	/**This method returns the value of the property for a given object instance.
	 * 
	 * The object instance must be of the same type as previously used, for building up the GetterChain.
	 * The returned object is the value which was retrieved for the property-path, which was used to call the {@link buildGetterChain} 
	 * 
	 * @param pObject
	 * @return
	 */
	public Object getProperty(Object pObject) {
		if(!baseClass.isInstance(pObject))
			throw new RuntimeException("Given object instance is not of same type as built up GetterChain");
		Object tRef = pObject;
		for (Getter tGetter : getterChain) {
			tRef = tGetter.getProperty(tRef);
		}
		return tRef;
	}
	
	/**
	 * @param baseClass
	 * @param propertyName
	 * @return
	 */
	private Getter getGetter(Class<?> baseClass, String propertyName) {
		Class< ? extends Getter> getterClass = getterRegistry.get(baseClass);
		Getter getter = null;
		try {
			getter = getterClass.newInstance();
			getter.initianlizeGetter(baseClass, propertyName);
		} catch (Exception e) {
			UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, e.getMessage(), e));
		}
		return getter;
	}
}
