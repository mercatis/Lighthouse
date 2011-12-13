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
package com.mercatis.lighthouse3.security.internal;

import org.eclipse.core.runtime.Assert;

import com.mercatis.lighthouse3.security.SecurityBinding;




public class ContextRolesCredentialTester extends org.eclipse.core.expressions.PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		Assert.isTrue(args.length == 1, "ContextRolesCredentialTester expects exactly one argument.");
		Assert.isTrue(args[0] instanceof String, "ContextRolesCredentialTester expects a string argument.");
		Assert.isTrue(property.equals("role"), "ContextRolesCredentialTester expects property 'role'.");
		Assert.isTrue(receiver instanceof SecurityBindingImpl);
		Assert.isTrue(expectedValue instanceof String);
		
		return ((SecurityBinding) receiver).hasRole((String) args[0], (String) expectedValue);
	}

}
