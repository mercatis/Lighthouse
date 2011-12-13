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
package com.mercatis.lighthouse3.ui.security.internal;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;

import com.mercatis.lighthouse3.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.ContextAdapter;


public class ContextRolesCredentialPropertyTester extends PropertyTester {
	
	private IAdapterManager adapterManager = Platform.getAdapterManager();
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		Assert.isTrue(args.length == 1, "ContextRolesCredentialTester expects exactly one argument.");
		Assert.isTrue(args[0] instanceof String, "ContextRolesCredentialTester expects a string argument.");
		Assert.isTrue(property.equals("role"), "ContextRolesCredentialTester expects property 'role'.");
		Assert.isTrue(expectedValue == null || expectedValue instanceof Boolean, "ExpectedValue must be either null or of type Boolean.");
		
		// if feed a Collection, only the first element is tested.
		Object tmp = null;
		if (receiver instanceof Collection) {
			Iterator<?> it = ((Collection<?>) receiver).iterator();
			if (it.hasNext()) {
				tmp = ((Collection<?>) receiver).iterator().next();
			} else {
				return false;
			}
		} else {
			tmp = receiver;
		}
		
		String role = (String) args[0];
		Boolean expectedBooleanValue = expectedValue == null ? Boolean.TRUE : (Boolean) expectedValue;
		ContextAdapter contextAdapter = (ContextAdapter) adapterManager.getAdapter(tmp, ContextAdapter.class);
		if (contextAdapter == null)
			return false;
		
		String context = (contextAdapter).toContext(tmp);
		return expectedBooleanValue.booleanValue() == CodeGuard.hasRole(context, role);
	}

}
