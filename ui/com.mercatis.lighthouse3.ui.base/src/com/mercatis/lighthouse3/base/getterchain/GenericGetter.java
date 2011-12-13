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

import java.lang.reflect.Method;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.mercatis.lighthouse3.base.UIBase;

/**
 * The generic getter retrieves properties for any object
 */
public class GenericGetter implements Getter {
	

	private Class<?> baseClass;
	private String methodName;
	
	public GenericGetter() {
	}
	
	/* (non-Javadoc)
	 * @see com.mercatis.alpiq.dealmanager.ui.common.getterchain.Getter#getProperty(java.lang.Object)
	 */
	public Object getProperty(Object pObject) {
		try {
			return baseClass.getDeclaredMethod(methodName).invoke(pObject);
		} catch (Exception e) {
			UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, e.getMessage(), e));
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.alpiq.dealmanager.ui.common.getterchain.Getter#getResultClass()
	 */
	public Class<?> getResultClass() {
		try {
			Method m = baseClass.getDeclaredMethod(methodName);
			return m.getReturnType();
		} catch (Exception e) {
			UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, e.getMessage(), e));
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.mercatis.alpiq.dealmanager.ui.common.getterchain.Getter#initianlizeGetter(java.lang.Class, java.lang.String)
	 */
	public void initianlizeGetter(Class<?> baseClass, String propertyName) {
		this.baseClass = baseClass;
		String firstLetter = propertyName.substring(0, 1);
		String remainingPart = propertyName.substring(1, propertyName.length());
		methodName = "get" + firstLetter.toUpperCase() + remainingPart;
	}

	/**
	 * @return the baseClass
	 */
	public Class<?> getBaseClass() {
		return baseClass;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

}
