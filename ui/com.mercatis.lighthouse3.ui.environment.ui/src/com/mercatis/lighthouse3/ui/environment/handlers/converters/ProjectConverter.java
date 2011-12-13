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
package com.mercatis.lighthouse3.ui.environment.handlers.converters;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

public class ProjectConverter extends AbstractParameterValueConverter {

	@Override
	public Object convertToObject(String parameterValue) throws ParameterValueConversionException {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(parameterValue);
	}

	@Override
	public String convertToString(Object parameterValue) throws ParameterValueConversionException {
		return ((IProject) parameterValue).getName();
	}

}
