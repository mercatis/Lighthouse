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
package com.mercatis.lighthouse3.ui.operations.ui.properties.testers;

import java.util.Iterator;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.ui.operations.base.model.OperationInstallationWrapper;
import com.mercatis.lighthouse3.ui.security.CodeGuard;
import com.mercatis.lighthouse3.ui.security.Role;

/**
 *
 */
public class OperationAvailableTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof IStructuredSelection))
			return false;
		
		Iterator<?> it = ((IStructuredSelection) receiver).iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof OperationInstallationWrapper) {
				Operation op = ((OperationInstallationWrapper) obj).getOperation();
				OperationInstallation oi = ((OperationInstallationWrapper) obj).getOperationInstallation();
					
				if (op == null || !CodeGuard.hasRole(Role.OPERATION_EXECUTE, oi))
					return false;
			}
		}
				
		return true;
	}
}
