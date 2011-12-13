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
package com.mercatis.lighthouse3.ui.environment.handlers;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;

import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;

/**
 * Simple tester to check values in selection of common navigator
 * 
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	private static enum PARAMETER {multiple, deployment, project_open};
	
	public PropertyTester() {
	}

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof TreeSelection) {
			TreeSelection selection = (TreeSelection) receiver;
			boolean expected = (Boolean) expectedValue;
			for (Object o : args) {
				PARAMETER p;
				try {
					p = PARAMETER.valueOf((String) o);
				}
				catch (Exception e) {
					System.err.println("Argument " + o + " is not supported.\nAvailable args are:");
					for (PARAMETER pa : PARAMETER.values()) {
						System.err.println(pa);
					}
					return false;
				}
				
				switch (p) {
				case multiple:
					return (selection.size() > 1) == expected;
				case deployment:
					if (selection.size() == 1) {
						TreePath path = selection.getPaths()[0];
						if (path != null && path.getParentPath() != null) {
							return (path.getParentPath().getLastSegment() instanceof Location) == expected;
						}
					}
					break;
				case project_open:
					for (TreePath path : selection.getPaths()) {
						if (path.getFirstSegment() instanceof LighthouseDomain) {
							LighthouseDomain lighthouseDomain = (LighthouseDomain) path.getFirstSegment();
							return lighthouseDomain.getProject().isOpen() == expected;
						}
					}
					break;
					default:
						return false;
				}
			}
		}
		return false;
	}

}
